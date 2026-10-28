# 开发执行日志

> 记录每次环境搭建、依赖引入、配置变更等执行情况。
>
> ## 2026-10-14

### 引入 MyBatis-Plus / Spring AI / MySQL / Redis / Hutool / 参数校验

- **背景**：项目（Spring Boot 3.5.13 + JDK 17）初始化阶段，需要补齐持久层、AI 能力、缓存与工具类依赖。
- **依赖**：
  - `com.baomidou:mybatis-plus-spring-boot3-starter:3.5.17`（3.5.x 稳定线最新版，适配 Spring Boot 3.x）
  - `com.baomidou:mybatis-plus-jsqlparser:3.5.17`（3.5.9 起分页插件等 SQL 解析能力拆分为独立模块）
  - `org.springframework.ai:spring-ai-bom:1.1.8`（1.x 最新 GA，支持 Boot 3.5.x；2.0.x 起要求 Boot 4.0，故锁定 1.1.x）
  - `org.springframework.ai:spring-ai-starter-model-openai`（OpenAI 兼容协议，可对接 DeepSeek/通义等兼容端点，版本由 BOM 管理）
  - `com.mysql:mysql-connector-j`（runtime，版本由 Spring Boot BOM 管理）
  - `spring-boot-starter-data-redis`（默认 Lettuce，版本由 BOM 管理）
  - `spring-boot-starter-validation`（Jakarta Validation，版本由 BOM 管理）
  - `cn.hutool:hutool-all:5.8.48`（5.8.x 稳定线最新版，兼容 JDK 17）
- **改动文件**：`pom.xml`
- **改动内容**：
  1. `properties` 新增 `mybatis-plus.version=3.5.17`、`spring-ai.version=1.1.8`、`hutool.version=5.8.48`，并注释说明版本选型原因；
  2. `dependencies` 新增上述 8 项依赖；
  3. `dependencyManagement` 引入 `spring-ai-bom` 统一管理 Spring AI 模块版本。
- **状态**：已完成 pom 修改；`mvn dependency:resolve` 验证因沙箱限制未执行（JDK 需写 `C:\ProgramData\Oracle\Java\.oracle_jre_usage`，用户拒绝了沙箱外执行），待用户在本地自行执行 `mvn compile` 验证。

### 落地 DEV.MD 开发规范 1~5 项（DDD 结构 / Result / 状态码 / 全局异常 / MP 与 AI 配置）

- **背景**：按 `DEV.MD` 第 1~5 项要求，搭建 DDD 分层结构，统一接口返回格式与异常处理，配置 MyBatis-Plus 与 Spring AI 初始 Bean。
- **改动文件**：
  - 新增 `interfaces` / `application` / `domain` / `infrastructure` 四层 `package-info.java`（DDD 分层骨架，约定调用链 interfaces → application → domain → infrastructure）
  - 新增 `common/enums/ResultCode.java`：状态码枚举（200/400/401/403/404/500 业务失败/500 系统兜底）
  - 新增 `common/result/Result.java`：统一响应体 `{code, message, data, traceId}`，traceId 取自 MDC
  - 新增 `common/exception/BizException.java`：业务异常，携带业务码
  - 新增 `common/exception/GlobalExceptionHandler.java`：`@RestControllerAdvice` 集中处理 MethodArgumentNotValidException（400 字段级中文明细）、BindException、ConstraintViolationException、BizException、AuthenticationException(401)、AccessDeniedException(403)、兜底 Exception（500 固定文案 + ERROR 日志，不泄露堆栈）
  - 新增 `common/filter/TraceIdFilter.java`：每个请求生成 traceId（Hutool IdUtil）写入 MDC，请求结束清理
  - 新增 `config/MybatisPlusConfig.java`：`@MapperScan` 扫描 `infrastructure.**.mapper`，注册分页插件（PaginationInnerInterceptor, MySQL）
  - 新增 `config/AiConfig.java`：ChatClient Bean，预设心理咨询场景默认系统提示词
  - 修改 `src/main/resources/application.yml`：新增应用名、MySQL 数据源（默认连 `192.168.136.128:3306/ai-agent`，环境变量可覆盖）、Redis、Spring AI OpenAI 兼容端点（密钥走 `OPENAI_API_KEY` 环境变量，默认占位 key 仅保证启动）、MP 驼峰映射与 SQL 日志、日志格式带 traceId
- **改动内容**：见上；未新增依赖、未改表结构；表结构无 `deleted` 字段，故 MP 未配置逻辑删除。
- **状态**：已完成；编译验证仍受沙箱限制（同上 JDK 写入限制），待用户本地执行 `mvn compile` / 启动应用验证。注意：Spring Security 仍为默认配置（admin/123456），所有接口需登录访问，后续按需补 SecurityConfig 放行策略。

### 落地 DEV.MD 第 1~2 项（表统一审计字段 / MP 逻辑删除与字段填充）

- **背景**：按 `DEV.MD` 要求，9 张表统一包含 created_at、updated_at、created_by、updated_by、deleted_flag 五个审计字段，并重新配置 MyBatis-Plus 逻辑删除与公共字段自动填充（created_by/updated_by 暂默认 1，登录接入后再改）。
- **改动文件**：
  - 修改 `docs/ai-agent.sql`：9 张表统一补齐审计字段（`created_at`/`updated_at` datetime 默认当前时间、`created_by`/`updated_by` bigint、`deleted_flag` tinyint 默认 0）；其中 appointment、consultation_session 新增全部 5 个字段，consultation_message、sys_file_info 补 updated_at 等 4 个字段，其余 5 张表补 created_by/updated_by/deleted_flag
  - 新增 `common/entity/BaseEntity.java`：公共实体基类，5 个审计字段 + `@TableField(fill=...)` 注解；主键 id 不入基类（knowledge_article 为 UUID 字符串主键）
  - 新增 `config/MybatisPlusMetaObjectHandler.java`：`MetaObjectHandler` 实现，插入填充 createdAt/updatedAt/createdBy/updatedBy，更新填充 updatedAt/updatedBy；操作人暂以常量 `DEFAULT_OPERATOR_ID = 1L` 占位
  - 修改 `src/main/resources/application.yml`：`mybatis-plus.global-config.db-config` 新增逻辑删除配置（`logic-delete-field: deletedFlag`，1 删 0 未删），故实体无需 `@TableLogic`
