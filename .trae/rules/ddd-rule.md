# DDD 编码规则集（最终版）

> 适用：Cursor / Claude Code / 任何 AI 编码助手
> 版本：v3.0

## 角色

你是资深 DDD 架构师。Domain 层零框架依赖，写走全链路，读走短路，依赖方向不可逆。

## 分层与依赖
interfaces → application → domain ← infrastructure ←（实现 domain 定义的接口）


- domain 层禁止 import：springframework、mybatis、redis、infrastructure、interfaces 任何类
- application 层禁止直接 import infrastructure/persistence 的 DO/PO 类
- infrastructure 实现 domain 定义的 Port / Repository 接口

## 各层职责与规则

### Domain 层（domain/）

| 维度 | 规则                                                                         |
|------|----------------------------------------------------------------------------|
| 职责 | 聚合、实体、值对象、领域事件、领域服务、仓储接口、防腐端口                                              |
| 聚合根 | class，禁止 public 无参构造（private + 静态工厂 create/restore），禁止 setter，业务变更通过行为方法驱动 |
| 值对象 | 简单用 record（紧凑构造器校验），复杂用 class（有行为）；命名用业务语义，**禁止 XxxVO**                    |
| 领域事件 | record，实现 DomainEvent，pullEvents() 提取，保存时写 Outbox 同事务                      |
| 仓储接口 | interface，放 domain/repository/   XxxRepository                             |
| 防腐端口 | interface，放 domain/port/  XxxPort,XxxProvider,XxxAdapter                   |
| 领域服务 | class，仅放跨聚合/跨实体的领域逻辑                                                       |
| 异常 | 自定义 DomainException  不强制                                                   |
| 禁止 | @Service、@Autowired、@Entity、任何 ORM 注解、任何框架注解                               |
| 允许 import | 仅 domain/ 内部、JDK                                                           |

### Application 层（application/）

| 维度              | 规则                                                                         |
|-----------------|----------------------------------------------------------------------------|
| 职责              | 用例编排：接收 Command/Query，调 Domain/Infrastructure，协调事务                         |
| 写侧服务            | XxxAppService 等直接用 **class**，**不强制 interface+impl**；仅多部署模式/多策略时抽 interface |
| 读侧服务            | XxxQueryService 等直接用 **class**，**不强制接口**；仅多数据源降级时抽 interface               |
| Command / Query | record                                                                     |
| DTO             | record 纯数据，放 application/dto/                                              |
| View            | record CQRS 读模型投影，放 application/view/                                      |
| 写侧返回            | XxxId / CommandResult（**禁止 Response**）                                     |
| 读侧返回            | XxxDTO / XxxView（**禁止 Response**）                                          |
| 事务              | 写侧 @Transactional(rollbackFor = Exception.class)，读侧加 @Transactional(readOnly = true)                    |
| 对象转换            | DO → DTO 用 MapStruct，放 application/mapper/                                 |
| saga编排          |  |


### Interfaces 层（interfaces/）

| 维度 | 规则                                                           |
|------|--------------------------------------------------------------|
| 职责 | HTTP 入口：接收请求、返回响应、参数校验、调 Application                         |
| Request | record（简单）或 class（复杂/继承），放 interfaces/request/，@Valid 校验     |
| Response | record，含格式化（金额/时间），放 interfaces/response/                    |
| Assembler | 手写：Request→Command、DTO/View→Response，放 interfaces/assembler/ |
| Controller | 返回 XxxResponse，**禁止直接返回 DTO/View**                           |
| 禁止 | 业务逻辑、直接调 Repository、直接操作 DO/PO                               |

### Infrastructure 层（infrastructure/）

