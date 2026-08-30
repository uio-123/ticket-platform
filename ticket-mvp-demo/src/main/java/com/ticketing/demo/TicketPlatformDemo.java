package com.ticketing.demo;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 可直接运行的票档购票 MVP。
 * 生产改造时，库存预扣应迁移到 Redis Lua，订单和出票事件应通过 MQ 异步处理。
 */
public final class TicketPlatformDemo {
    public static void main(String[] args) {
        TicketService service = new TicketService(Duration.ofMinutes(15));
        Instant now = Instant.parse("2026-08-29T12:00:00Z");
        TicketTier vip = service.createTier("周末音乐节", "2026-09-20 晚场", "VIP 票档",
                new BigDecimal("1080.00"), 3, 2, now.minus(Duration.ofHours(1)), now.plus(Duration.ofDays(7)));

        Order paidOrder = service.createOrder(10001L, vip.id(), 2, now);
        service.pay(paidOrder.id(), now.plus(Duration.ofMinutes(5)));
        List<Ticket> tickets = service.ticketsOf(paidOrder.id());
        check(tickets.size() == 2, "支付后应生成两张电子票");
        service.verifyTicket(tickets.get(0).token(), now.plus(Duration.ofMinutes(6)));
        expectFailure(() -> service.verifyTicket(tickets.get(0).token(), now.plus(Duration.ofMinutes(7))), "已核验");
        expectFailure(() -> service.createOrder(10001L, vip.id(), 1, now.plus(Duration.ofMinutes(8))), "限购");

        Order expiredOrder = service.createOrder(10002L, vip.id(), 1, now.plus(Duration.ofMinutes(9)));
        check(service.availableStock(vip.id()) == 0, "待支付订单应预扣库存");
        service.releaseExpiredOrders(now.plus(Duration.ofMinutes(25)));
        check(service.order(expiredOrder.id()).status() == OrderStatus.CANCELLED, "超时订单应取消");
        check(service.availableStock(vip.id()) == 1, "超时取消应回补库存");

        System.out.println("Ticket MVP demo passed");
        System.out.printf("order=%d status=%s tickets=%d remainingStock=%d%n",
                paidOrder.id(), service.order(paidOrder.id()).status(), tickets.size(), service.availableStock(vip.id()));
    }