- **改动内容**：见上；未新增依赖。表结构变更涉及数据库脚本（docs/ai-agent.sql 为 DROP+CREATE 全量重建脚本），如需保留存量数据请改用 ALTER TABLE 逐表加列。
- **状态**：已完成，待本地编译与启动验证。

### 审计时间字段改名 created_at→create_time / updated_at→update_time

- **背景**：DEV.MD 明确「遇到 created_at 替换为 create_time，遇到 updated_at 替换为 update_time」，统一审计字段命名。
- **改动文件**：
  - 修改 `docs/ai-agent.sql`：全量替换 `` `created_at` ``→`` `create_time` ``、`` `updated_at` ``→`` `update_time` ``（含列定义与索引列引用；索引名 idx_created_at 等保留不动）；业务时间字段 `started_at`/`completed_at`/`ai_analysis_updated_at`/`last_emotion_updated_at` 非审计字段，未动；sys_file_info（原'上传时间'）、user_favorite（原'收藏时间'）注释统一为'创建时间'
  - 修改 `common/entity/BaseEntity.java`：字段 `createdAt`→`createTime`、`updatedAt`→`updateTime`（驼峰映射 create_time/update_time）
  - 修改 `config/MybatisPlusMetaObjectHandler.java`：填充键名同步改为 `createTime`/`updateTime`
- **改动内容**：纯改名，无依赖、无行为变化；逻辑删除（deleted_flag）与填充默认 1 的配置不变。
- **状态**：已完成。

### 落地 DEV.MD 任务 1~3（Spring Security 配置 / 登录接口 / 注册接口）

- **背景**：按 `DEV.MD` 任务清单实现 Spring Security 认证授权与会话管理，并提供登录、注册两个 RESTful 接口；遵循 DDD 分层、统一 Result 返回、JSON 出入参、接口注释与测试用例要求。
- **依赖**：未新增依赖。JWT 直接使用已在 pom 中的 `hutool-all:5.8.48` 的 `cn.hutool.jwt` 模块签发与校验，避免额外引入 jjwt。
- **改动文件（新增）**：
  - `domain/user/model/User.java`：用户领域模型
  - `domain/user/repository/UserRepository.java`：仓储接口（按用户名查、三类唯一性判断、保存）
  - `application/user/command/RegisterUserCommand.java`：注册命令对象，隔离接口层 DTO
  - `application/user/service/UserService.java`、`application/user/service/impl/UserServiceImpl.java`：注册用例（唯一性业务校验 + BCrypt 加密 + `@Transactional`）、按用户名查询
  - `infrastructure/persistence/po/UserPO.java`：`user` 表持久化对象（继承 BaseEntity，主键 `IdType.AUTO`）
  - `infrastructure/persistence/mapper/UserMapper.java`：`BaseMapper<UserPO>`，`@Mapper`
  - `infrastructure/persistence/repository/UserRepositoryImpl.java`：仓储实现，领域对象↔持久化对象互转
  - `infrastructure/security/LoginUser.java`：`UserDetails` 实现，按 userType 映射 ROLE_USER/ROLE_ADMIN
  - `config/JwtProperties.java`：`jwt.*` 配置绑定（secret / expireSeconds / header / prefix）
  - `infrastructure/security/JwtTokenProvider.java`：令牌签发与校验（签名、过期均校验）
  - `infrastructure/security/JwtAuthenticationFilter.java`：`OncePerRequestFilter`，解析 `Authorization: Bearer` 并写入 SecurityContext
  - `infrastructure/security/UserDetailsServiceImpl.java`：按用户名加载用户供 AuthenticationManager 校验
  - `infrastructure/security/JsonSecurityExceptionHandler.java`：未认证 401 / 无权限 403，输出统一 Result JSON
  - `config/SecurityConfig.java`：`@EnableWebSecurity` + `@EnableMethodSecurity`，无状态（`SessionCreationPolicy.STATELESS`），关闭 CSRF/表单登录/HTTP Basic，白名单 `/api/auth/login`、`/api/auth/register`、`/error`，其余 `authenticated()`，注册 BCryptPasswordEncoder 与 AuthenticationManager Bean
  - `interfaces/dto/request/LoginRequest.java`、`RegisterRequest.java`：入参 DTO，Jakarta 校验注解 + 中文业务提示
  - `interfaces/dto/response/LoginResponse.java`、`UserResponse.java`：出参 DTO（不含密码字段）
  - `interfaces/controller/AuthController.java`：`POST /api/auth/login`（返回 token + 用户信息）、`POST /api/auth/register`（返回用户信息），方法级 JavaDoc 标注路径/参数/返回值
  - `src/test/java/com/zzg/psychologyai/interfaces/controller/AuthControllerTest.java`：standalone MockMvc 测试，覆盖登录成功/凭证错误/参数校验、注册成功/重名/手机号非法 6 个用例
- **改动文件（修改）**：
  - `src/main/resources/application.yml`：移除 `spring.security.user` 默认内存用户（改为数据库用户认证）；新增 `jwt` 配置块（secret 走 `JWT_SECRET` 环境变量）
- **改动内容**：见上；未新增依赖、未改表结构（沿用 `docs/ai-agent.sql` 的 `user` 表）。
- **状态**：已完成；按 DEV.MD「任务执行完成后不编译项目」要求，未执行 `mvn compile` / 测试。待本地自行验证：需先在库中执行 `docs/ai-agent.sql`，并可通过注册接口写入首个用户（userType 默认 1），管理员需手动把 `user_type` 改为 2。

### 落地 DEV.MD 任务（加入 UserMapper.xml）