| 维度                  | 规则                                                                                 |
|---------------------|------------------------------------------------------------------------------------|
| 职责                  | 技术实现：持久化、缓存、MQ、外部系统适配                                                              |
| DO/PO               | class，放 persistence/mysql/、read/、elasticsearch/、mongo/  等                          |
| Converter           | 手写 Domain ↔ DO，放 persistence/converter/                                            |
| Adapter             | 实现 domain/port/，放 acl/{externalsystem}/                                            |
| 外部 DTO / Translator | 放 acl/ 内部，不污染 domain                                                               |
| Redis               | RedisService + DistributedLock + XxxCacheAdapter                                   |
| 事件                  | RabbitMQEventPublisher + OutboxRelay                                               |
| 其他                  | 建自外部系统的包  infrastructure/redis、infrastructure/persistence/、 infrastructure/mongo 等 |
| 禁止                  | DO 离开本层、业务逻辑写进 Mapper/Adapter                                                      |




## 命名铁律

| 层 | 输入 | 输出 | 禁止 |
|---|---|---|---|
| interfaces | XxxRequest | XxxResponse | 返回 DTO/View |
| application 写 | XxxCommand | XxxId / CommandResult | 返回 Response |
| application 读 | XxxQuery | XxxDTO / XxxView | 返回 Response |
| domain | — | 业务语义名 | DTO/VO/Response |
| infrastructure | XxxDO / XxxESDO / XxxReadDO | — | DO 泄露 |

**全局禁用 `xxxVO`**。值对象用业务名称（Value、XxxId）。



## 聚合根
- 禁止 public 无参构造，改为 private + 静态工厂方法（create / restore）
- 禁止 setter，业务变更通过领域行为方法驱动
- 状态变更必须校验不变量，违反抛 DomainException
- 领域事件在行为方法内 addEvent()，通过 pullEvents() 提取
- 聚合根用 class，不用 record

## 值对象
- 简单值对象用 record + 紧凑构造器校验
- 有复杂行为的用 class
- 运算返回新实例（不可变）
- 命名用业务语义，不叫 XxxVO

## 领域事件
- 用 record，实现 DomainEvent 标记接口
- 构造器内防御性拷贝
- 通过 Repository 保存时写入 Outbox 表，与业务数据同事务


## CQRS

| 维度 | 写侧 | 读侧 |
|------|------|------|
| 入口 | Request→Assembler→Command | Query 直接进 QueryService |
| 核心 | 聚合根行为方法 | 直接查 DO/ES/Redis |
| 事务 | @Transactional | 无 |
| 事件 | 聚合产生→Outbox→MQ | 不产生 |
| 返回 | XxxId | XxxDTO / XxxView |
| 禁止 | 读侧 import 聚合根 | 写侧绕过聚合改 DO |

## 对象转换

| 转换                     | 位置                                    | 方式 |
|------------------------|---------------------------------------|---|
| Request → Command      | interfaces/assembler/                 | 手写 |
| DTO/View → Response    | interfaces/assembler/                 | 手写 |
| Command → Domain(聚合根)       | 聚合根单例 或 domain 工厂方法              | 手写 |
| DO → DTO/View          | application/mapper/                   | MapStruct |
| Domain(聚合根)   ↔  DO/PO | infrastructure/persistence/converter/ | 手写 |





## 各层"隔离/转换"职责

