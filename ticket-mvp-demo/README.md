# Ticket MVP Demo

零外部依赖的可执行演示模块，覆盖简历项目的核心业务边界：票档库存、场次限购、待支付订单、15 分钟超时释放、模拟支付、电子票生成和一次性验票。

执行：

```powershell
mvn -f ticket-mvp-demo/pom.xml package
java -jar ticket-mvp-demo/target/ticket-mvp-demo-1.0.0.jar
```

本模块用内存和 `synchronized` 模拟原子预扣，目的是让业务闭环可直接运行。接入微服务时，应将 `createOrder` 中的原子段替换为 Redis Lua 预扣，并将订单创建、支付成功和出票拆为 MQ 消费链路。