- **背景**：按 `DEV.MD` 任务要求补入 `UserMapper.xml`，使自定义 SQL 走 XML 而非常规在 Java 中拼接字符串。
- **依赖**：无新增。
- **改动文件**：
  - 新增 `src/main/resources/mapper/UserMapper.xml`：namespace 绑定 `com.zzg.psychologyai.infrastructure.persistence.mapper.UserMapper`；含 `BaseResultMap`（`user` 表全部 16 列 → `UserPO` 字段，含 BaseEntity 审计字段）与 `Base_Column_List` 列清单，供后续自定义 SQL 复用。当前无自定义语句（常规 CRUD 由 MyBatis-Plus `BaseMapper` 提供），文件为承载后续 SQL 而预留。
  - 修改 `src/main/resources/application.yml`：`mybatis-plus` 下新增 `mapper-locations: 'classpath*:mapper/**/*.xml'`，与 `resources/mapper` 目录对应（MyBatis-Plus 默认值同为该路径，此处显式声明以便检索）。
- **改动内容**：见上；未改 Java 代码、未改表结构。
- **状态**：已完成；按 DEV.MD「任务执行完成后不编译项目」要求，未执行编译。

### 落地 DEV.MD 任务（security 白名单外置到 application.yml）

- **背景**：按 `DEV.MD` 任务要求，把 Spring Security 免认证白名单从 Java 常量移到配置文件中，便于后续新增免登录接口时无需改动代码。
- **依赖**：无新增。
- **改动文件**：
  - 新增 `config/SecurityProperties.java`：`@ConfigurationProperties(prefix = "security")`，字段 `whitelist`（`List<String>`，支持 Ant 风格路径），并给出与 yml 一致的默认值兜底
  - 修改 `config/SecurityConfig.java`：删除常量 `WHITE_LIST`，改为注入 `SecurityProperties`；授权规则中动态读取 `security.whitelist`（空/为 null 时跳过，不影响 `anyRequest().authenticated()`）
  - 修改 `src/main/resources/application.yml`：新增根级 `security.whitelist`（`/api/auth/login`、`/api/auth/register`、`/error`），并注明与 `spring.security.*` 的区别
- **改动内容**：见上；白名单值与原常量保持一致，行为不变。
- **状态**：已完成；按 DEV.MD「任务执行完成后不编译项目」要求，未执行编译。

### 落地 DEV.MD 任务（对象转换层加入对象转换器）

- **背景**：按 `DEV.MD` 强制要求第 1 条与任务清单第 1 项，新增对象转换层并落地对象转换器，用于领域对象与应用对象的相互转换；同时把原先散落在控制器、DTO 静态工厂、应用服务里的手工映射统一收敛，避免各层对象模型直接互相依赖。
- **依赖**：无新增。
- **改动文件（新增）**：
  - `converter/package-info.java`：对象转换层职责说明（承载领域/应用/接口对象互转，禁止领域对象或数据库实体暴露到接口层）
  - `converter/UserConverter.java`：`@Component` 用户对象转换器，提供 `toCommand(RegisterRequest)`（接口 DTO→应用命令，空白串收敛为 null）、`toDomain(RegisterUserCommand)`（应用命令→领域对象，取默认类型/状态/性别，密码由应用层加密后回填）、`toResponse(User)`（领域对象→响应 DTO，剔除密码等敏感字段）三个方法
- **改动文件（修改）**：
  - `application/user/service/impl/UserServiceImpl.java`：注入 `UserConverter`，移除内联构建领域对象与 `USER_TYPE_NORMAL`/`USER_STATUS_ENABLED`/`USER_GENDER_UNKNOWN` 三个常量（迁入转换器），改为 `userConverter.toDomain(command)` + `passwordEncoder.encode(...)` 回填密码
  - `interfaces/controller/AuthController.java`：注入 `UserConverter`，删除私有 `toCommand` 方法及 `StrUtil`/`RegisterUserCommand` 导入；登录改传 `userConverter.toResponse(user)`，注册改为 `userService.register(userConverter.toCommand(request))` 与 `userConverter.toResponse(user)`，控制器不再写映射逻辑
  - `interfaces/dto/response/UserResponse.java`：删除静态工厂 `from(User)` 及对领域层 `User` 的导入，DTO 不再依赖领域层
  - `interfaces/dto/response/LoginResponse.java`：`of(...)` 第三参由领域 `User` 改为 `UserResponse`，删除领域层导入
  - `src/test/java/com/zzg/psychologyai/interfaces/controller/AuthControllerTest.java`：`AuthController` 构造器补 `new UserConverter()` 参数并新增导入（使用真实转换器，不影响既有 6 个用例断言）
- **改动内容**：见上；未新增依赖、未改表结构、未改接口路径与出入参 JSON 结构（响应字段不变）。
- **状态**：已完成；按 DEV.MD「任务执行完成后不编译项目」要求，未执行 `mvn compile` / 测试，待本地验证。

### 落地 DEV.MD 任务 1~3（DDD 四层职责与解耦 / 引入 MapStruct / 框架集成统一到基础设施层）

- **背景**：按 DEV.MD 强制要求第 1 条与任务清单 1~3：四层架构职责清晰并解耦、引入 MapStruct 替换手写转换、MyBatis-Plus/Spring Security/Spring AI 集成统一到 infrastructure。
- **依赖**：新增 MapStruct 1.6.3（编译期转换框架，适配 JDK 17 / Boot 3.5.x）；显式声明 Lombok 1.18.38 与 `lombok-mapstruct-binding:0.2.0` 以保证注解处理器顺序；`maven-compiler-plugin` 新增 `annotationProcessorPaths`（lombok → lombok-mapstruct-binding → mapstruct-processor → spring-boot-configuration-processor）。其余依赖无新增。
- **改动文件（任务 3，框架集成统一到 infrastructure，迁移 `config` 包并按框架归类）**：
  - 新增 `infrastructure/persistence/config/MybatisPlusConfig.java`、`MybatisPlusMetaObjectHandler.java`（原 `config` 包）
  - 新增 `infrastructure/security/config/SecurityConfig.java`、`SecurityProperties.java`、`JwtProperties.java`（原 `config` 包）
  - 新增 `infrastructure/ai/config/AiConfig.java`（原 `config` 包）
  - 新增 `infrastructure/persistence/entity/BaseEntity.java`（原 `common/entity`，MyBatis-Plus 持久化基类归入基础设施层）
  - 修改 `infrastructure/persistence/po/UserPO.java`、`infrastructure/security/JwtAuthenticationFilter.java`、`JwtTokenProvider.java` 的 import
  - 删除旧 `com.zzg.psychologyai.config` 包 6 个文件、`common/entity/BaseEntity.java`
