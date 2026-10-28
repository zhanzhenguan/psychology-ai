# DDD 四层架构每层功能职责

## 总览：四层一句话定义

| 层 | 一句话 | 核心问题 |
|----|--------|---------|
| **Interfaces（用户接口层）** | 系统对外暴露的"门面" | 外部怎么调用我？ |
| **Application（应用层）** | 业务流程的"编舞者" | 这个业务用例怎么串起来？ |
| **Domain（领域层）** | 业务规则的"心脏" | 业务逻辑是什么？ |
| **Infrastructure（基础设施层）** | 技术能力的"手脚" | 数据怎么存？消息怎么发？ |

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
└── OrderEventConsumer.java     # MQ 消息消费入口（如果是被调用方）
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

**用例编排 + 事务边界 + 跨聚合协调**。

它不关心"订单怎么计算总价"（那是 domain），它只关心"下单这个用例需要依次调用哪些聚合、事务怎么管、事件怎么发"。

### 包含什么

```
application/
├── OrderAppService.java        # 应用服务（用例入口）
├── InventoryAppService.java
├── command/                    # 命令对象（CQRS）
│   ├── SubmitOrderCommand.java
│   └── CancelOrderCommand.java
├── query/                      # 查询对象
│   └── OrderQueryService.java  # 读模型查询（可绕过 domain）
├── dto/                        # 应用层 DTO（跨层传递）
│   └── OrderSummaryDTO.java
└── DomainEventPublisher.java   # 事件发布接口（定义，实现在 infra）
```

### 核心特征

```java
@Service
@Transactional
public class OrderAppService {

    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;
    private final DomainEventPublisher eventPublisher;

    public OrderId submitOrder(SubmitOrderCommand command) {
        // 1. 加载聚合
        Inventory inventory = inventoryRepository.findByProductId(
            command.getProductId());

        // 2. 业务校验（委托给 domain）
        if (!inventory.isAvailable(command.getQuantity())) {
            throw new BusinessException("库存不足");
        }

        // 3. 创建聚合（业务逻辑在 domain 内执行）
        Order order = Order.create(
            OrderId.nextId(),
            command.getCustomerId(),
            command.getProductId(),
            command.getQuantity()
        );

        // 4. 持久化
        orderRepository.save(order);
        inventoryRepository.save(inventory);

        // 5. 发布事件
        order.pullEvents().forEach(eventPublisher::publish);

        return order.getId();
    }
}
```

### 不该做什么

| ❌ 不该做 | 为什么 |
|-----------|--------|
| 包含 if/else 业务规则（如"如果金额>1000则免邮"） | 这是 domain 的职责 |
| 直接 new 一个 Repository 实现 | 应该通过接口注入 |
| 处理 HTTP/Serialization 细节 | 那是 interfaces 的事 |
| 直接调用另一个子域的 Repository | 跨子域通过事件或防腐层 |

---

## 第三层：Domain（领域层）—— 最重要的一层

### 职责

**业务规则 + 状态变更 + 不变量保证**。

这是系统的核心价值所在。它不依赖任何框架、任何数据库、任何网络——**纯 POJO**，可以在任何环境运行。

### 包含什么

```
domain/
├── model/                       # 实体、值对象、聚合根
│   ├── Order.java               # 聚合根（充血模型）
│   ├── OrderItem.java           # 实体
│   ├── Money.java               # 值对象（不可变）
│   ├── OrderStatus.java         # 枚举
│   └── Inventory.java           # 另一个聚合
├── event/                       # 领域事件
│   ├── DomainEvent.java         # 标记接口
│   └── OrderSubmittedEvent.java
├── repository/                  # 仓储接口（定义，不实现）
│   ├── OrderRepository.java
│   └── InventoryRepository.java
├── service/                     # 领域服务（仅当逻辑不属于单个聚合时）
│   └── OrderTransferService.java
└── exception/
    └── DomainException.java
```

### 充血模型示例

