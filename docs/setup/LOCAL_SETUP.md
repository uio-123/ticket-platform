# 本地环境说明

## 依赖与建议版本

| 组件 | 建议版本 | 作用 |
| --- | --- | --- |
| JDK | 11（或 17 编译 Java 11 目标） | 服务运行与构建 |
| MySQL | 8.0 | 演出、订单与电子票数据 |
| Redis | 6+ | 抢票库存、限购、幂等和结果查询 |
| RabbitMQ | 3.9+（启用 delayed-message 插件） | 异步建单、支付通知、延迟关单 |
| Nacos | 2.1+ | 服务发现与共享配置 |

## 启动顺序

1. MySQL：创建 `ticket_event`、`ticket_order`、`ticket_payment`、`ticket_user` 数据库。
2. 执行 `docs/sql/01-ticket-schema.sql`，再执行 `docs/sql/02-demo-data.sql`。
3. 启动 Redis 与 RabbitMQ；RabbitMQ 需开启 `rabbitmq_delayed_message_exchange` 插件以使用旧订单延迟检查。
4. 启动 Nacos，并导入 `shared-jdbc.yaml`、`shared-log.yaml`、`shared-swagger.yaml`。Seata 不属于抢票主链路，可在演示时不启用。
5. 启动服务：`event-service` → `order-service` → `payment-service` → `ticket-gateway`（可选）。

## 服务启动命令

在项目根目录执行，Windows PowerShell 示例：

```powershell
mvn -pl event-service spring-boot:run
mvn -pl order-service spring-boot:run
mvn -pl payment-service spring-boot:run
```

若未通过网关演示，可直接访问服务端口：event `8081`、order `8085`、payment `8086`。

## Nacos 最小配置要点

所有服务需具备 `ticket.db.host`、`ticket.db.pw`；包含数据库连接的服务还需对应的 `ticket.db.database`。不要提交本机数据库地址、账号或密码；可参考 `docs/config/` 的占位配置。

## 常见问题

| 现象 | 原因与处理 |
| --- | --- |
| 服务在启动时连接 Nacos 失败 | 确认 Nacos 地址与 `bootstrap.yaml` 一致，或在本地配置中覆盖地址。 |
| 抢票返回“场次尚未预热” | 先执行抢票接口集合中的 Redis 预热请求。 |
| 抢票长期为 `QUEUED` | 检查 RabbitMQ 连接、`ticket.rush.request.queue` 和订单服务消费者日志。 |
| 支付后未出票 | 检查 `ticket.pay.direct` 交换机、`ticket.pay.success.queue` 队列和订单服务日志。 |
| 订单未超时关闭 | 检查订单服务的定时任务是否运行，并确认订单创建时间已超过 15 分钟。 |