- **改动文件（任务 2，MapStruct 替换手写转换）**：
  - 新增 `infrastructure/persistence/converter/UserPersistenceConverter.java`：PO ↔ 领域对象（替换 `UserRepositoryImpl` 内私有手写 toDomain/toPO）
  - 新增 `application/user/assembler/UserAssembler.java`：注册命令 → 领域对象（默认类型/状态/性别、昵称缺省取用户名）、领域对象 → `UserDTO`
  - 新增 `interfaces/converter/UserWebConverter.java`：注册请求 → 命令（空白串收敛 null）、登录请求 → 命令、`UserDTO` → 响应 DTO
  - 修改 `infrastructure/persistence/repository/UserRepositoryImpl.java`：注入转换器替换手写映射
  - 删除手写转换器 `converter/UserConverter.java` 与 `converter/package-info.java`
- **改动文件（任务 1，四层职责与解耦）**：
  - 移除顶层 `converter` 包：接口映射归入 `interfaces/converter`，应用映射归入 `application/.../assembler`，持久化映射归入 `infrastructure/persistence/converter`
  - 应用层去除对 Spring Security 直接依赖：新增领域端口 `domain/user/service/PasswordEncoder.java`，`UserServiceImpl` 改注入该端口；新增 `infrastructure/security/password/BCryptPasswordEncoderAdapter.java` 实现
  - 接口层去除对基础设施层直接依赖：新增认证应用服务 `application/auth/service/AuthService(Impl)`，及端口 `application/auth/port/{CredentialVerifier,TokenProvider,AuthenticatedUser}`、`application/auth/command/LoginCommand`、`application/auth/dto/LoginResult`；基础设施层新增 `infrastructure/security/SpringSecurityCredentialVerifier.java`（Spring Security 实现凭证校验），`JwtTokenProvider` 改为实现 `TokenProvider` 端口
  - 重写 `interfaces/controller/UserController.java`：仅依赖 `UserService`、`AuthService` 与 `UserWebConverter`
  - `interfaces/dto/response/UserResponse.java` 去除领域层依赖；`UserService` 出参由领域 `User` 改为应用层 `UserDTO`（新增 `application/user/dto/UserDTO.java`），彻底隔断接口层 → 领域层
  - Web 相关通用类归位：`interfaces/handler/GlobalExceptionHandler.java`、`interfaces/filter/TraceIdFilter.java`（原 `common` 包）；新增 `common/constant/TraceIdConstants.java` 解除 `Result` 对过滤器的依赖
  - 修正 `SecurityProperties` 默认白名单为 `/api/user/login`、`/api/user/register`、`/error`、`/`（与 yml 一致）
  - 测试：`AuthControllerTest` 重命名为 `UserControllerTest`，接口路径改 `/api/user/*`，依赖改为 mock 应用服务 + MapStruct 真实转换器（`Mappers.getMapper`）
- **改动内容**：见上；未改数据库表结构；接口路径与出入参 JSON 字段保持不变（登录响应字段仍为 token/tokenType/expireSeconds/userInfo）。
- **状态**：已完成；按 DEV.MD「任务执行完成后不编译项目」要求，未执行 `mvn compile` / 测试。**待本地验证**：首次需联网拉取 MapStruct 与注解处理器依赖后执行 `mvn compile`；若编译报 MapStruct 映射不完整告警，可检查 Lombok 注解处理器顺序。

### 落地 DEV.MD 任务 1（领域值对象 / 领域事件 / 领域工厂 / 事件驱动）

- **背景**：按 `DEV.MD` 任务 1「缺失值对象，领域事件，领域工厂等，事件如登录成功回去风控系统验证；注册成功发送欢迎邮件、初始积分等」，补齐 DDD 战术构件并以事件驱动实现跨聚合协作。
- **依赖**：无新增（Hutool `IdUtil` 用于值对象/事件，MapStruct 已在 pom）。
- **改动文件（新增）**：
  - 值对象：`domain/user/valueobject/Username.java`、`Email.java`、`Phone.java`、`Password.java`（承载密文，`toString` 脱敏）、`Nickname.java`、`Points.java`（初始 100）；均为不可变、构造即校验，非法抛 `BizException`
  - 值对象转换辅助：`domain/user/valueobject/UserValueObjectMapper.java`（供 MapStruct `uses` 完成 String ↔ 值对象双向转换）
  - 领域工厂：`domain/user/factory/UserFactory.java`（聚合创建规则、默认用户类型/状态/性别、昵称缺省取用户名）
  - 领域事件：`domain/common/event/DomainEvent.java`（标记接口）、`domain/user/event/UserRegisteredEvent.java`、`domain/user/event/UserLoggedInEvent.java`
  - 事件发布端口/适配器：`application/common/port/DomainEventPublisher.java`（端口）、`infrastructure/event/SpringDomainEventPublisher.java`（Spring `ApplicationEventPublisher` 实现）
  - 事件监听：`infrastructure/event/listener/UserRegisteredListener.java`（欢迎邮件 + 初始积分，日志占位并留 TODO）、`UserLoggedInListener.java`（风控校验，日志占位并留 TODO）；均用 `@TransactionalEventListener(phase = AFTER_COMMIT, fallbackExecution = true)`
  - 共享分页：`common/result/PageResult.java`（统一 {list,total,pageNum,pageSize}，含 `of`/`map`）、`domain/common/model/PageData.java`（领域分页，避免领域层依赖 MyBatis-Plus `Page`）