| 层              | 做什么转换                   | 叫什么             | 位置                    | 推荐        |
|----------------|-------------------------|-----------------|-----------------------|-----------|
| Interfaces     | Request ↔ Command, DTO ↔ Response | Assembler       | interfaces/assembler/| 手写        |
|
| Application    | DO → DTO                | Mapper | application/mapper/   | MapStruct |
| **ACL**        | **外部模型 ↔ Domain 模型**                 | **Translator + Adapter**            | **infrastructure/acl/**                  | 手写        |
| Infrastructure |       Domain ↔ DO               |       Converter          |    infrastructure/persistence/converter/                   | 手写        |

## 规则固化

|维度 |	规则| 形式                                                             |
|-|-|----------------------------------------------------------------|
|Assembler|	手写，放 interfaces/assembler/| final class + 静态方法（无依赖时）；@Component class + 实例方法（有依赖时）；不抽接口    |
|Converter|	手写，放 infrastructure/persistence/converter/| @Component class；不抽接口（除非多数据源需要多态）                              |
|Mapper|	MapStruct，放 application/mapper/| interface（@Mapper(componentModel = "spring")）；MapStruct 自动生成实现 |
|MapStruct |禁止出现在	interfaces/、domain/、infrastructure/persistence/converter/| 无                                                              |
|手写 Mapper |禁止出现在	application/mapper/（统一用 MapStruct 保持一致性）| 无                                                              |

## 防腐层（ACL）

- Port interface 在 domain/port/
- Adapter class 在 infrastructure/acl/{externalsystem}/
- 外部 DTO 和 Translator 放 acl/ 内部
- Application 只依赖 Port 接口

## 中间件职责

| 中间件 | 用途 | 位置 |
|---|---|---|
| MySQL | 写模型 + Outbox + 读宽表 | persistence/mysql/ + read/ |
| MongoDB | 快照/审计 | persistence/mongo/ |
| Elasticsearch | 全文检索 | persistence/elasticsearch/ |
| Redis | 缓存 + 分布式锁 + 幂等 | redis/ |
| RabbitMQ | 领域事件异步投递 | event/ |

## Outbox

- 聚合保存与事件写 Outbox 同 @Transactional 事务
- OutboxRelay 异步轮询发布到 MQ

## record 使用

| 场景 | record | class |
|---|---|---|
| Command / Query | ✅ | ❌ |
| DTO / View | ✅ | ❌ |
| Request / Response | ✅ 简单 | ✅ 复杂/继承 |
| Port 返回值 | ✅ | ❌ |
| 聚合根 | ❌ | ✅ |
| 值对象 | ✅ 简单 | ✅ 复杂 |
| DO / PO | ❌ | ✅ |
| Application 服务 | ❌ | ✅ |
| Domain 服务 | ❌ | ✅ |




写侧：
 Application → Repository（接口，Domain定义）
                ↓
              RepositoryImpl（Infrastructure）
                ↓ 查 DB
              得到 OrderDO
                ↓ Converter 转换
              返回 Order（聚合根）→ Application 层

读侧：
 Application QueryService → XxxMapper（直接查）
                             ↓
                        得到 XxxReadDO（或 ESReadDO）
                             ↓ MapStruct 转换
                        得到 XxxDTO / XxxDetailView
                             ↓ Assembler 转换
                        得到 XxxResponse → Controller 返回


## 异常处理（Exception）不强制要求

| 维度         | 规则                                                                                                  |
|------------|-----------------------------------------------------------------------------------------------------|
| 基类         | BusinessException（抽象，RuntimeException），放 ddd-shared/exception/                                      |
| Domain 层   | DomainException extends BusinessException，按聚合建子类                                                    |
| Application 层 | ApplicationException extends BusinessException，按场景建子类                                               |
| 错误码        | 枚举定义，不硬编码；Domain 和 Application 各自维护                                                                 
| 全局 Handler | 只捕获 BusinessException，不逐个捕获子类                                                                       |
| 禁止         | Domain 层抛 ApplicationException；Application 层抛 DomainException（反过来可以：Domain 抛的 Application 不用 catch） |



## 单元测试

- 纯 POJO，零 Spring，零 DB
- Given-When-Then 结构
- 只测聚合根业务行为和状态变更
- 毫秒级执行

## 生成代码自检

1. domain 层有 @Service/@Autowired/@Entity → 删除
2. domain 层 import 非 domain 包 → 修正
3. 聚合根有 public 无参构造 → private
4. 聚合根有 setter → 改业务方法
5. 读侧 import 聚合根 → 改查 DO
6. 有 xxxVO 命名 → 改 DTO/View/Response
7. Port 在 domain？Adapter 在 infrastructure？
8. Outbox 与业务同事务？
9. Request→Command / DTO→Response 在 interfaces/assembler？
10. AppService/QueryService 是否多余接口 → 合并为 class
11. 单元测试纯 POJO？