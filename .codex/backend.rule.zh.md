# Refinex Backend AI Development Rules（ZH）

# Java 21 + Spring Boot 4 + Spring Cloud 2025.1

本规范是 Refinex 后端代码生成与重构的强制规则。
AI 在生成、修改、重构任何后端代码时，**MUST 严格遵守**。
违反本规范属于 **严重架构违规（Critical Architecture Violation）**。

---

# 0. 架构目标

架构基础：

- DDD（Domain-Driven Design）
- Clean Architecture
- Hexagonal Architecture
- AI-First Maintainability

核心目标：

- 业务规则内聚到 domain，避免“贫血模型”
- 技术细节下沉到 infrastructure，避免业务逻辑扩散
- 严格控制依赖方向，避免层间污染
- 保证系统长期可维护与可演进

---

# 1. 技术栈基线（MUST）

MUST 使用：

```text
Java 21（项目基线；不得低于 17）
Spring Framework 7.x
Spring Boot 4.x
Spring Cloud 2025.1.x
Spring Cloud Alibaba 2025.x
MyBatis Plus 3.5.x
MapStruct 1.6.x
Spring HTTP Service Client
```

MUST NOT 使用：

```text
FeignClient / OpenFeign
RestTemplate（新增代码）
BeanUtils.copyProperties
ModelMapper
跨层手写对象转换（绕过 MapStruct）
```

说明：OpenFeign 已 feature-complete；新代码统一使用 Spring HTTP Service Client。

---

# 2. 标准四层架构（MUST）

系统必须采用以下分层：

```text
interfaces
application
domain
infrastructure
```

标准依赖方向（铁律）：

```text
interfaces -> application -> domain
infrastructure -> domain
```

禁止依赖方向：

```text
domain -> application
domain -> interfaces
domain -> infrastructure

application -> interfaces
application -> infrastructure.persistence.mapper

interfaces -> domain
interfaces -> infrastructure
interfaces -> mapper / repository impl
```

---

# 3. 分层职责规则（MUST）

## 3.1 interfaces 层

推荐位置：

```text
interfaces.controller
interfaces.facade
interfaces.listener
interfaces.job
interfaces.vo
```

职责：

- 接收请求（HTTP/MQ/Job）
- 参数校验
- 调用 application
- DTO -> VO 返回

禁止：

- 业务规则判断
- 数据库访问
- 远程 HTTP 调用
- 直接访问 mapper/repository impl/infrastructure

## 3.2 application 层（Use Case）

推荐位置：

```text
application.service
application.command
application.query
application.dto
application.assembler
```

职责：

- 编排用例流程
- 调用 domain（实体/领域服务/仓储接口）
- 管理事务边界
- DTO <-> Domain 转换

必须：

- 通过 domain.repository / domain.gateway 接口访问外部能力
- 使用 MapStruct（assembler）完成 DTO <-> Domain 转换

禁止：

- 直接访问 mapper / database
- 直接访问 infrastructure.client / Redis / MQ
- 在 application 实现领域规则（规则应回到 domain）

## 3.3 domain 层（核心层，最高优先级）

推荐位置：

```text
domain.model.entity
domain.model.valueobject
domain.service
domain.repository
domain.gateway
domain.event
```

只允许包含：

- Entity / Aggregate / ValueObject
- DomainService
- Repository Interface / Gateway Interface
- Domain Event

绝对禁止：

```text
Spring 注解与 API
MyBatis 注解与 API
HTTP/Redis/MQ/数据库客户端
任何 infrastructure 实现类
```

约束：

- domain 必须保持 Pure Java、Framework-Independent
- 聚合间通过 ID 引用，不直接持有外部聚合对象

## 3.4 infrastructure 层

推荐位置：

```text
infrastructure.persistence.mapper
infrastructure.persistence.entity
infrastructure.persistence.repository
infrastructure.persistence.converter
infrastructure.client
infrastructure.config
infrastructure.mq
infrastructure.cache
```

职责：

- 实现 domain.repository / domain.gateway
- 数据库访问（Mapper）
- 远程调用（HTTP Service Client）
- MQ/Redis/配置等技术实现

---

# 4. 数据对象边界（MUST）

严格区分对象类型：

- `VO`：仅 interfaces 使用
- `DTO`：仅 application 使用
- `Entity/VO(ValueObject)`：仅 domain 使用
- `DO`：仅 infrastructure 持久化使用

禁止跨层泄露：

- Controller 返回 DO
- Application 直接操作 DO
- Domain 出现 DTO/VO/DO