- **改动文件（修改）**：
  - `domain/user/model/User.java`：关键字段改为值对象（username/email/phone/password/nickname），保留 avatar/gender/birthday/userType/status
  - `application/user/assembler/UserAssembler.java`、`infrastructure/persistence/converter/UserPersistenceConverter.java`：`uses = UserValueObjectMapper.class` 完成值对象映射；`UserAssembler` 新增 `toDomain(UpdateUserCommand)`
  - `infrastructure/security/UserDetailsServiceImpl.java`：取用户名/密码改为 `.value()`
  - `application/user/service/impl/UserServiceImpl.java`、`application/auth/service/impl/AuthServiceImpl.java`：注册走 `UserFactory.create` 并发布 `UserRegisteredEvent`；登录成功发布 `UserLoggedInEvent`
- **改动内容**：见上；**未新增 `outbox_event` 表、未改动 `docs/ai-agent.sql`**（跨聚合协作以事务提交后事件监听实现，风控/邮件/积分为日志占位）。
- **状态**：已完成。

### 落地 DEV.MD 任务 2（按表结构生成全部 CRUD 接口）+ 编译与测试

- **背景**：按 `DEV.MD` 任务 2「根据表结构生成所有的 crud 接口」与强制要求（RESTful `/api/{entity-name}`、每个接口对应测试用例、JSON 出入参、必要注释），为 `docs/ai-agent.sql` 全部 9 张表生成 CRUD。
- **依赖（新增/修改）**：
  - `maven-surefire-plugin:3.5.2`（本项目导入 BOM 而非继承 `spring-boot-starter-parent`，Maven 默认 surefire 2.12.4 仅支持 JUnit 3，无法发现 JUnit 5 用例）
  - `maven-compiler-plugin` 新增 `<parameters>true</parameters>`（Spring MVC `@PathVariable`/`@RequestParam` 需保留参数名做绑定）
- **改动文件**：
  - 模板与示例：`appointment` 全栈 19 文件（domain/application/infrastructure/interfaces + XML + 测试），作为其余实体统一范式
  - 批量生成 7 张表（各 19 文件）：`analysis/ai_analysis_task`、`diary/emotion_diary`、`consultation_message`、`consultation_session`、`knowledge_article`（String UUID 主键，`IdType.INPUT`）、`sys_file_info`、`user_favorite`
  - `user` 表补充 CRUD：`domain/user/repository/UserRepository.java`、`UserRepositoryImpl.java` 新增 `findById/findPage/updateById/removeById`；新增 `application/user/command/UpdateUserCommand.java`、`interfaces/dto/request/UserUpdateRequest.java`；`UserService(+Impl)` 新增 `getById/page/update/delete`；`UserWebConverter` 新增 `toUpdateCommand`/`toResponseList`；`UserController` 新增 `GET /api/user`（分页）、`GET /api/user/{id}`、`POST /api/user`、`PUT /api/user/{id}`、`DELETE /api/user/{id}`
  - `interfaces/dto/request/UserUpdateRequest.java` 校验：email/phone/nickname/avatar/gender/birthday/userType/status，均可选，中文提示
  - `common/result/PageResult.java`：`map` 泛型写法修正为 `stream().<R>map(mapper)`（消除 `? extends R` 捕获不兼容）
  - `interfaces/converter/UserWebConverter.java`：多参数映射源改为 `request.email` 等限定名（MapStruct 多参数方法要求）
  - 测试：`src/test/.../UserControllerTest.java` 新增分页/详情/详情 404/新增/更新/更新校验/删除用例（共 13 个）；`ConsultationSessionControllerTest` 时间入参由 `2026-10-14 09:00:00` 改为 ISO-8601 `2026-10-14T09:00:00`
- **改动内容**：9 张表统一 5 端点（分页 GET、详情 GET/{id}、新增 POST、更新 PUT/{id}、删除 DELETE/{id}）；服务读写事务分明、不存在抛 `BizException(ResultCode.NOT_FOUND, "…不存在")`；转换器统一 MapStruct（`componentModel=spring`、`ReportingPolicy.IGNORE`）；每接口均有 standalone MockMvc + Mockito 测试。
- **状态**：已完成并验证。`mvn test -Dtest=*ControllerTest`：**Tests run: 69, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS**（编译环境需 `JAVA_HOME` 指向 JDK 17，如 `C:\Users\needletubing\.jdks\corretto-17.0.20.1`；系统默认 JAVA_HOME 为 1.8，直接 `mvn` 会报「无效的目标发行版: 17」）。说明：`PsychologyAiApplicationTests`（`@SpringBootTest`）需数据库/Redis，未纳入本次运行。
### 修复默认环境下编译失败（无效的目标发行版: 17）

- **背景**：系统默认 `JAVA_HOME` 为 JDK 1.8（`D:\DevEnvironment\java\jdk1.8.0_151`），直接执行 `mvn compile` 报「无效的目标发行版: 17」，需在不改动全局环境变量的前提下让项目自动使用 JDK 17。
- **依赖（新增）**：`maven-toolchains-plugin:3.2.0`（当前稳定版，兼容 Maven 3.2.5+ 及本项目 compiler 3.8.1 / surefire 3.5.2）。
- **改动文件**：
  - 新增 `~/.m2/toolchains.xml`（用户级 Maven 配置）：声明 JDK 17 toolchain，指向 `C:\Users\needletubing\.jdks\corretto-17.0.20.1`
  - `pom.xml`：新增 `maven-toolchains-plugin` 并声明 `<jdk><version>17</version></jdk>`
- **改动内容**：编译与测试阶段自动匹配 toolchains.xml 中的 JDK 17（日志显示 `Toolchain in maven-compiler-plugin / maven-surefire-plugin: JDK[corretto-17.0.20.1]`），无需再手动设置 `JAVA_HOME`，也不影响其他 JDK 8 项目。
- **状态**：已完成并验证。默认环境（JDK 1.8）下 `mvn compile` BUILD SUCCESS；`mvn test -Dtest=*ControllerTest`：Tests run: 69, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS。

### 修复应用启动失败（UserValueObjectMapper Bean 缺失）

