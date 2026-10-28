# DDD 分层规范与演进触发点

> 本文档记录 psychology-ai 项目当前的分层链路取舍，以及**什么信号出现时必须重构分层**。
> 目的：避免为了"教科书分层"硬塞空壳类，也避免该隔离时不隔离导致腐化。

---

## 一、当前分层链路（写 / 读不对称）

### 写侧（长链路，保护业务不变量）

```
HTTP Request
  → interfaces/Controller  (@Valid 校验)
  → application/Command    (当前直接复用 Command 做 @RequestBody，无独立 Request)
  → application/XxxCommandService   (事务边界、用例编排)
  → domain/XxxFactory.create(...)   (聚合根构造，值对象校验)
  → domain/Xxx 聚合根  (行为方法内部 registerEvent)
  → infrastructure/XxxRepositoryImpl (MapStruct: 聚合根 → PO)
  → DB
  → 事务提交后 @TransactionalEventListener(AFTER_COMMIT) 发领域事件
```

### 读侧（短链路，不经过聚合根）

```
DB
  → infrastructure/XxxMapper.selectXxx() → PO
  → infrastructure/XxxReadConverter (MapStruct: PO → ReadModel，扁平投影，无值对象)
  → application/XxxQueryService  (读端口)
  → interfaces/XxxWebConverter (MapStruct: ReadModel → Response)
  → HTTP Response
```

**关键约定**：
- 写侧转换器 `XxxPersistenceConverter`：PO ↔ 聚合根，走值对象
- 读侧转换器 `XxxReadConverter`：PO → ReadModel，扁平字符串列直接投影
- 接口层转换器 `XxxWebConverter`：ReadModel → Response
- 三者职责切开，不互相复用

---

## 二、Request → Command 这层：加还是不加

### 当前取舍：**不加**

`RegisterUserCommand` / `LoginCommand` 直接作为 Controller 的 `@RequestBody`，字段与用例输入完全一致。
理由：单端、内部系统、无渠道/签名/版本字段，加一层纯搬运是样板代码。

### 触发以下任一信号时，**必须抽 Request 层**

| 信号 | 为什么必须加 |
|---|---|
| 多端接入（App / 小程序 / H5 / 开放平台）字段不同 | 一个 Command 装不下各端差异 |
| 请求带签名、渠道号、设备 ID、推广参数、客户端版本 | 这些是展示/接入层字段，不该污染用例输入 |
| 接口要版本演进（v1/v2 字段名/语义不同） | 隔离接口契约变化，application 层不动 |
| 接口层校验规则与用例校验关注点不同（如接口要加密参数解密后再校验） | 注解堆在 Command 上会让领域用例被接口细节污染 |
| 对外开 API，字段命名要遵循开放平台规范（下划线 vs 驼峰） | Command 保持领域命名，Request 做翻译 |

### 抽 Request 层时的标准做法

```
interfaces/dto/request/RegisterUserRequest.java   ← 带 @Valid 注解、接口契约字段
interfaces/converter/UserWebConverter.java        ← Request.toCommand() MapStruct 一行
application/user/command/RegisterUserCommand.java ← 纯字段，无校验注解，用例输入
```

转换用 MapStruct，不要手写；Request 和 Command 字段名相同时 MapStruct 自动映射。

---

## 三、值对象：加还是不加

### 当前保留的 4 个值对象（都有真实业务不变量）

| 值对象 | 不变量 |
|---|---|
| `Username` | 4-64 位、字母数字下划线、登录主键 |
| `Password` | 密文承载 + `toString` 脱敏（安全边界） |
| `Email` | 格式校验 + 唯一索引 |
| `Phone` | 大陆手机号格式 + 唯一索引 |

### 已删除（反面教材）

- ~~`Nickname`~~：纯展示字符串，无行为、无唯一约束 → 降级为 `String`
- ~~`Points`~~：没有任何聚合持有积分，纯占位 → YAGNI 删除

### 触发以下信号时，才把一个字段升级为值对象

