---
alwaysApply: true
description: Spring Boot + Java 后端开发规范，覆盖分层架构、REST 接口、DTO、统一响应、参数校验、全局异常、事务、日志与安全
---

# Spring Boot + Java 开发规范

## 0. 技术栈基线
- Java 17+，Spring Boot 3.x
- 依赖注入用构造器注入；允许用 Lombok（@RequiredArgsConstructor / @Slf4j），但团队若禁用 Lombok 则显式写构造器和 getter/setter
- 持久层：MyBatis-Plus 或 JPA 二选一，全项目统一，不混用
- 配置外置：用 application.yml + 环境变量/配置中心，密钥禁止硬编码
- 引入依赖时，必须检测与当前的spring boot的兼容版本,需要加版本号,注释说明版本号的原因

## 1. 分层架构（硬约束）
调用链严格走：Controller → Service → Mapper/Repository → DB，禁止跨层调用（如 Controller 直接调 Mapper）[3,6,19](@ref)

- **Controller 层**
  - 只做：HTTP 方法映射、@Valid 参数校验、调 Service、返回统一响应
  - 禁止：写业务逻辑、直接查库、返回 Entity
- **Service 层**
  - 放核心业务逻辑、业务校验、事务边界（@Transactional）
  - 读方法加 @Transactional(readOnly = true)
  - 不依赖 HTTP 对象（HttpServletRequest 除外非必要不传）
- **Mapper / Repository 层**
  - 只做数据访问，SQL 走 XML 或 @Query，禁止在 Java 代码里拼 SQL 字符串
  - 复杂查询用分页对象（PageHelper / Pageable），不一次性查全表

## 2. DTO / VO 规范（硬约束）
- 接口入参用 XxxRequest / XxxQuery，出参用 XxxResponse / XxxVO
- **禁止把 Entity / DO 直接作为接口返回体**（避免懒加载异常、字段泄露、schema 耦合）[6,10](@ref)
- 实体转 DTO 放在 Service 或独立 Converter/Mapper 里，不写在 Controller
- 时间类型统一用 LocalDateTime，前后端约定格式（如 yyyy-MM-dd HH:mm:ss）

## 3. REST 接口规范
- 路径用复数名词：/api/users、/api/orders，不用 /getXxx、/doXxx
- 方法语义：GET 查、POST 建、PUT 全量改、PATCH 部分改、DELETE 删
- 返回用 ResponseEntity 或统一 Result 包装：
  - 成功：code=0 / 200，data 放数据
  - 创建成功返回 201，删除成功返回 204 或带 msg 的 200
- 分页接口统一返回 { list, total, pageNum, pageSize }
- 每个接口加 Operation 注解（Knife4j/OpenAPI），说明用途、入参、出参

## 4. 统一响应与全局异常
- 统一响应体：Result<T> { int code; String message; T data; String traceId; }
- 用 @RestControllerAdvice 集中处理，不每个 Controller 里 try-catch [4,6,13](@ref)
- 必须处理的异常：
  - MethodArgumentNotValidException → 400 + 字段级错误信息
  - 自定义 BizException / BusinessException → 业务码 + 安全文案
  - 未登录/无权限 → 401 / 403
  - 兜底 Exception → 500 + 固定文案 + 返回 traceId，**不返回堆栈、SQL、类名**
- 业务异常继承 RuntimeException，带 code + message；系统异常打 ERROR 日志，业务预期异常打 WARN
- 日志里带 traceId / MDC，方便对账 [4,13](@ref)

## 5. 参数校验
- 入参 DTO 字段用 Jakarta 校验注解：@NotNull @NotBlank @Size @Min @Max @Email @Pattern
- Controller 方法参数加 @Valid 或 @Validated 触发校验 [4,6](@ref)
- 校验注解的 message 写中文业务语（“手机号格式不正确”），不写“不能为 null”
- 跨字段校验、业务唯一性校验（如手机号已存在）放在 Service 里抛业务异常