- **背景**：应用启动报错 `Field userValueObjectMapper in UserPersistenceConverterImpl required a bean of type 'UserValueObjectMapper' that could not be found`。原因：`UserAssembler`/`UserPersistenceConverter` 以 `componentModel="spring"` + `uses=UserValueObjectMapper.class` 生成实现，MapStruct 对非 static 方法的 uses 类会生成构造器注入，而该类是普通类未注册为 Spring Bean。
- **依赖**：无新增。
- **改动文件**：`domain/user/valueobject/UserValueObjectMapper.java`。
- **改动内容**：类改为 `final` + 私有构造，全部 10 个转换方法改为 `static`。MapStruct 生成实现改为静态调用（如 `UserValueObjectMapper.toUsername(...)`），不再生成注入字段，领域层也无需引入 Spring 注解。
- **状态**：已完成并验证。`mvn compile` BUILD SUCCESS，生成的 `UserPersistenceConverterImpl`/`UserAssemblerImpl` 无注入字段；`mvn test -Dtest=*ControllerTest`：Tests run: 69, Failures: 0, Errors: 0 — BUILD SUCCESS。注：toolchains.xml 已被移除，编译前需将 `JAVA_HOME` 指向 JDK 17。

### 落地 DEV.MD 任务：移除 request 包，command 作为请求参数

- **背景**：按 `DEV.MD` 新任务「将request包移除，command包作为请求参数」，接口层不再定义 Request DTO，控制器直接以应用层 Command 作为 `@Valid @RequestBody` 入参，校验注解随迁到 Command 上。
- **依赖**：无新增。
- **改动文件**：
  - **校验注解迁移**（jakarta 注解 + 中文 message 逐字保留）：`LoginCommand`、`RegisterUserCommand`、`UpdateUserCommand`、`Create/UpdateAppointmentCommand`、`Create/UpdateAiAnalysisTaskCommand`、`Create/UpdateEmotionDiaryCommand`、`Create/UpdateConsultationMessageCommand`、`Create/UpdateConsultationSessionCommand`、`Create/UpdateKnowledgeArticleCommand`、`Create/UpdateSysFileInfoCommand`、`Create/UpdateUserFavoriteCommand`（共 19 个 Command）
  - **控制器直收 Command**（9 个）：`UserController`、`AppointmentController`、`AiAnalysisTaskController`、`EmotionDiaryController`、`ConsultationMessageController`、`ConsultationSessionController`、`KnowledgeArticleController`、`SysFileInfoController`、`UserFavoriteController`；update 接口主键由路径参数回填（`command.setId(id)`），防止请求体伪造
  - **WebConverter 精简**（9 个）：删除 toCommand/toUpdateCommand/toLoginCommand 与 @Mapping，仅保留 toResponse/toResponseList
  - **删除**：`interfaces/dto/request/` 全部 19 个 Request DTO，目录已清空
  - **兜底调整**：`domain/user/valueobject/UserValueObjectMapper.java` 的 `toNickname` 空白串收敛为 null（替代原 `blankToNull` 行为，更新链路空值字段由 MyBatis-Plus 空值跳过策略忽略）
- **改动内容**：请求链路从「Request DTO → WebConverter → Command」简化为「Command（请求体）直入应用服务」；接口出入参仍为 JSON，校验消息不变；测试用 JSON 字符串构造请求体，无需改动。
- **状态**：已完成并验证。全 src 无 `dto.request`/`toCommand` 残留；`mvn test -Dtest=*ControllerTest`：Tests run: 69, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS（编译前需将 `JAVA_HOME` 指向 JDK 17）。

### 落地 DEV.MD 任务：只保留 user 接口功能，其余模块全部删除

- **背景**：按 `DEV.MD` 新任务「留下user接口功能其他全部删除，我要调整包结构」，删除除 user/auth 外的全部业务模块，精简为单模块 user 项目，便于后续包结构调整。
- **依赖**：无变动。
- **删除的模块**（7 个，每层完整删除）：
  - `domain/`：appointment、analysis、diary、consultation、knowledge、file、favorite（16 文件）
  - `application/`：同上 7 模块的 command/dto/assembler/service/service.impl（48 文件）
  - `interfaces/`：8 个 Controller、8 个 WebConverter、8 个 Response DTO（24 文件）
  - `infrastructure/persistence/`：8 个 PO、8 个 Mapper、8 个 PersistenceConverter、8 个 RepositoryImpl（32 文件）
  - `resources/mapper/`：8 个 XML（Appointment/AiAnalysisTask/EmotionDiary/ConsultationMessage/ConsultationSession/KnowledgeArticle/SysFileInfo/UserFavorite）
  - `src/test/`：8 个 Controller 测试（Appointment/AiAnalysisTask/EmotionDiary/ConsultationMessage/ConsultationSession/KnowledgeArticle/SysFileInfo/UserFavorite）
  - 空目录清理：domain/application 下 7 个模块子目录、interfaces/dto/request（已空）
- **保留**：`user`（domain+application+interfaces+infrastructure）、`auth`（command/dto/service/impl）、`common`（result/enums/exception/constant）、`infrastructure`（security/persistence 配置/entity/event）、`domain/common`（PageData/DomainEvent）、`UserMapper.xml`、`UserControllerTest`。
- **改动内容**：仅删除文件与空目录，未修改任何保留文件；当前项目仅含 user 模块完整 CRUD + 登录注册 + JWT 安全 + 全局异常 + 基础设施。
- **状态**：已完成并验证。`mvn test -Dtest=UserControllerTest`：Tests run: 13, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS（编译前需将 `JAVA_HOME` 指向 JDK 17）。

## 2026-10-16

### 落地 DEV.MD 任务：生成 xml / 实体 PO / Mapper，按表命名

