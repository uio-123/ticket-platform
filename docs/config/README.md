# 本地配置模板

以下值仅为占位示例，必须在本机或 Nacos 中覆盖，不能提交真实凭据。

```yaml
ticket:
  db:
    host: ${TICKET_DB_HOST:127.0.0.1}
    pw: ${TICKET_DB_PASSWORD:change-me}
spring:
  cloud:
    nacos:
      server-addr: ${NACOS_ADDR:127.0.0.1:8848}
  redis:
    host: ${REDIS_HOST:127.0.0.1}
    port: ${REDIS_PORT:6379}
  rabbitmq:
    host: ${RABBITMQ_HOST:127.0.0.1}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:guest}
    password: ${RABBITMQ_PASSWORD:guest}
```

`event-service` 使用 `ticket_event`，`order-service` 使用 `ticket_order`，`payment-service` 使用 `ticket_payment`，`user-service` 使用 `ticket_user`。将对应数据库名配置到 Nacos 的共享 JDBC 配置中。
