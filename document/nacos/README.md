# Nacos 配置落地说明

本文档定义 Refinex 当前配置中心标准，目标是：

1. 三个服务（`refinex-auth` / `refinex-user` / `refinex-gateway`）本地仅保留 **Nacos 启动引导配置**。
2. 业务与中间件配置统一放到 Nacos。
3. 通过统一 Group/Namespace 避免配置散落与漂移。

## 1. Namespace 与 Group 约定

- `namespace`：建议按环境拆分（`dev` / `test` / `prod`），默认可用 `public`
- `group` 规划：

1. `REFINEX_SHARED`：共享配置
2. `REFINEX_SERVICE`：服务专属配置
3. `SENTINEL_GROUP`：Sentinel 规则配置

## 2. 需要在 Nacos 创建的 DataId

### 2.1 共享配置（Group=`REFINEX_SHARED`）

1. `refinex-base.yml`
2. `refinex-cache.yml`
3. `refinex-datasource.yml`
4. `refinex-es.yml`
5. `refinex-job.yml`
6. `refinex-limiter.yml`
7. `refinex-seata.yml`
8. `refinex-stream.yml`

### 2.2 服务配置（Group=`REFINEX_SERVICE`）

1. `refinex-auth.yml`
2. `refinex-user.yml`
3. `refinex-gateway.yml`

### 2.3 Sentinel 规则（Group=`SENTINEL_GROUP`）

1. `refinex-gateway-flow-rules`（网关流控规则）
2. `business-flow-rules`（业务流控规则，可选）
3. `business-param-flow-rules`（热点参数规则，可选）

> Sentinel 规则通常是 JSON，不是 YAML。

## 3. `document/nacos` 文件说明

`document/nacos/*.yml` 是配置中心的标准源文件：

- `document/nacos/refinex-base.yml`
- `document/nacos/refinex-cache.yml`
- `document/nacos/refinex-datasource.yml`
- `document/nacos/refinex-es.yml`
- `document/nacos/refinex-job.yml`
- `document/nacos/refinex-limiter.yml`
- `document/nacos/refinex-seata.yml`
- `document/nacos/refinex-stream.yml`
- `document/nacos/refinex-auth.yml`
- `document/nacos/refinex-user.yml`
- `document/nacos/refinex-gateway.yml`

其中邮件能力配置已并入 `refinex-base.yml`（`refinex.mail.*` 与 `spring.mail.*`），
认证服务可直接消费，无需新增独立 DataId。
同时 `refinex-base.yml` 已收敛为“非敏感固定值 + 敏感项占位符”模式。

## 4. 大写占位符（`${REFINEX_XXX}`）来源说明

你看到的 `${REFINEX_XXX}`（大写变量）可以来自以下任一来源：

1. **环境变量**（推荐）
2. **JVM 启动参数**（`-DREFINEX_XXX=...`）
3. **jar 同级外部配置文件**（`./config/application.yml`）
4. **容器平台注入**（K8s ConfigMap/Secret、Docker Compose env）

### 4.1 本仓库已提供敏感变量模板

`config/application.yml` 仅维护敏感变量（密码、密钥、鉴权令牌）：

- `config/application.yml`

非敏感配置（开关、地址、端口、策略）统一放 Nacos DataId。

### 4.2 推荐策略

1. 密码、密钥、Token：优先环境变量或 Secret 注入
2. 非敏感参数：放 Nacos 共享/服务配置
3. 本地开发：可用 `config/application.yml` 仅托底敏感项

### 4.3 jar 同级配置示例

`config/application.yml` 已给出敏感变量清单与中文注释，按实际环境替换即可。

### 4.4 环境变量示例

```bash
export REFINEX_NACOS_SERVER_ADDR=127.0.0.1:8848
export REFINEX_NACOS_NAMESPACE=dev
export REFINEX_MYSQL_PASSWORD=your_password
export REFINEX_REDIS_PASSWORD=your_redis_password
```

## 5. 三服务 `application.yml` 规范

以下三个文件已标准化为“仅引导 Nacos”模式：

1. `refinex-auth/src/main/resources/application.yml`
2. `refinex-business/refinex-user/src/main/resources/application.yml`
3. `refinex-gateway/src/main/resources/application.yml`

仅保留：

1. `spring.application.name`
2. Nacos 连接参数（`server-addr/username/password/namespace`）
3. `spring.config.import`（导入共享与服务 DataId）

## 6. 旧 classpath 配置文件处理说明

以下文件仍保留在模块 `resources` 中作为模板/兜底，不再由三服务通过 `classpath:` 显式导入：

- `refinex-common/refinex-base/src/main/resources/refinex-base.yml`
- `refinex-common/refinex-cache/src/main/resources/refinex-cache.yml`
- `refinex-common/refinex-datasource/src/main/resources/refinex-datasource.yml`
- `refinex-common/refinex-elasticsearch/src/main/resources/refinex-es.yml`
- `refinex-common/refinex-job/src/main/resources/refinex-job.yml`
- `refinex-common/refinex-limiter/src/main/resources/refinex-limiter.yml`
- `refinex-common/refinex-seata/src/main/resources/refinex-seata.yml`
- `refinex-common/refinex-stream/src/main/resources/refinex-stream.yml`

建议后续通过发布脚本将 `document/nacos/*.yml` 同步到 Nacos，保证单一事实源。