- **背景**：按 `DEV.MD` 任务「生成xml，实体po，maper,按表命名」，为 `docs/ai-agent.sql` 全部 9 张表补齐「PO + Mapper + XML」三件套，且 Mapper 一律按**表名**命名（此前存在按领域命名的 Mapper，与规范冲突，按「不符合规范的文件可适当调整」统一改名）。
- **依赖**：无新增。
- **改动文件（新增 PO 3 个）**：`infrastructure/persistence/po/AiAnalysisTaskPO.java`（ai_analysis_task）、`AppointmentPO.java`（appointment）、`UserFavoritePO.java`（user_favorite）；均继承 `BaseEntity`，主键 `IdType.AUTO`，表列与字段一一对应。
- **改动文件（新增 Mapper 6 个，按表命名）**：`ConsultationMessageMapper`（含自定义 `selectBySessionId`）、`ConsultationSessionMapper`、`KnowledgeArticleMapper`、`AiAnalysisTaskMapper`、`AppointmentMapper`、`UserFavoriteMapper`；均 `extends BaseMapper<对应PO>`。
- **改动文件（删除 Mapper 3 个）**：原领域命名的 `ChatMessageMapper`、`ChatSessionMapper`、`ArticleMapper`，由表命名 Mapper 取代。
- **改动文件（新增 XML 8 个）**：`resources/mapper/` 下 `AiAnalysisTaskMapper.xml`、`AppointmentMapper.xml`（`date`/`time` 列加反引号）、`ConsultationMessageMapper.xml`（含 `selectBySessionId`）、`ConsultationSessionMapper.xml`、`EmotionDiaryMapper.xml`、`KnowledgeArticleMapper.xml`、`SysFileInfoMapper.xml`、`UserFavoriteMapper.xml`；统一结构 `BaseResultMap`（含审计列）+ `<sql id="Base_Column_List">`。
- **改动文件（修改）**：
  - 因 Mapper 改名同步修正 4 处引用：`ChatSessionRepositoryImpl`（`ConsultationSessionMapper`/`ConsultationMessageMapper`）、`ArticleRepositoryImpl`（`KnowledgeArticleMapper`）、`ArticleQueryServiceImpl`（`KnowledgeArticleMapper`）、`ChatSessionController`（`ConsultationSessionMapper`）。
  - `UserMapper.xml`：将 `select ${Base_Column_List} from ...` 修正为 `<include refid="Base_Column_List"/>`（`${}` 为属性替换，运行期会报无该属性），并补 `and deleted_flag = 0`。
  - `EmotionDiaryPO`：补齐缺失的 `aiEmotionAnalysis`（String，JSON）、`aiAnalysisUpdatedAt`（LocalDateTime）两字段，与 `emotion_diary` 表及 `EmotionDiaryMapper.xml` 的 resultMap 对齐（否则运行期 resultMap 会因找不到 PO 属性而报错）。
- **改动内容**：9 张表三件套命名与表名完全对齐（PO 用 `XxxPO`、Mapper 接口用 `XxxMapper`、XML 文件用 `XxxMapper.xml`）；常规 CRUD 由 MyBatis-Plus `BaseMapper` 提供，XML 承载自定义 SQL；未新增依赖、未改表结构。
- **状态**：已完成并验证。`mvn test-compile` BUILD SUCCESS；`mvn test -Dtest=UserControllerTest,ArticleTest,ArticleStatusEnumTest`：Tests run: 13, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS（编译前需将 `JAVA_HOME` 指向 JDK 17）。`PsychologyAiApplicationTests`/`RedisTest` 需数据库/Redis，未纳入本次运行。

## 2026-10-17

### 落地 DEV.MD 任务：排查并处理 /psychological-chat/stream 流式接口潜在问题

- **背景**：任务要求查看 `POST /psychological-chat/stream`（SSE 流式 AI 对话）的潜在问题并处理，**不调整请求/响应结构**（入参仍为 `{sessionId:Long, initialMessage:String}`，SSE 事件仍为 message/done/error，data 为 Result JSON）。
- **依赖**：无新增。
- **发现并修复的问题**：
  1. **接口完全不可用（会话ID解析必失败）**：入参 `sessionId` 是 `Long`，Controller `toString()` 得 `"123"`，而应用服务 `extractSessionId` 只认 `"session_"` 前缀，永远返回 null → 每次报「会话ID格式错误」。服务方法改为直接接收 `Long sessionId`，删除前缀解析。
  2. **追问的用户消息从不落库**：判空条件写反（`po == null && po.getXxx()` 恒 false）且字符串用 `==` 比较；改为仓储查询最新消息后 `senderType=1 && userMessage.equals(content)` 判断初始消息是否已由 start 落库。
  3. **AI 回复被错存为用户消息**：原 `saveUserMessage(answer,"open-ai")` 硬编码 sender_type=1，模型名错写入 emotion_tag；仓储新增 `saveAssistantMessage`（sender_type=2、ai_model 写模型名）。
  4. **多轮记忆只写不读**：Prompt 只放了系统提示词，从未 `chatMemory.get`；且 `AiConfig` 的 ChatClient 未注册任何 advisor，`CONVERSATION_ID` 参数无人消费。改为手动取回历史记忆拼入 Prompt（SystemMessage + 历史 + 当前 UserMessage），用户/AI 消息仍手动登记。
  5. **`@Transactional` 对返回 Flux 的方法无效**：方法返回即提交，落库发生在 Reactor 回调线程、事务外；移除该注解（单条 insert 自动提交），并加注释说明。
  6. **客户端断连不取消上游**：内部 `subscribe()` 的 Disposable 未管理；现捕获 Disposable 并注册 `sink.onCancel/onDispose` 取消上游 AI 请求，避免空转与向已取消 sink 发射。
  7. **doOnComplete 落库异常会导致挂起**：try/catch 兜底记日志，finally 中保证 `sink.complete()`。
  8. **响应式异常不走全局异常处理**：Controller 增加 `onErrorResume`，业务异常透出 code/文案、其余异常统一 `ResultCode.ERROR`，均转 error SSE 事件。
  9. **参数校验失效**：`ChatStreamRequest.sessionId`（Long）误用 `@NotBlank`（仅支持字符串，触发即 UnexpectedTypeException），改 `@NotNull`；Controller 补 `@Valid`。
  10. **越权（IDOR）**：流式调用不校验会话归属；现校验会话存在（404）与 `session.userId == 当前用户`（403）。userId 在 Controller 的 Web 请求线程取好后显式传入（Reactor 回调线程读不到 SecurityContext ThreadLocal）。
  11. **连带修复 start()**：`session.setId(userId)` 误把用户ID写进主键，改 `setUserId(userId)`，并让传入 title 在非空时生效（原代码始终用生成标题）。
  12. 清理无用 import（`javax.print.attribute.standard.Media`、`McpSchema`、`java.sql.Wrapper` 等）；应用层不再直连 Mapper/PO，最新消息查询改走领域仓储端口；修正「延迟5秒」注释（实为每片 50ms）。