---

# 5. Repository 规则（MUST）

规则：

- `domain.repository.*` 只定义接口
- `infrastructure.persistence.repository.*` 实现接口
- Mapper 只能在 `infrastructure.persistence.mapper` 中出现并被 repository impl 调用

禁止：

- application 直接注入 mapper
- interfaces 直接访问 repository impl
- domain 直接访问 mapper/database

---

# 6. 远程调用规则（Spring HTTP Service Client）

## 6.1 位置与定义

- HTTP 接口定义在 `infrastructure.client`
- 使用 `@HttpExchange` + `@GetExchange/@PostExchange/...`
- 不使用 `@FeignClient`

## 6.2 注册与分组（Framework 7 / Boot 4）

- 使用 `@ImportHttpServices(group = "<group>", ...)` 注册客户端
- `group` 建议与 serviceId 对齐（如 `user-service`）
- 可通过 `HttpServiceProxyRegistry` 按 group 获取代理（多租户/多区域场景）

## 6.3 配置键（Boot 4）

必须使用：

```text
spring.http.clients.*
spring.http.serviceclient.<group>.*
```

禁止使用旧错误键：

```text
spring.http.client.service.*
```

## 6.4 内外部调用约定

- 内部微服务：优先 `base-url: lb://<service-id>`（接入 LoadBalancer）
- 外部第三方：使用显式 `https://...`
- 常量 header 可配在 group 或注解；动态鉴权/trace/tenant 必须走拦截器或 group configurer

## 6.5 容错与可观测性

- 需要降级时使用 `@HttpServiceFallback`
- 错误语义统一映射为业务异常，禁止将所有错误抛成同一种 RuntimeException
- 关键 client 必须有契约测试（如 WireMock）

---

# 7. MapStruct 规则（MUST）

转换层位置：

- `application.assembler`：DTO <-> Domain
- `infrastructure.persistence.converter`：Domain <-> DO

禁止：

- `BeanUtils.copyProperties`
- 大量散落的手写转换逻辑

补充：确有复杂映射时，优先在 MapStruct 的 `default` 方法或表达式内封装，避免跨层复制粘贴转换代码。

---

# 8. 事务规则（MUST）

`@Transactional` 只允许出现在：

```text
application.service
```

禁止出现在：

```text
interfaces
domain
infrastructure.persistence.repository
infrastructure.persistence.mapper
```

说明：并非每个 application 方法都必须开启事务；仅在需要事务边界的用例上使用。

---

# 9. Domain 纯净性检查（MUST）

Code Review 必检：

- domain 无 `import org.springframework.*`
- domain 无 `import com.baomidou.*`
- Entity 状态变更通过业务方法，不靠外部 setter 任意改写
- ValueObject 不可变
- domain 单元测试可脱离 Spring 容器运行

---

# 10. refinex-common 复用规则（MUST）

开发前必须检查：

```text
refinex-common
```

优先复用已有能力（如 `Result`、`PageResult`、通用异常、基础工具类等）。

禁止：

- 在业务 service 模块重复实现 common 已存在能力

能力缺失时：

- 先补充到 `refinex-common` 对应子模块，再在业务模块使用

---

# 11. 严重违规清单（MUST NEVER）

以下行为属于严重违规：

- Controller/Facade 直接访问 mapper
- Application 直接操作 DO 或 mapper
- Domain 使用 Spring/MyBatis/HTTP 客户端
- 跳过 application，直接从 interfaces 调 domain
- 新增 Feign/OpenFeign 客户端
- 使用错误的 Boot 4 HTTP Client 配置前缀
- 手写跨层对象转换替代 MapStruct

---

# 12. AI 开发执行顺序（MUST）

AI 必须按顺序执行：

1. 检查 `refinex-common` 是否已有可复用能力
2. 确认变更所在层与依赖方向是否合法
3. 按对象边界设计 VO/DTO/Entity/DO
4. 优先定义 domain 接口，再由 infrastructure 实现
5. 远程调用统一走 HTTP Service Client（group 化配置）
6. 补充必要测试（至少覆盖核心业务规则与关键客户端契约）

决策优先级：

```text
复用 > 扩展 > 新建
```

---

# 13. 参考依据

本规范依据并对齐以下文档：

- `document/reference/领域驱动设计（DDD）：从核心概念到 Spring Cloud 标准落地.md`
- `document/reference/Spring HTTP Service Client 指南.md`

如历史代码与本规范冲突，以本规范为准。
