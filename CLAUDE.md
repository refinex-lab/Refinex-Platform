# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Refinex-Platform is an AI-Native multi-tenant enterprise microservices platform built with Spring Boot 4.0.2, Spring Cloud 2025.1.1, and React. It provides authentication, RBAC authorization, organization management, knowledge base/RAG capabilities, and AI/Agent extensions.

**Tech Stack:**
- Backend: Java 21, Spring Boot 4, Spring Cloud, Spring Cloud Alibaba (Nacos)
- Frontend: React 19, Vite, TanStack Router, Shadcn UI, TailwindCSS 4
- Database: MySQL 8+, Redis 7+
- Auth: Sa-Token
- ORM: MyBatis-Plus 3.5.15

## Repository Structure

```
Refinex-Platform/
├── refinex-admin/              # React frontend (Vite + Shadcn UI)
├── refinex-gateway/            # Spring Cloud Gateway
├── refinex-auth/               # Authentication service
├── refinex-business/
│   ├── refinex-user/           # User domain service (profile, account)
│   ├── refinex-system/         # System management (RBAC, org, data resources)
│   ├── refinex-kb/             # Knowledge base & RAG
│   ├── refinex-ai/             # AI & Agent orchestration
│   └── refinex-openapi/        # Open API & ecosystem integration
├── refinex-common/             # Shared modules (base, web, cache, datasource, file, mail, sms, etc.)
├── document/
│   ├── sql/                    # Database schemas
│   └── nacos/                  # Nacos configuration files
└── config/                     # Local sensitive config (not in git)
```

## Build & Run Commands

### Backend (Java)

```bash
# Build entire project from root
mvn clean install -DskipTests

# Build specific module
cd refinex-auth
mvn clean package -DskipTests

# Run a service (from module directory)
mvn spring-boot:run

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**Service startup order:**
1. Nacos, MySQL, Redis
2. `refinex-gateway` (port 8080)
3. `refinex-auth` (port 8081)
4. `refinex-business/refinex-user` (port 8082)
5. `refinex-business/refinex-system` (port 8083)

### Frontend (React)

```bash
cd refinex-admin

# Install dependencies
pnpm install

# Development server (port 5173)
pnpm dev

# Build for production
pnpm build

# Lint
pnpm lint

# Format code
pnpm format
```

## Architecture Patterns

### Domain-Driven Design (DDD)

Each business module follows DDD layering:

```
refinex-business/refinex-{module}/
└── src/main/java/cn/refinex/{module}/
    ├── api/                    # REST controllers
    ├── application/
    │   ├── service/            # Application services (orchestration)
    │   ├── command/            # Command DTOs
    │   ├── dto/                # Data transfer objects
    │   └── assembler/          # Domain ↔ DTO converters
    ├── domain/
    │   ├── model/
    │   │   ├── entity/         # Domain entities
    │   │   └── enums/          # Domain enums
    │   ├── repository/         # Repository interfaces
    │   └── error/              # Domain error codes
    └── infrastructure/
        ├── persistence/
        │   ├── dataobject/     # Database entities (DO)
        │   ├── mapper/         # MyBatis mappers
        │   └── repository/     # Repository implementations
        └── client/             # External service clients