| 信号 | 例子 |
|---|---|
| 字段有**多条校验规则**且跨用例复用 | 金额（币种+小数位+正负）、身份证号、订单号格式 |
| 字段有**业务行为**（不只是数据） | 时间区间（重叠判断）、地址（格式化） |
| 字段**必须和其他字段一起作为整体替换** | 金额+币种必须成对出现 |
| 字段需要**脱敏/安全边界** | 密码、身份证、手机号中间四位 |
| 字段作为**跨聚合标识**被多个聚合引用 | 用户 ID、订单号 |

反例：只有"非空 + 最大长度 64"的展示字段，不要值对象化。

---

## 四、领域事件：什么时候由聚合根发，什么时候由应用层发

### 当前约定：聚合根行为方法内 `registerEvent`

```java
public void markRegistered() {
    registerEvent(UserRegisteredEvent.of(this.id, this.username.value(), ...));
}
```

应用服务在 `save` 后调 `domainEventPublisher.publishEvents(agg)` 统一发布。

### 什么时候用应用层直接 `publish(new XxxEvent(...))`

- 事件**不依赖聚合根状态**，纯跨模块通知（如"系统配置变更"）
- 事件来源不是某个聚合根的业务行为（如定时任务触发的对账事件）

### 监听器约定

- 一律 `@TransactionalEventListener(phase = AFTER_COMMIT, fallbackExecution = true)`
- 监听器只做跨聚合协作（发邮件、发风控通知），**不做本聚合的业务规则**
- 外部调用失败只打日志告警，不回滚主事务

---

## 五、Repository：写侧和读侧分开

### 当前写侧 `UserRepository` 只保留

- `findByUsername`（登录发事件要加载聚合根）
- `existsByUsername/Email/Phone`（注册唯一性校验）
- `save`

### 读侧 `UserQueryService` 独立

- 直接 `PO → ReadModel`，不加载聚合根
- 未来加列表页/详情页/搜索，都加到 QueryService，不要塞回 Repository

### 反模式（不要做）

- ❌ `UserRepository.findPage(...)` 返回聚合根列表（读侧不该走聚合根）
- ❌ 一个 Repository 同时管写和读
- ❌ 应用服务直接注入 MyBatis-Plus 的 `Mapper`（绕过领域仓储端口）

---

## 六、CQRS 包结构规范

```
application/
├── <模块>/
│   ├── command/          ← 写用例
│   │   ├── XxxCommand.java
│   │   └── XxxCommandService.java(Impl)
│   └── query/             ← 读用例
│       ├── XxxView.java   ← ReadModel（record，扁平）
│       └── XxxQueryService.java(Impl)
└── common/port/           ← 跨模块端口

infrastructure/
├── persistence/
│   ├── po/                ← 持久化对象
│   ├── mapper/            ← MyBatis Mapper
│   ├── repository/        ← 写侧仓储实现
│   ├── query/            ← 读侧查询实现
│   └── converter/
│       ├── XxxPersistenceConverter.java  ← 写：PO ↔ 聚合根
│       └── XxxReadConverter.java         ← 读：PO → View
└── event/listener/        ← 领域事件监听器

interfaces/
├── controller/
├── converter/             ← View → Response
└── dto/response/          ← 接口响应体
```

---

## 七、Checklist：新功能加聚合时照着走

- [ ] 字段有不变量 → 值对象（record），否则用 String/基本类型
- [ ] 聚合根继承 `AggregateRoot`，行为方法内 `registerEvent`
- [ ] 事件写成 `record XxxEvent(...) implements DomainEvent`
- [ ] 写侧用 `XxxCommandService`，读侧用 `XxxQueryService`
- [ ] 转换器三个：PersistenceConverter（写）/ ReadConverter（读）/ WebConverter（接口）
- [ ] 唯一性校验放 CommandService，不要塞 Controller
- [ ] 密码等敏感字段不出现在 toString / View / Response
- [ ] Controller 只做参数转发，不写业务逻辑
- [ ] 接口字段和用例输入一致时，Command 直接做 `@RequestBody`；分叉了再抽 Request
