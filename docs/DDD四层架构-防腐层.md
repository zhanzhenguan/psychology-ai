# DDD 四层架构完整职责说明（含防腐层 ACL）

## 总览：四层一句话定义 + 防腐层定位

| 层 | 一句话 | 核心问题 | 防腐层角色 |
|----|--------|---------|-----------|
| **Interfaces（用户接口层）** | 系统对外暴露的"门面" | 外部怎么调用我？ | 入站防腐（协议转换） |
| **Application（应用层）** | 业务流程的"编舞者" | 这个业务用例怎么串起来？ | 协调端口调用 |
| **Domain（领域层）** | 业务规则的"心脏" | 业务逻辑是什么？ | **定义端口（Port）**——声明需要什么外部能力 |
| **Infrastructure（基础设施层）** | 技术能力的"手脚" | 数据怎么存？消息怎么发？ | **实现适配器（Adapter）**——防腐层的核心实现 |

---

## 第一层：Interfaces（用户接口层）

### 职责

**协议适配 + 参数校验 + DTO 转换**，仅此而已。

它是系统与外部世界的交界处——HTTP API、RPC、消息消费者、定时任务入口，都算 interfaces 层。

### 包含什么

```
interfaces/
├── OrderController.java        # HTTP 入口
├── OrderDTO.java               # 请求/响应 DTO
├── OrderDTOAssembler.java      # DTO ↔ 领域对象转换
├── GlobalExceptionHandler.java # 异常转 HTTP 状态码
└── OrderEventConsumer.java     # MQ 消息消费入口（入站防腐）
```

### 不该做什么

| ❌ 不该做 | 为什么 |
|-----------|--------|
| 写业务逻辑 | 那是 domain 的事 |
| 直接操作数据库 | 那是 infra 的事 |
| 编排多个聚合的协作 | 那是 application 的事 |
| 依赖 domain 的具体类（除了 DTO 转换） | 应该只依赖 application 接口 |

### 代码示例（该做的）

```java
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderAppService orderAppService;

    @PostMapping
    public Result<OrderSubmitResponse> submit(@Valid @RequestBody SubmitOrderRequest request) {
        // 1. DTO 转换（interfaces 层的职责）
        SubmitOrderCommand command = SubmitOrderCommand.builder()
            .customerId(request.getCustomerId())
            .productId(request.getProductId())
            .quantity(request.getQuantity())
            .build();

        // 2. 调用应用层（不做业务判断）
        OrderId orderId = orderAppService.submitOrder(command);

        // 3. 组装响应
        return Result.success(new OrderSubmitResponse(orderId.value()));
    }
}
```

---

## 第二层：Application（应用层）

### 职责

**用例编排 + 事务边界 + 跨聚合协调 + 通过端口调用外部能力**。

它不关心"订单怎么计算总价"（那是 domain），它只关心"下单这个用例需要依次调用哪些聚合、事务怎么管、外部系统怎么调"。

### 包含什么

```
application/
├── OrderAppService.java        # 应用服务（用例入口，@Transactional）
├── InventoryAppService.java
├── command/                    # 命令对象（CQRS）
│   ├── SubmitOrderCommand.java
│   └── CancelOrderCommand.java
├── query/                      # 查询对象
│   └── OrderQueryService.java
├── dto/                        # 应用层 DTO
│   └── OrderSummaryDTO.java
└── DomainEventPublisher.java   # 事件发布接口（定义，实现在 infra）
```

### 核心特征

```java
@Service
@Transactional
public class OrderAppService {

    private final OrderRepository orderRepository;
    private final PaymentClientPort paymentClient;  // ← 依赖防腐层端口

    public OrderId submitOrder(SubmitOrderCommand command) {
        // 1. 加载聚合
        Order order = Order.create(...);

        // 2. 通过防腐层端口调用外部支付系统（不关心谁实现）
        PaymentResult result = paymentClient.initiatePayment(
            order.getId(), order.getTotalAmount());

        if (!result.success()) {
            throw new BusinessException("支付发起失败");
        }

        // 3. 持久化
        orderRepository.save(order);
        return order.getId();
    }
}
```

### 不该做什么

| ❌ 不该做 | 为什么 |
|-----------|--------|
| 包含 if/else 业务规则 | 这是 domain 的职责 |
| 直接 new 一个 Repository 实现 | 应该通过接口注入 |
| 直接调用外部 HTTP/SDK | 通过防腐层端口调用 |
| 处理序列化细节 | 那是 interfaces 的事 |

---

## 第三层：Domain（领域层）—— 最重要的一层

### 职责

**业务规则 + 状态变更 + 不变量保证 + 声明外部能力端口**。

这是系统的核心价值所在。它不依赖任何框架、任何数据库——**纯 POJO**。

### 包含什么