## 6. 事务规范
- @Transactional 只加在 Service 公共方法上，不在 Controller 加
- 明确 readOnly 属性；写方法默认读写，读方法显式 readOnly=true
- 事务方法里不吞异常，不 catch 后不抛出（会导致不回滚）
- 事务粒度按业务用例走，不把整个 Service 类无脑加事务
- 涉及多表操作必须同一事务；跨服务调用走消息/补偿，不靠本地事务

## 7. 依赖注入与 Bean
- 禁止 @Autowired 字段注入，用构造器注入 + final 字段（Lombok @RequiredArgsConstructor 可接受）[6,16](@ref)
- Bean 单一职责，不在 @Component 里堆多块不相关逻辑
- 配置类用 @ConfigurationProperties 绑定，不用一堆 @Value 散着写
- 工具类加 @UtilityClass 或私有构造，不被扫成 Bean

## 8. 日志规范
- 统一 SLF4J + Logback，用 @Slf4j；禁止 System.out / System.err
- 参数化日志：log.info("order created, orderNo={}", orderNo)，禁止字符串拼接
- 级别：入口/关键节点 INFO，可预期异常 WARN，非预期异常 ERROR（带异常对象 e）
- 禁止打印：密码、token、身份证、完整银行卡号等敏感字段
- 关键链路（下单、支付、登录、导出）必须可串联 traceId

## 9. 安全红线
- 所有外部输入都视为不可信；SQL 必须参数化，禁止 ${} 拼接（MyBatis 里禁止用 ${} 拼用户输入）
- 密码不存明文，用 BCrypt/SM3 等单向散列；token 走标准鉴权中间件
- 响应里不回吐：堆栈、SQL、数据库字段名、内部异常类名
- 权限校验放拦截器/注解（@PreAuthorize 或自定义），不写在每个接口里手判
- 文件上传校验后缀 + 大小 + 类型，存到对象存储或指定目录，不直传 web 根目录

## 10. 代码风格
- 类名 PascalCase（UserController），方法/变量 camelCase（getUserInfo）
- 常量 UPPER_SNAKE_CASE（MAX_RETRY_COUNT），包名全小写
- 实现类以 Impl 结尾（UserServiceImpl），接口以 Service 结尾
- 方法不超过 50 行，单层嵌套不超 3 层，复杂分支拆私有方法
- 缩进 4 空格，不用 Tab；一行一句；禁止魔数，抽常量或枚举
- 业务枚举用 enum 承载 code+desc，不写散装数字判断

## 11. 注释与文档
- 类、public 方法必须 JavaDoc：类说职责，方法说用途/@param/@return/@throws
- 注释写“为什么”不写“做了什么”；坑点、临时方案、兼容逻辑必须注原因
- Controller 接口补 Knife4j 注解（@Operation @Parameter）
- 不写废话注释（// 设置名字 user.setName(name) 这种删掉）

## 12. 测试
- 单测用 JUnit 5 + Mockito，Service 层核心逻辑必须有测试
- 外部依赖（DB、Redis、第三方接口）用 mock，不真连
- 测试类命名 XxxTest，方法名 场景_预期结果（shouldReturnXxxWhenYyy）
- 跑通 mvn test / gradle test 再算改完

## 13. AI 生成代码时的约束（给 Trae 看）
- 先读项目已有代码：复用公共 Result、BaseEntity、BaseMapper、公共工具类、已有异常类，不重复造
- 不主动引入 package.json/pom.xml 里没有的依赖；要加先说明用途
- 新增接口必须：入参 DTO + @Valid + 统一响应 + 必要的日志 + 异常走全局处理器
- 不返回 Entity 给前端，不写字段注入，不写 System.out
- 改动范围最小化：只动对话里提到的接口/方法，不顺手重构无关代码
- 改完在回复里列出：新增/修改了哪些文件、影响了哪些接口、有没有加表或加依赖
- 涉及表结构变更，顺带给 Flyway/SQL 脚本，不只在 Java 里加字段