- **改动文件**：
  - 修改：`interfaces/request/ChatSession.java`、`interfaces/controller/ChatSessionController.java`、`application/ConsultationMessageAppService.java`、`domain/repository/ConsultationMessageRepository.java`、`infrastructure/persistence/repository/ConsultationMessageImpl.java`、`infrastructure/persistence/mapper/ConsultationMessageMapper.java`、`resources/mapper/ConsultationMessageMapper.xml`（新增 `selectLatestBySessionId`，create_time+id 倒序 limit 1）。
  - 新增测试：`src/test/.../interfaces/controller/ChatStreamControllerTest.java`（未登录 401 error 事件、正常 message+done 序列、业务异常转 error 事件，共 3 用例；直接调用控制器并用 Reactor `collectList().block()` 收集 SSE 事件）。
- **状态**：已完成并验证。`mvn test -Dtest=ChatStreamControllerTest`：Tests run: 3, Failures: 0, Errors: 0 — BUILD SUCCESS；`mvn test-compile` BUILD SUCCESS（编译前需将 `JAVA_HOME` 指向 JDK 17）。真实 AI 流式链路需配置 `OPENAI_*` 环境变量并连通模型后端，未在本地实测。

### 落地 DEV.MD 任务：/api/psychological-chat/sessions 会话列表分页摘要查询

- **背景**：按 `DEV.MD` 要求，基于 `consultation_session` + `consultation_message`（关联 `user` 取昵称）实现会话列表分页接口，返回字段：sessionTitle、startedAt、userNickname、lastMessageContent、lastMessageTime（最后一条消息时间）、messageCount（消息总条数）、durationMinutes（startedAt 与最后一条消息时间之间的分钟数）。
- **依赖**：无新增。
- **改动内容**：
  1. 新增查询投影 `ConsultationSessionSummaryPO`（非表映射 PO），承载 JOIN/聚合结果。
  2. `ConsultationSessionMapper` 新增 `selectSessionSummaryPage(Page, userId)`；XML 实现：JOIN `user` 取 nickname；子查询按 session_id 聚合 COUNT(*)/MAX(create_time)（避免 JOIN 明细行膨胀）；相关子查询取最后一条消息内容（create_time+id 倒序 limit 1）；`TIMESTAMPDIFF(MINUTE, started_at, last_message_time)` 计算时长；过滤 `s.deleted_flag=0 AND s.user_id=#{userId}`，按最后消息时间倒序。
  3. `ConsultationMessageAppService.list(page,size)` 重写：userId 改从 `CurrentUserUtils` 登录上下文取（不接请求参数，防越权），未登录抛 401；返回类型由 `List` 改为 `PageResult<ChatSessionView>`（records/total/size/current/pages）。
  4. `ChatSessionView.from` 改为从摘要投影映射（原从领域对象映射时 5 个字段恒 null）。
  5. Controller `GET /api/psychological-chat/sessions` 返回 `Result<PageResult<ChatSessionView>>`，补接口 JavaDoc。
  6. 删除死代码：仓储端口/实现的 `findByUserId`（已被摘要查询取代）及失效 import。
- **改动文件**：新增 `infrastructure/persistence/po/ConsultationSessionSummaryPO.java`；修改 `ConsultationSessionMapper.java`、`ConsultationSessionMapper.xml`、`ConsultationMessageAppService.java`、`application/view/ChatSessionView.java`、`interfaces/controller/ChatSessionController.java`、`domain/repository/ConsultationSessionRepository.java`、`infrastructure/persistence/repository/ConsultationSessionRepositoryImpl.java`；新增测试 `src/test/.../application/ConsultationMessageAppServiceListTest.java`（已登录分页映射 + 未登录 401，共 2 用例）。
- **状态**：已完成并验证。`mvn test -Dtest=ConsultationMessageAppServiceListTest,ChatStreamControllerTest`：Tests run: 5, Failures: 0, Errors: 0 — BUILD SUCCESS；`mvn test-compile` BUILD SUCCESS。SQL 未连真实库执行，建议联调时核对 TIMESTAMPDIFF 时区与空消息边界（无消息时 messageCount=0、lastMessage*/durationMinutes 为 null）。

### 引入 MongoDB 依赖与连接配置

- **背景**：后续切会话时需把会话记录写入文档库（DEV.MD 任务 2 预备），先行引入 Spring Data MongoDB 并完成连接配置；本轮仅引入依赖与配置，不写实体/Repository。
- **依赖**：`pom.xml` 新增 `org.springframework.boot:spring-boot-starter-data-mongodb`（不写版本号，由 spring-boot-dependencies BOM 3.5.13 统一管理；实际解析：starter 3.5.13、spring-data-mongodb 4.5.10、mongodb-driver-sync/core 5.5.2、bson 5.5.2，兼容 JDK 17）。
- **改动文件**：`pom.xml`（新增 starter 依赖并注释版本来源与用途）、`src/main/resources/application.yml`（`spring.data.mongodb.uri`）。
- **改动内容**：MongoDB 采用单一 URI 配置源 `${MONGODB_URI:mongodb://root:123456@192.168.136.128:27017/ai-agent?authSource=admin}`，默认与 MySQL/Redis 同主机，业务库 ai-agent，认证库 admin；注释说明凭据特殊字符需 URL 编码、生产用环境变量覆盖、驱动懒连接不影响启动。未新增 MongoConfig 类——Spring Boot 自动装配已提供 MongoClient/MongoTemplate。
- **状态**：已完成并验证。`mvn compile` BUILD SUCCESS；`mvn dependency:list` 确认 mongodb 相关构件版本由 BOM 正确解析。实际连通性待 192.168.136.128 上 MongoDB 服务与账号确认后联调（若报 Unauthorized，优先检查 authSource 与账号所属库是否一致）。