```

### Key Architectural Decisions

1. **Service Boundaries:**
   - `refinex-auth`: Authentication flows only (login, register, password reset)
   - `refinex-user`: Current user perspective (profile, account settings)
   - `refinex-system`: System administration (RBAC, organizations, data resources)
   - Services communicate via Feign clients defined in `refinex-common/refinex-api`

2. **Multi-Tenancy:**
   - Tenant isolation via `estab_id` (organization ID)
   - Platform-level resources use `estab_id = 0`
   - User-tenant relationships in `def_estab_user` table

3. **RBAC Model:**
   - Roles (`scr_role`) can be system-built, tenant-built, or custom
   - Permissions include: menus (`scr_menu`), menu operations (`scr_menu_op`), and data resource interfaces (`scr_drs_interface`)
   - Data resource permissions use SQL-based filtering for row-level security

4. **Username vs User Code:**
   - `username` is NOT unique (can be real name, allows duplicates)
   - `user_code` is unique and system-generated (format: `U_XXXXXXXXXX`)
   - Database: `def_user` table has `UNIQUE KEY uk_user_code` but only `KEY idx_username`

## Database Schema

Primary database: `refinex_platform`

**Key tables:**
- `def_user`: User master data
- `def_user_identity`: Login identities (username/password, phone/SMS, email, OAuth, etc.)
- `def_estab`: Organizations/tenants
- `def_estab_user`: User-organization relationships
- `scr_role`: Roles
- `scr_role_user`: Role assignments
- `scr_menu`: Menu definitions
- `scr_role_menu`: Role-menu permissions
- `scr_drs`: Data resources
- `scr_drs_interface`: Data resource interfaces
- `scr_role_drs`: Role-data resource permissions

**Schema location:** `document/sql/001_user_rbac_schema.sql`

## Configuration Management

### Nacos Configuration

Configuration is split between Nacos and local files:

1. **Nacos configs** (in `document/nacos/`):
   - `REFINEX_SHARED`: Shared configuration (datasource, redis, etc.)
   - `REFINEX_SERVICE`: Service-specific configuration

2. **Local config** (`config/application.yml`):
   - Sensitive information (passwords, API keys, addresses)
   - Not committed to git
   - Loaded via `spring.config.import=optional:file:./config/application.yml`

### Environment Profiles

Maven profiles: `dev` (default), `test`, `pre`, `prod`

Activate via: `mvn spring-boot:run -Dspring-boot.run.profiles=test`

## Frontend Architecture

### React Admin (`refinex-admin`)

- **Router:** TanStack Router (file-based routing in `src/routes/`)
- **State:** Zustand stores (e.g., `src/stores/auth-store.ts`)
- **API:** Axios with interceptors in `src/lib/api-client.ts`
- **UI:** Shadcn UI components (customized for RTL support)
- **Forms:** React Hook Form + Zod validation

**Key directories:**
- `src/features/`: Feature modules (auth, system, user-management, etc.)
- `src/components/`: Reusable UI components
- `src/lib/`: Utilities and helpers
- `src/stores/`: Zustand state stores

**Modified Shadcn components** (don't blindly update via CLI):
- Modified: scroll-area, sonner, separator
- RTL-updated: alert-dialog, calendar, command, dialog, dropdown-menu, select, table, sheet, sidebar, switch

## Common Development Tasks

### Adding a New Business Module

1. Create module under `refinex-business/refinex-{module}/`
2. Follow DDD structure (api, application, domain, infrastructure)
3. Add module to parent `pom.xml`
4. Define API contracts in `refinex-common/refinex-api`
5. Register service in Nacos
6. Add gateway routes in `refinex-gateway`

### Adding a New API Endpoint

1. Define DTO in `application/dto/` or `application/command/`
2. Add method to application service
3. Create REST controller in `api/` package
4. Add Feign client interface in `refinex-api` if needed by other services

### Database Changes

1. Update schema in `document/sql/001_user_rbac_schema.sql`
2. Create corresponding DO (DataObject) in `infrastructure/persistence/dataobject/`
3. Create/update MyBatis mapper in `infrastructure/persistence/mapper/`
4. Update domain entity if needed
5. Update repository interface and implementation

### Frontend Feature Development

1. Create feature directory in `src/features/{feature-name}/`
2. Define API functions in feature's `api.ts`
3. Create page components
4. Add route in `src/routes/`
5. Update navigation in sidebar if needed

## Code Conventions

### Backend

- **Package naming:** `cn.refinex.{module}.{layer}`
- **Class naming:**
  - Controllers: `*Controller`
  - Services: `*Service` or `*ApplicationService`
  - Repositories: `*Repository`
  - DTOs: `*DTO`, `*Command`, `*Query`
  - Domain entities: `*Entity`
  - Database entities: `*Do` (e.g., `DefUserDo`)
- **Error handling:** Use `BizException` with error codes from `*ErrorCode` classes
- **Validation:** Use JSR-303 annotations on DTOs
- **字段注释规范（强制）：** 所有 Java POJO 类（DO、Entity、DTO、VO、Command、Query、Request）的每个字段注释必须严格对齐 `document/sql/` 下对应数据库表字段的 COMMENT，保持文字完全一致，不得自行改写或省略。数据库 COMMENT 是字段注释的唯一权威来源。
- **方法注释规范（强制）：** 所有公开方法（public method）必须编写标准 Javadoc 注释，包含方法说明、`@param` 参数描述、`@return` 返回值描述（void 方法除外）。私有辅助方法也应有简要 Javadoc。

### Frontend

- **File naming:** kebab-case for files, PascalCase for components
- **Component structure:** Functional components with hooks
- **State management:** Zustand for global state, React Hook Form for form state
- **API calls:** Centralized in feature's `api.ts` file
- **Styling:** TailwindCSS utility classes, avoid custom CSS

## Testing

### Backend

```bash
# Run all tests
mvn test

# Run tests for specific module
cd refinex-auth
mvn test

# Run single test class
mvn test -Dtest=UserApplicationServiceTest

# Run single test method
mvn test -Dtest=UserApplicationServiceTest#testCreateUser
```

### Frontend

```bash
cd refinex-admin

# Run linter
pnpm lint

# Format check
pnpm format:check

# Format fix
pnpm format
```

## Important Notes

1. **User Identity System:**
   - Multiple identity types supported (username/password, phone/SMS, email, OAuth, SAML, TOTP)
   - One user can have multiple identities
   - Identity uniqueness: `(identity_type, identifier, issuer)` must be unique

2. **Permission System:**
   - Three permission types: menu access, menu operations, data resource interfaces
   - Data resource permissions use SQL expressions for row-level filtering
   - Permissions are assigned to roles, roles are assigned to users

3. **Tenant Isolation:**
   - Most entities have `estab_id` for tenant isolation
   - Platform-level entities use `estab_id = 0`
   - Always filter by `estab_id` in queries unless explicitly platform-level

4. **Soft Delete:**
   - All entities have `deleted` field (0 = active, 1 = deleted)
   - Always include `deleted = 0` in queries
   - Use logical delete instead of physical delete

5. **Optimistic Locking:**
   - All entities have `lock_version` field
   - MyBatis-Plus handles version checking automatically

6. **Audit Fields:**
   - All entities have: `create_by`, `update_by`, `delete_by`, `gmt_create`, `gmt_modified`
   - Automatically populated by MyBatis-Plus handlers