```java
public class Order {
    private OrderId id;
    private CustomerId customerId;
    private List<OrderItem> items;
    private OrderStatus status;
    private Money totalAmount;
    private List<DomainEvent> domainEvents = new ArrayList<>();

    // 构造函数私有化，通过工厂方法创建
    public static Order create(OrderId id, CustomerId customerId,
                                String productId, int quantity) {
        if (quantity <= 0) {
            throw new DomainException("购买数量必须大于0");
        }
        Order order = new Order();
        order.id = id;
        order.customerId = customerId;
        order.items = new ArrayList<>();
        order.items.add(new OrderItem(productId, quantity));
        order.status = OrderStatus.DRAFT;
        order.totalAmount = Money.zero();
        order.domainEvents.add(new OrderCreatedEvent(id, customerId));
        return order;
    }

    // 行为方法——业务规则内聚在这里
    public void submit() {
        if (status != OrderStatus.DRAFT) {
            throw new DomainException("只有草稿状态可以提交");
        }
        this.status = OrderStatus.SUBMITTED;
        this.domainEvents.add(new OrderSubmittedEvent(this.id, this.customerId, this.items));
    }

    public void addItem(String productId, int quantity) {
        if (status != OrderStatus.DRAFT) {
            throw new DomainException("只有草稿状态可以添加商品");
        }
        if (quantity <= 0) {
            throw new DomainException("数量必须大于0");
        }
        items.add(new OrderItem(productId, quantity));
    }

    // 只暴露行为，不暴露可变集合
    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
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
| 依赖 Spring / MyBatis / Redis | domain 必须纯净，可独立测试 |
| 直接操作数据库 | 通过 Repository 接口 |
| 调用 HTTP / MQ | 通过事件或防腐层 |
| 包含 JSON / XML 序列化逻辑 | 那是 Converter 的事 |
| 包含 `new Date()` / `UUID.randomUUID()` | 时间、ID 应由外部传入或通过接口抽象 |

---

## 第四层：Infrastructure（基础设施层）

### 职责

**技术实现——所有 domain 层定义的接口，在这里提供具体实现**。

它是整个系统的"手脚"：数据库存取、缓存、消息队列、外部 API 调用、文件存储。

### 包含什么

```
infrastructure/
├── persistence/                 # 持久化
│   ├── OrderDO.java             # 数据对象
│   ├── OrderMapper.java         # MyBatis Mapper
│   ├── OrderRepositoryImpl.java # Repository 接口的实现
│   ├── OrderConverter.java      # Domain ↔ DO 转换
│   └── mapper/OrderMapper.xml
├── redis/                       # Redis
│   ├── RedisConfig.java
│   ├── RedisOrderCache.java
│   └── RedisDistributedLock.java
├── event/                       # 事件发布实现
│   ├── OutboxRelay.java         # Outbox 扫描器
│   ├── KafkaDomainEventPublisher.java
│   └── RedisDomainEventPublisher.java
├── outbox/                      # Outbox 模式
│   ├── OutboxDO.java
│   ├── OutboxMapper.java
│   └── OutboxRelay.java
├── external/                    # 外部系统调用
│   ├── PaymentClient.java
│   └── PaymentClientImpl.java
└── config/                      # 全局配置
    ├── MyBatisConfig.java
    └── JacksonConfig.java
```

### Repository 实现示例

```java
@Repository
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderMapper orderMapper;
    private final OrderConverter converter;

    @Override
    public Order findById(OrderId id) {
        OrderDO orderDO = orderMapper.selectById(id.value());
        if (orderDO == null) {
            throw new EntityNotFoundException("Order not found: " + id);
        }
        return converter.toDomain(orderDO);
    }

    @Override
    public void save(Order order) {
        OrderDO orderDO = converter.toDO(order);
        if (orderMapper.existsById(orderDO.getId())) {
            orderMapper.updateById(orderDO);
        } else {
            orderMapper.insert(orderDO);
        }
        // 同时保存 Outbox 事件
        order.pullEvents().forEach(event -> {
            outboxMapper.insert(converter.toOutboxEvent(event));
        });
    }
}
```

### 不该做什么

| ❌ 不该做 | 为什么 |
|-----------|--------|
| 包含业务判断（如"如果状态是XX才更新"） | 业务逻辑应该在 domain |
| 被 domain 层依赖 | 依赖倒置：domain 定义接口，infra 实现 |
| 直接暴露 DO 给 application/domain | 通过 Converter 转成领域对象 |

---

## 依赖方向（铁律）

```
Interfaces → Application → Domain ← Infrastructure
                  ↓
            Domain（定义接口）
                  ↑
            Infrastructure（实现接口）
```

**规则**：
1. **Interfaces 依赖 Application**（调用应用服务）
2. **Application 依赖 Domain**（编排聚合，调用仓储接口）
3. **Domain 不依赖任何层**（最纯净）
4. **Infrastructure 依赖 Domain**（实现仓储接口、事件接口）

> 如果 domain 需要调用技术能力（如发消息），**在 domain 定义接口，infrastructure 提供实现**——这就是依赖倒置。

---

## 四层 vs 传统三层

| 维度 | 三层架构 | DDD 四层 |
|------|---------|----------|
| Service 层 | 又当编排又当业务，膨胀成上帝类 | Application 只编排，Domain 管业务 |
| Entity | 贫血 POJO，只有 getter/setter | 充血模型，封装业务规则 |
| DAO/Mapper | Service 直接调用 | 通过 Repository 接口隔离 |
| 事务 | 到处 `@Transactional` | 集中在 Application 层 |
| 可测试性 | 要启动 Spring 才能测业务 | Domain 层纯 POJO，毫秒级单元测试 |

---

## 一句话记忆法

> **Interfaces 接客，Application 排戏，Domain 演戏，Infrastructure 搭台。**

- Interfaces：把外部请求翻译成内部命令
- Application：编排谁先谁后、事务怎么管
- Domain：业务规则的唯一真相源
- Infrastructure：所有脏活累活的技术实现
