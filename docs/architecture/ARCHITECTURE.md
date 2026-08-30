# 架构、时序与状态图

## 服务架构

```mermaid
flowchart LR
  Client[客户端] --> Gateway[ticket-gateway]
  Gateway --> User[user-service]
  Gateway --> Event[event-service]
  Gateway --> Order[order-service]
  Order --> Redis[(Redis)]
  Order --> MQ[(RabbitMQ)]
  Order --> Event
  Payment[payment-service] --> MQ
  MQ --> Order
  Event --> EventDB[(ticket_event)]
  Order --> OrderDB[(ticket_order)]
  Payment --> PayDB[(ticket_payment)]
  User --> UserDB[(ticket_user)]
  Gateway --> Nacos[Nacos]
  Event --> Nacos
  Order --> Nacos
  Payment --> Nacos
```

## 抢票时序

```mermaid
sequenceDiagram
  participant C as Client
  participant O as order-service
  participant R as Redis
  participant M as RabbitMQ
  participant E as event-service
  participant DB as MySQL

  C->>O: POST /ticket-rush
  O->>R: Lua: 校验库存 + 场次限购 + 预扣
  R-->>O: queued / sold out / limit exceeded
  O->>M: TicketRushMessage(requestId, userId)
  O-->>C: requestId（QUEUED）
  M->>O: 异步消费消息
  O->>E: 数据库条件扣减票档库存
  E->>DB: available_stock >= quantity
  O->>DB: 创建待支付订单与明细
  O->>R: 写入 SUCCESS:orderId
  C->>O: GET /ticket-rush/{requestId}
  O-->>C: SUCCESS:orderId
  Note over O,R: 消费失败则回补 Redis 库存和用户计数
```

## 支付、出票与验票

```mermaid
sequenceDiagram
  participant P as payment-service
  participant M as RabbitMQ
  participant O as order-service
  participant DB as MySQL
  P->>M: ticket.pay.success(orderId)
  M->>O: TicketPaidListener
  O->>DB: PENDING_PAYMENT → PAID（条件更新）
  O->>DB: 生成电子票 token
  Note over O,DB: 重复支付只允许一次状态迁移；已有电子票直接复用
  O->>DB: 验票 WHERE token=? AND status=ISSUED
  DB-->>O: 成功一次，重复验票影响行数为 0
```

## 订单状态

```mermaid
stateDiagram-v2
  [*] --> PENDING_PAYMENT: 异步建单成功
  PENDING_PAYMENT --> PAID: 支付成功消息
  PENDING_PAYMENT --> CANCELLED: 超过 15 分钟 / 主动取消
  CANCELLED --> [*]: 回补票档库存
  PAID --> ISSUED: 异步生成电子票
  ISSUED --> VERIFIED: 一次性验票
  VERIFIED --> [*]
```

## 一致性边界

- 抢票入口以 Redis Lua 处理热点库存与限购；它不是最终订单事实。
- MQ 消费者完成数据库订单与票档扣减；失败时补偿 Redis 预扣结果。
- 订单状态迁移、验票均采用条件更新，避免重复消息和重复请求导致多次生效。
- 真实支付、退款、选座和跨地域容灾不在当前版本范围内。