```
domain/
├── model/                       # 实体、值对象、聚合根
│   ├── Order.java               # 聚合根（充血模型）
│   ├── OrderItem.java
│   ├── Money.java               # 值对象（不可变）
│   └── OrderStatus.java
├── event/                       # 领域事件
│   ├── DomainEvent.java
│   └── OrderSubmittedEvent.java
├── repository/                  # 仓储接口
│   ├── OrderRepository.java
│   └── InventoryRepository.java
├── port/                        # 🛡️ 防腐层端口（声明外部能力）
│   ├── PaymentClientPort.java   # 支付能力
│   ├── InventoryClientPort.java # 库存能力
│   └── NotificationPort.java    # 通知能力
├── service/                     # 领域服务（仅当逻辑不属于单个聚合时）
│   └── OrderTransferService.java
└── exception/
    └── DomainException.java
```

### 端口定义示例（防腐层的核心契约）

```java
// domain/port/PaymentClientPort.java
package com.example.ddd.order.domain.port;

import com.example.ddd.order.domain.model.OrderId;

/**
 * 支付能力端口
 * 定义"订单子域需要什么样的支付能力"，不关心谁实现
 */
public interface PaymentClientPort {
    PaymentResult checkPaymentStatus(OrderId orderId);
    PaymentResult initiatePayment(OrderId orderId, Money amount);

    record PaymentResult(boolean success, String transactionId, PaymentStatus status) {}
    enum PaymentStatus { PENDING, COMPLETED, FAILED, REFUNDED }
}
```

### 充血模型示例

```java
public class Order {
    private OrderId id;
    private OrderStatus status;
    private List<DomainEvent> domainEvents = new ArrayList<>();

    public static Order create(OrderId id, CustomerId customerId, String productId, int quantity) {
        if (quantity <= 0) throw new DomainException("数量必须大于0");
        Order order = new Order();
        order.id = id;
        order.status = OrderStatus.DRAFT;
        order.domainEvents.add(new OrderCreatedEvent(id));
        return order;
    }

    public void submit() {
        if (status != OrderStatus.DRAFT) throw new DomainException("只有草稿可提交");
        this.status = OrderStatus.SUBMITTED;
        this.domainEvents.add(new OrderSubmittedEvent(this.id));
    }

    public List<DomainEvent> pullEvents() {
        List<DomainEvent> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return events;
    }
}
```

### 不该做什么

| ❌ 不该做 | 为什么 |
|-----------|--------|
| 依赖 Spring / MyBatis / Redis | domain 必须纯净 |
| 直接操作数据库 | 通过 Repository 接口 |
| 直接调用外部 HTTP/SDK | 通过 Port 接口声明，infra 实现 |
| 包含 JSON 序列化逻辑 | 那是 Converter 的事 |

---

## 第四层：Infrastructure（基础设施层）

### 职责

**技术实现——所有 domain 层定义的接口，在这里提供具体实现，包括防腐层适配器**。

它是整个系统的"手脚"：数据库存取、缓存、消息队列、外部 API 调用、防腐层翻译。

### 包含什么

```
infrastructure/
├── persistence/                 # 持久化
│   ├── OrderDO.java
│   ├── OrderMapper.java
│   ├── OrderRepositoryImpl.java
│   └── OrderConverter.java
├── acl/                         # 🛡️ 防腐层实现（核心）
│   ├── payment/
│   │   ├── PaymentClientAdapter.java    # 实现 PaymentClientPort
│   │   ├── ExternalPaymentDTO.java      # 外部系统 DTO
│   │   └── PaymentTranslator.java       # 外部模型 → 本地值对象
│   ├── inventory/
│   │   ├── InventoryClientAdapter.java  # 实现 InventoryClientPort
│   │   └── InventoryTranslator.java
│   └── notification/
│       └── EmailNotificationAdapter.java
├── redis/                       # Redis
│   ├── RedisConfig.java
│   └── RedisOrderCache.java
├── event/                       # 事件发布实现
│   ├── OutboxRelay.java
│   └── KafkaDomainEventPublisher.java
├── outbox/                      # Outbox 模式
│   ├── OutboxDO.java
│   └── OutboxMapper.java
└── config/                      # 全局配置
    └── MyBatisConfig.java
```

### 防腐层适配器示例

```java
// infrastructure/acl/payment/PaymentClientAdapter.java
@Component
@RequiredArgsConstructor
public class PaymentClientAdapter implements PaymentClientPort {

    private final ExternalPaymentClient externalClient;  // 外部 SDK/Feign

    @Override
    public PaymentResult checkPaymentStatus(OrderId orderId) {
        ExternalPaymentDTO dto = externalClient.query(orderId.value());
        return PaymentTranslator.toPaymentResult(dto);  // 翻译防腐
    }

    @Override
    public PaymentResult initiatePayment(OrderId orderId, Money amount) {
        ExternalPaymentRequest request = PaymentTranslator.toExternalRequest(orderId, amount);
        ExternalPaymentDTO dto = externalClient.pay(request);
        return PaymentTranslator.toPaymentResult(dto);
    }
}
```