    private static void expectFailure(Runnable operation, String expectedMessage) {
        try {
            operation.run();
            throw new IllegalStateException("预期操作失败，但实际成功");
        } catch (BusinessException exception) {
            check(exception.getMessage().contains(expectedMessage), "异常信息不符合预期：" + exception.getMessage());
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    static final class TicketService {
        private final Duration paymentTimeout;
        private final AtomicLong tierIds = new AtomicLong();
        private final AtomicLong orderIds = new AtomicLong();
        private final AtomicLong ticketIds = new AtomicLong();
        private final Map<Long, TicketTier> tiers = new LinkedHashMap<>();
        private final Map<Long, Order> orders = new LinkedHashMap<>();
        private final Map<Long, List<Ticket>> ticketsByOrder = new LinkedHashMap<>();

        TicketService(Duration paymentTimeout) {
            this.paymentTimeout = paymentTimeout;
        }

        TicketTier createTier(String showName, String sessionName, String tierName, BigDecimal price,
                              int totalStock, int purchaseLimit, Instant saleStart, Instant saleEnd) {
            TicketTier tier = new TicketTier(tierIds.incrementAndGet(), showName, sessionName, tierName, price,
                    totalStock, totalStock, purchaseLimit, saleStart, saleEnd);
            tiers.put(tier.id(), tier);
            return tier;
        }

        synchronized Order createOrder(long userId, long tierId, int quantity, Instant now) {
            TicketTier tier = requiredTier(tierId);
            if (quantity <= 0) {
                throw new BusinessException("购票数量必须大于 0");
            }
            if (now.isBefore(tier.saleStart()) || !now.isBefore(tier.saleEnd())) {
                throw new BusinessException("当前场次未开售或已停售");
            }
            int purchased = purchasedQuantity(userId, tierId);
            if (purchased + quantity > tier.purchaseLimit()) {
                throw new BusinessException("超过场次限购数量");
            }
            if (tier.availableStock() < quantity) {
                throw new BusinessException("票已售罄");
            }
            tier.decreaseStock(quantity);
            Order order = new Order(orderIds.incrementAndGet(), userId, tierId, quantity,
                    tier.price().multiply(BigDecimal.valueOf(quantity)), OrderStatus.PENDING_PAYMENT, now);
            orders.put(order.id(), order);
            return order;
        }

        synchronized void pay(long orderId, Instant now) {
            Order order = requiredOrder(orderId);
            if (order.status() == OrderStatus.PAID) {
                return; // 支付回调幂等
            }
            if (order.status() != OrderStatus.PENDING_PAYMENT) {
                throw new BusinessException("订单不可支付");
            }
            if (!now.isBefore(order.createdAt().plus(paymentTimeout))) {
                cancel(order);
                throw new BusinessException("订单已超时关闭");
            }
            order.markPaid(now);
            List<Ticket> tickets = new ArrayList<>();
            for (int i = 0; i < order.quantity(); i++) {
                tickets.add(new Ticket(ticketIds.incrementAndGet(), order.id(),
                        "TKT-" + order.id() + "-" + (i + 1), TicketStatus.ISSUED, null));
            }
            ticketsByOrder.put(order.id(), tickets);
        }

        synchronized void releaseExpiredOrders(Instant now) {
            for (Order order : orders.values()) {
                if (order.status() == OrderStatus.PENDING_PAYMENT && !now.isBefore(order.createdAt().plus(paymentTimeout))) {
                    cancel(order);
                }
            }
        }

        synchronized void verifyTicket(String token, Instant now) {
            for (List<Ticket> tickets : ticketsByOrder.values()) {
                for (Ticket ticket : tickets) {
                    if (ticket.token().equals(token)) {
                        if (ticket.status() != TicketStatus.ISSUED) {
                            throw new BusinessException("电子票已核验");
                        }
                        ticket.verify(now);
                        return;
                    }
                }
            }
            throw new BusinessException("电子票不存在");
        }

        int availableStock(long tierId) {
            return requiredTier(tierId).availableStock();
        }

        Order order(long orderId) {
            return requiredOrder(orderId);
        }

        List<Ticket> ticketsOf(long orderId) {
            return List.copyOf(ticketsByOrder.getOrDefault(orderId, List.of()));
        }

        private int purchasedQuantity(long userId, long tierId) {
            return orders.values().stream()
                    .filter(order -> order.userId() == userId && order.tierId() == tierId && order.status() != OrderStatus.CANCELLED)
                    .mapToInt(Order::quantity)
                    .sum();
        }

        private void cancel(Order order) {
            if (order.status() == OrderStatus.PENDING_PAYMENT) {
                requiredTier(order.tierId()).increaseStock(order.quantity());
                order.cancel();
            }
        }

        private TicketTier requiredTier(long tierId) {
            TicketTier tier = tiers.get(tierId);
            if (tier == null) throw new BusinessException("票档不存在");
            return tier;
        }

        private Order requiredOrder(long orderId) {
            Order order = orders.get(orderId);
            if (order == null) throw new BusinessException("订单不存在");
            return order;
        }
    }

    static final class TicketTier {
        private final long id;
        private final String showName, sessionName, name;
        private final BigDecimal price;
        private final int totalStock, purchaseLimit;
        private int availableStock;
        private final Instant saleStart, saleEnd;

        TicketTier(long id, String showName, String sessionName, String name, BigDecimal price,
                   int totalStock, int availableStock, int purchaseLimit, Instant saleStart, Instant saleEnd) {
            this.id = id; this.showName = showName; this.sessionName = sessionName; this.name = name;
            this.price = price; this.totalStock = totalStock; this.availableStock = availableStock;
            this.purchaseLimit = purchaseLimit; this.saleStart = saleStart; this.saleEnd = saleEnd;
        }
        long id() { return id; }
        BigDecimal price() { return price; }
        int availableStock() { return availableStock; }
        int purchaseLimit() { return purchaseLimit; }
        Instant saleStart() { return saleStart; }
        Instant saleEnd() { return saleEnd; }
        void decreaseStock(int quantity) { availableStock -= quantity; }
        void increaseStock(int quantity) { availableStock += quantity; }
    }

    static final class Order {
        private final long id, userId, tierId;
        private final int quantity;
        private final BigDecimal totalAmount;
        private OrderStatus status;
        private final Instant createdAt;
        private Instant paidAt;

        Order(long id, long userId, long tierId, int quantity, BigDecimal totalAmount, OrderStatus status, Instant createdAt) {
            this.id = id; this.userId = userId; this.tierId = tierId; this.quantity = quantity;
            this.totalAmount = totalAmount; this.status = status; this.createdAt = createdAt;
        }
        long id() { return id; }
        long userId() { return userId; }
        long tierId() { return tierId; }
        int quantity() { return quantity; }
        OrderStatus status() { return status; }
        Instant createdAt() { return createdAt; }
        void markPaid(Instant paidAt) { this.status = OrderStatus.PAID; this.paidAt = paidAt; }
        void cancel() { this.status = OrderStatus.CANCELLED; }
    }

    static final class Ticket {
        private final long id, orderId;
        private final String token;
        private TicketStatus status;
        private Instant verifiedAt;
        Ticket(long id, long orderId, String token, TicketStatus status, Instant verifiedAt) {
            this.id = id; this.orderId = orderId; this.token = token; this.status = status; this.verifiedAt = verifiedAt;
        }
        String token() { return token; }
        TicketStatus status() { return status; }
        void verify(Instant verifiedAt) { this.status = TicketStatus.VERIFIED; this.verifiedAt = verifiedAt; }
    }

    enum OrderStatus { PENDING_PAYMENT, PAID, CANCELLED }
    enum TicketStatus { ISSUED, VERIFIED }
    static final class BusinessException extends RuntimeException { BusinessException(String message) { super(message); } }
}
