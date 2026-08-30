# Ticket Platform

面向演出的票务交易平台，聚焦“票档售卖”场景，提供演出管理、库存防超卖、异步建单、支付出票与一次性验票的微服务闭环。

## 核心链路

`浏览演出 → 选择场次和票档 → Redis Lua 预扣 → MQ 异步建单 → 15 分钟内模拟支付 → MQ 出票 → 二维码验票`

## 模块

| 模块 | 职责 |
| --- | --- |
| `ticket-common` | 公共异常、配置、鉴权和工具类 |
| `ticket-api` | Feign 客户端与跨服务 DTO |
| `ticket-gateway` | 网关和统一鉴权入口 |
| `user-service` | 用户登录与观演人基础能力 |
| `event-service` | 演出、场次、票档及票档库存 |
| `order-service` | 票务订单、超时关闭、电子票、验票与抢票队列 |
| `payment-service` | 模拟支付单和支付成功消息 |
| `ticket-service` | 购票清单预留模块 |
| `ticket-mvp-demo` | 无外部依赖的可执行业务规则演示 |

## 技术栈

Spring Boot 2.7、Spring Cloud Alibaba、MyBatis-Plus、MySQL、Redis、RabbitMQ、Nacos、OpenFeign、Sentinel（可选）。项目的 Maven 编译目标为 Java 11；本地可用 JDK 17 以 `--release 11` 方式构建。

## 明确边界

已实现：票档售卖、场次限购、Redis Lua 预扣、MQ 异步建单、失败回补、模拟支付、超时关单、电子票和一次性验票。

不实现：选座、真实第三方支付、退款、真实证件核验、主办方后台与生产级风控。

## 快速开始

1. 按 [本地环境说明](docs/setup/LOCAL_SETUP.md) 启动 MySQL、Redis、RabbitMQ 与 Nacos。
2. 执行初始化脚本：`docs/sql/01-ticket-schema.sql`、`docs/sql/02-demo-data.sql`。
3. 将 [配置模板](docs/config/README.md) 中的示例值写入本地配置或 Nacos。
4. 编译：

   ```powershell
   mvn -DskipTests compile
   ```

5. 依次启动 `event-service`、`order-service`、`payment-service`；需要网关时再启动 `ticket-gateway`。
6. 使用 [演示请求集](docs/http/ticket-demo.http) 完成全链路演示。

无需外部服务即可验证核心规则：

```powershell
.\ticket-mvp-demo\run-demo.ps1
```

## 项目文档

- [接口操作与故障排查](docs/demo/DEMO_GUIDE.md)
- [架构、时序和状态图](docs/architecture/ARCHITECTURE.md)