### 翻译器（Translator）—— 防腐层的核心

```java
public class PaymentTranslator {
    public static PaymentResult toPaymentResult(ExternalPaymentDTO dto) {
        return new PaymentResult(
            "SUCCESS".equals(dto.getCode()),
            dto.getTransactionId(),
            mapStatus(dto.getStatus())
        );
    }

    private static PaymentStatus mapStatus(String externalStatus) {
        return switch (externalStatus) {
            case "PAID" -> PaymentStatus.COMPLETED;
            case "FAILED" -> PaymentStatus.FAILED;
            default -> PaymentStatus.PENDING;
        };
    }
}
```

### 不该做什么

| ❌ 不该做 | 为什么 |
|-----------|--------|
| 包含业务判断 | 业务逻辑应该在 domain |
| 被 domain 层依赖 | 依赖倒置：domain 定义接口，infra 实现 |
| 直接暴露外部 DTO 给 application/domain | 通过 Translator 转成本地值对象 |
| 一个 Adapter 调用 5 个外部系统 | 按外部系统拆分多个 Adapter |

---

## 依赖方向（铁律 + 防腐层）

```
Interfaces → Application → Domain ← Infrastructure
                  ↓                    ↑
            Domain Port ←────── ACL Adapter
```

**规则**：
1. **Interfaces 依赖 Application**（调用应用服务）
2. **Application 依赖 Domain**（编排聚合，调用仓储接口和防腐层端口）
3. **Domain 不依赖任何层**（最纯净，只定义 Port 接口）
4. **Infrastructure 依赖 Domain**（实现 Repository + 实现防腐层 Adapter）
5. **防腐层 Adapter 在 Infrastructure，接口（Port）在 Domain**

> 如果 domain 需要调用外部系统，在 domain 定义 Port 接口 → infra 的 ACL 实现 Adapter → Application 通过 Port 调用

---

## ACL vs 事件驱动（什么时候用哪个？）

| 场景 | 用 ACL（同步调用） | 用事件（异步解耦） |
|------|-------------------|-------------------|
| 下单时**实时校验库存** | ✅ 调用库存子域接口 | ❌ 太慢 |
| 订单支付后**通知物流发货** | ❌ 不需要实时 | ✅ 发事件 |
| 查询外部**最新汇率** | ✅ 同步查询 | ❌ |
| 订单取消后**释放库存** | 看情况 | ✅ 事件更解耦 |
| 调用**支付宝/微信支付** | ✅ 必须同步 | ❌ |

---

## 四层 vs 传统三层（含防腐层对比）

| 维度 | 三层架构 | DDD 四层 + ACL |
|------|---------|---------------|
| Service 层 | 又当编排又当业务 | Application 编排，Domain 管业务 |
| 外部调用 | 直接在 Service 里 @Autowired FeignClient | 通过 Domain Port + Infra Adapter |
| 外部模型 | 直接传到 Service/Entity | ACL Translator 翻译，外部模型不进领域层 |
| 可测试性 | 要启动 Spring + Mock 外部服务 | Domain 纯 POJO 毫秒级测试，ACL 可独立 Mock |

---

## 一句话记忆法（含防腐层）

> **Interfaces 接客，Application 排戏，Domain 演戏+定规矩（Port），Infrastructure 搭台+做翻译（ACL Adapter）。**

- **Interfaces**：把外部请求翻译成内部命令
- **Application**：编排谁先谁后、事务怎么管、通过端口调外部
- **Domain**：业务规则的唯一真相源 + 声明需要什么外部能力（Port）
- **Infrastructure**：所有脏活累活 + 防腐层翻译（Adapter 实现 Port）

---

## 完整目录结构参考

```
order/
├── interfaces/      → Controller, DTO, DTOAssembler
├── application/     → AppService, Command, Query, DTO
├── domain/
│   ├── model/       → 聚合根、值对象、枚举
│   ├── event/       → 领域事件
│   ├── repository/  → 仓储接口
│   ├── port/        → 🛡️ 防腐层端口（PaymentClientPort 等）
│   ├── service/     → 领域服务
│   └── exception/   → 领域异常
└── infrastructure/
    ├── persistence/  → Repository 实现、DO、Mapper、Converter
    ├── acl/          → 🛡️ 防腐层适配器 + 翻译器
    ├── redis/        → 缓存配置与实现
    ├── event/        → 事件发布实现
    ├── outbox/       → Outbox 模式
    └── config/       → 全局技术配置
```
