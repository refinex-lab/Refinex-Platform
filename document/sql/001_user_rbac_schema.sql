-- ============================================================================
-- Refinex Platform 用户与RBAC权限系统 MySQL 建表结构
-- 约定：
-- 1) 所有表包含 BaseEntity 通用字段：id, create_by, update_by, delete_by, deleted, lock_version, gmt_create, gmt_modified
-- 2) 编码字段统一使用 code 后缀，排序字段统一为 sort，状态字段统一为 status
-- 3) 字符集/排序规则统一：utf8mb4 / utf8mb4_0900_ai_ci
-- ============================================================================

-- 如果数据库 refinex_platform 不存在则创建，编码 utf8mb4，排序规则 utf8mb4_0900_ai_ci
CREATE DATABASE IF NOT EXISTS refinex_platform DEFAULT CHARACTER SET utf8mb4 DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE refinex_platform;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================
-- 字典/值集
-- ============================
DROP TABLE IF EXISTS app_valueset;
CREATE TABLE app_valueset (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  set_code VARCHAR(64) NOT NULL COMMENT '值集编码',
  set_name VARCHAR(128) NOT NULL COMMENT '值集名称',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
  sort INT NOT NULL DEFAULT 0 COMMENT '排序(升序)',
  description VARCHAR(255) DEFAULT NULL COMMENT '描述',
  ext_json JSON DEFAULT NULL COMMENT '扩展信息',
  create_by BIGINT DEFAULT NULL COMMENT '创建人用户ID',
  update_by BIGINT DEFAULT NULL COMMENT '更新人用户ID',
  delete_by BIGINT DEFAULT NULL COMMENT '删除人用户ID',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  lock_version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  gmt_modified DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  UNIQUE KEY uk_valueset_code (set_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='值集定义';

DROP TABLE IF EXISTS app_value;
CREATE TABLE app_value (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  set_code VARCHAR(64) NOT NULL COMMENT '值集编码',
  value_code VARCHAR(64) NOT NULL COMMENT '值编码',
  value_name VARCHAR(128) NOT NULL COMMENT '值名称',
  value_desc VARCHAR(255) DEFAULT NULL COMMENT '值描述',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
  is_default TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认 1是 0否',
  sort INT NOT NULL DEFAULT 0 COMMENT '排序(升序)',
  ext_json JSON DEFAULT NULL COMMENT '扩展信息',
  create_by BIGINT DEFAULT NULL COMMENT '创建人用户ID',
  update_by BIGINT DEFAULT NULL COMMENT '更新人用户ID',
  delete_by BIGINT DEFAULT NULL COMMENT '删除人用户ID',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  lock_version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  gmt_modified DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  UNIQUE KEY uk_value_code (set_code, value_code),
  KEY idx_value_set (set_code, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='值集明细';

-- ============================
-- 组织/用户/团队
-- ============================
DROP TABLE IF EXISTS def_estab;
CREATE TABLE def_estab (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  estab_code VARCHAR(64) NOT NULL COMMENT '组织编码',
  estab_name VARCHAR(128) NOT NULL COMMENT '组织名称',
  estab_short_name VARCHAR(64) DEFAULT NULL COMMENT '组织简称',
  estab_type TINYINT NOT NULL DEFAULT 1 COMMENT '组织类型 0平台 1租户 2合作方',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 2停用 3冻结',
  industry_code VARCHAR(64) DEFAULT NULL COMMENT '行业编码(值集 industry_type)',
  size_range VARCHAR(64) DEFAULT NULL COMMENT '规模区间(值集 estab_size)',
  owner_user_id BIGINT DEFAULT NULL COMMENT '主负责人用户ID',
  contact_name VARCHAR(64) DEFAULT NULL COMMENT '联系人',
  contact_phone VARCHAR(32) DEFAULT NULL COMMENT '联系人电话',
  contact_email VARCHAR(128) DEFAULT NULL COMMENT '联系人邮箱',
  website_url VARCHAR(255) DEFAULT NULL COMMENT '官网地址',
  logo_url VARCHAR(255) DEFAULT NULL COMMENT 'Logo地址',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  ext_json JSON DEFAULT NULL COMMENT '扩展信息',
  create_by BIGINT DEFAULT NULL COMMENT '创建人用户ID',
  update_by BIGINT DEFAULT NULL COMMENT '更新人用户ID',
  delete_by BIGINT DEFAULT NULL COMMENT '删除人用户ID',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  lock_version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  gmt_modified DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  UNIQUE KEY uk_estab_code (estab_code),
  KEY idx_estab_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='组织/租户';

DROP TABLE IF EXISTS def_estab_address;
CREATE TABLE def_estab_address (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  estab_id BIGINT NOT NULL COMMENT '组织ID',
  addr_type TINYINT NOT NULL DEFAULT 1 COMMENT '地址类型 1注册 2办公 3收货',
  country_code VARCHAR(16) DEFAULT NULL COMMENT '国家/地区代码',
  province_code VARCHAR(32) DEFAULT NULL COMMENT '省份代码',
  city_code VARCHAR(32) DEFAULT NULL COMMENT '城市代码',
  district_code VARCHAR(32) DEFAULT NULL COMMENT '区县代码',
  province_name VARCHAR(64) DEFAULT NULL COMMENT '省份名称',
  city_name VARCHAR(64) DEFAULT NULL COMMENT '城市名称',
  district_name VARCHAR(64) DEFAULT NULL COMMENT '区县名称',
  address_line1 VARCHAR(255) DEFAULT NULL COMMENT '地址1',
  address_line2 VARCHAR(255) DEFAULT NULL COMMENT '地址2',
  postal_code VARCHAR(32) DEFAULT NULL COMMENT '邮编',
  latitude DECIMAL(10,6) DEFAULT NULL COMMENT '纬度',
  longitude DECIMAL(10,6) DEFAULT NULL COMMENT '经度',
  is_default TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认 1是 0否',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  ext_json JSON DEFAULT NULL COMMENT '扩展信息',
  create_by BIGINT DEFAULT NULL COMMENT '创建人用户ID',
  update_by BIGINT DEFAULT NULL COMMENT '更新人用户ID',
  delete_by BIGINT DEFAULT NULL COMMENT '删除人用户ID',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  lock_version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  gmt_modified DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  KEY idx_estab_addr (estab_id, addr_type),
  KEY idx_estab_default (estab_id, is_default)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='组织地址';

DROP TABLE IF EXISTS def_estab_auth_policy;
CREATE TABLE def_estab_auth_policy (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  estab_id BIGINT NOT NULL COMMENT '组织ID',
  password_login_enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否允许密码登录 1是 0否',
  sms_login_enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否允许短信登录 1是 0否',
  email_login_enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否允许邮箱登录 1是 0否',
  wechat_login_enabled TINYINT NOT NULL DEFAULT 0 COMMENT '是否允许微信扫码登录 1是 0否',
  mfa_required TINYINT NOT NULL DEFAULT 0 COMMENT '是否强制多因素认证 1是 0否',
  mfa_methods VARCHAR(128) DEFAULT NULL COMMENT '多因素方式(逗号分隔，如 TOTP,SMS,EMAIL,WEBAUTHN)',
  password_min_len TINYINT NOT NULL DEFAULT 8 COMMENT '密码最小长度',
  password_strength TINYINT NOT NULL DEFAULT 1 COMMENT '密码强度 0低 1中 2高',
  password_expire_days INT NOT NULL DEFAULT 0 COMMENT '密码过期天数(0表示不过期)',
  login_fail_threshold INT NOT NULL DEFAULT 5 COMMENT '连续失败阈值',
  lock_minutes INT NOT NULL DEFAULT 30 COMMENT '锁定时长(分钟)',
  session_timeout_minutes INT NOT NULL DEFAULT 120 COMMENT '会话超时(分钟)',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  ext_json JSON DEFAULT NULL COMMENT '扩展信息',
  create_by BIGINT DEFAULT NULL COMMENT '创建人用户ID',
  update_by BIGINT DEFAULT NULL COMMENT '更新人用户ID',
  delete_by BIGINT DEFAULT NULL COMMENT '删除人用户ID',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  lock_version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  gmt_modified DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  UNIQUE KEY uk_estab_auth_policy (estab_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='组织认证与安全策略';

DROP TABLE IF EXISTS def_user;
CREATE TABLE def_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  user_code VARCHAR(64) NOT NULL COMMENT '用户编码',
  username VARCHAR(64) DEFAULT NULL COMMENT '用户名(本地账号)',
  display_name VARCHAR(64) DEFAULT NULL COMMENT '显示名称',
  nickname VARCHAR(64) DEFAULT NULL COMMENT '昵称',
  avatar_url VARCHAR(255) DEFAULT NULL COMMENT '头像',
  gender TINYINT NOT NULL DEFAULT 0 COMMENT '性别 0未知 1男 2女 3其他',
  birthday DATE DEFAULT NULL COMMENT '生日',
  user_type TINYINT NOT NULL DEFAULT 1 COMMENT '用户类型 0平台 1租户 2合作方',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 2停用 3锁定',
  primary_estab_id BIGINT DEFAULT NULL COMMENT '主组织ID',
  primary_phone VARCHAR(32) DEFAULT NULL COMMENT '主手机号',
  phone_verified TINYINT NOT NULL DEFAULT 0 COMMENT '手机号是否已验证 1是 0否',
  primary_email VARCHAR(128) DEFAULT NULL COMMENT '主邮箱',
  email_verified TINYINT NOT NULL DEFAULT 0 COMMENT '邮箱是否已验证 1是 0否',
  last_login_time DATETIME(3) DEFAULT NULL COMMENT '最后登录时间',
  last_login_ip VARCHAR(64) DEFAULT NULL COMMENT '最后登录IP',
  login_fail_count INT NOT NULL DEFAULT 0 COMMENT '连续登录失败次数',
  lock_until DATETIME(3) DEFAULT NULL COMMENT '锁定截止时间',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  ext_json JSON DEFAULT NULL COMMENT '扩展信息',
  create_by BIGINT DEFAULT NULL COMMENT '创建人用户ID',
  update_by BIGINT DEFAULT NULL COMMENT '更新人用户ID',
  delete_by BIGINT DEFAULT NULL COMMENT '删除人用户ID',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  lock_version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  gmt_modified DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  UNIQUE KEY uk_user_code (user_code),
  UNIQUE KEY uk_username (username),
  KEY idx_user_status (status),
  KEY idx_user_estab (primary_estab_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户主档';

DROP TABLE IF EXISTS def_user_identity;
CREATE TABLE def_user_identity (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  identity_type TINYINT NOT NULL COMMENT '登录方式 1用户名密码 2手机号短信 3邮箱密码 4邮箱验证码 5微信扫码 6OAuth/OIDC 7SAML 8TOTP',
  identifier VARCHAR(191) NOT NULL COMMENT '登录标识(手机号/邮箱/用户名/openid/unionid等)',
  issuer VARCHAR(255) NOT NULL DEFAULT '' COMMENT '身份提供方/发行者(如 wechat/oidc iss)',
  credential VARCHAR(255) DEFAULT NULL COMMENT '凭证(密码哈希/密钥/令牌)',
  credential_alg VARCHAR(32) DEFAULT NULL COMMENT '凭证算法(如 bcrypt/scrypt/argon2)',
  is_primary TINYINT NOT NULL DEFAULT 0 COMMENT '是否主身份 1是 0否',
  verified TINYINT NOT NULL DEFAULT 0 COMMENT '是否已验证 1是 0否',
  verified_at DATETIME(3) DEFAULT NULL COMMENT '验证时间',
  bind_time DATETIME(3) DEFAULT NULL COMMENT '绑定时间',
  last_login_time DATETIME(3) DEFAULT NULL COMMENT '最近登录时间',
  last_login_ip VARCHAR(64) DEFAULT NULL COMMENT '最近登录IP',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
  ext_json JSON DEFAULT NULL COMMENT '扩展信息(如微信unionid)',
  create_by BIGINT DEFAULT NULL COMMENT '创建人用户ID',
  update_by BIGINT DEFAULT NULL COMMENT '更新人用户ID',
  delete_by BIGINT DEFAULT NULL COMMENT '删除人用户ID',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  lock_version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  gmt_modified DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  UNIQUE KEY uk_identity_unique (identity_type, identifier, issuer),
  KEY idx_identity_user (user_id, identity_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户登录身份';

DROP TABLE IF EXISTS def_estab_user;
CREATE TABLE def_estab_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  estab_id BIGINT NOT NULL COMMENT '组织ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  member_type TINYINT NOT NULL DEFAULT 0 COMMENT '成员类型 0员工 1外包 2外部协作',
  is_admin TINYINT NOT NULL DEFAULT 0 COMMENT '是否组织管理员 1是 0否',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1有效 2禁用 3离职',
  join_time DATETIME(3) DEFAULT NULL COMMENT '加入时间',
  leave_time DATETIME(3) DEFAULT NULL COMMENT '离开时间',
  position_title VARCHAR(64) DEFAULT NULL COMMENT '职位',
  ext_json JSON DEFAULT NULL COMMENT '扩展信息',
  create_by BIGINT DEFAULT NULL COMMENT '创建人用户ID',
  update_by BIGINT DEFAULT NULL COMMENT '更新人用户ID',
  delete_by BIGINT DEFAULT NULL COMMENT '删除人用户ID',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  lock_version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  gmt_modified DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  UNIQUE KEY uk_estab_user (estab_id, user_id),
  KEY idx_user_estab_status (user_id, estab_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='组织成员关系';

DROP TABLE IF EXISTS def_team;
CREATE TABLE def_team (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  estab_id BIGINT NOT NULL COMMENT '组织ID',
  team_code VARCHAR(64) NOT NULL COMMENT '团队编码',
  team_name VARCHAR(128) NOT NULL COMMENT '团队名称',
  parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父级团队ID',
  leader_user_id BIGINT DEFAULT NULL COMMENT '负责人用户ID',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 2停用',
  sort INT NOT NULL DEFAULT 0 COMMENT '排序(升序)',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  ext_json JSON DEFAULT NULL COMMENT '扩展信息',
  create_by BIGINT DEFAULT NULL COMMENT '创建人用户ID',
  update_by BIGINT DEFAULT NULL COMMENT '更新人用户ID',
  delete_by BIGINT DEFAULT NULL COMMENT '删除人用户ID',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  lock_version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  gmt_modified DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  UNIQUE KEY uk_team_code (estab_id, team_code),
  KEY idx_team_parent (estab_id, parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='团队/部门';

DROP TABLE IF EXISTS def_team_user;
CREATE TABLE def_team_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  team_id BIGINT NOT NULL COMMENT '团队ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  role_in_team TINYINT NOT NULL DEFAULT 0 COMMENT '团队内角色 0成员 1负责人',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1有效 2禁用',
  join_time DATETIME(3) DEFAULT NULL COMMENT '加入时间',
  ext_json JSON DEFAULT NULL COMMENT '扩展信息',
  create_by BIGINT DEFAULT NULL COMMENT '创建人用户ID',
  update_by BIGINT DEFAULT NULL COMMENT '更新人用户ID',
  delete_by BIGINT DEFAULT NULL COMMENT '删除人用户ID',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  lock_version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  gmt_modified DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  UNIQUE KEY uk_team_user (team_id, user_id),
  KEY idx_user_team (user_id, team_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='团队成员关系';

DROP TABLE IF EXISTS def_op;
CREATE TABLE def_op (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  op_code VARCHAR(64) NOT NULL COMMENT '操作员编码',
  op_name VARCHAR(64) NOT NULL COMMENT '操作员名称',
  op_type TINYINT NOT NULL DEFAULT 0 COMMENT '操作员类型 0平台运营 1客服 2审计',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 2停用',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  ext_json JSON DEFAULT NULL COMMENT '扩展信息',
  create_by BIGINT DEFAULT NULL COMMENT '创建人用户ID',
  update_by BIGINT DEFAULT NULL COMMENT '更新人用户ID',
  delete_by BIGINT DEFAULT NULL COMMENT '删除人用户ID',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  lock_version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  gmt_modified DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  UNIQUE KEY uk_op_code (op_code),
  KEY idx_op_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='平台操作员档案';

-- ============================
-- RBAC 权限体系
-- ============================
DROP TABLE IF EXISTS scr_system;
CREATE TABLE scr_system (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  system_code VARCHAR(64) NOT NULL COMMENT '系统编码',
  system_name VARCHAR(128) NOT NULL COMMENT '系统名称',
  system_type TINYINT NOT NULL DEFAULT 0 COMMENT '系统类型 0平台 1租户 2业务子系统',
  base_url VARCHAR(255) DEFAULT NULL COMMENT '系统基础URL',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 2停用',
  sort INT NOT NULL DEFAULT 0 COMMENT '排序(升序)',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  ext_json JSON DEFAULT NULL COMMENT '扩展信息',
  create_by BIGINT DEFAULT NULL COMMENT '创建人用户ID',
  update_by BIGINT DEFAULT NULL COMMENT '更新人用户ID',
  delete_by BIGINT DEFAULT NULL COMMENT '删除人用户ID',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  lock_version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  gmt_modified DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  UNIQUE KEY uk_system_code (system_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统定义';

DROP TABLE IF EXISTS scr_role;
CREATE TABLE scr_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  system_id BIGINT NOT NULL COMMENT '系统ID',
  estab_id BIGINT NOT NULL DEFAULT 0 COMMENT '组织ID(平台级为0)',
  role_code VARCHAR(64) NOT NULL COMMENT '角色编码',
  role_name VARCHAR(128) NOT NULL COMMENT '角色名称',
  role_type TINYINT NOT NULL DEFAULT 0 COMMENT '角色类型 0系统内置 1租户内置 2自定义',
  data_scope_type TINYINT NOT NULL DEFAULT 0 COMMENT '数据范围 0全部 1本人 2团队/部门 3自定义',
  parent_role_id BIGINT NOT NULL DEFAULT 0 COMMENT '父角色ID(用于层级角色)',
  is_builtin TINYINT NOT NULL DEFAULT 0 COMMENT '是否内置 1是 0否',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 2停用',
  sort INT NOT NULL DEFAULT 0 COMMENT '排序(升序)',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  ext_json JSON DEFAULT NULL COMMENT '扩展信息',
  create_by BIGINT DEFAULT NULL COMMENT '创建人用户ID',
  update_by BIGINT DEFAULT NULL COMMENT '更新人用户ID',
  delete_by BIGINT DEFAULT NULL COMMENT '删除人用户ID',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  lock_version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  gmt_modified DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  UNIQUE KEY uk_role_code (system_id, estab_id, role_code),
  KEY idx_role_parent (system_id, parent_role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色';

DROP TABLE IF EXISTS scr_role_user;
CREATE TABLE scr_role_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  estab_id BIGINT NOT NULL COMMENT '组织ID',
  granted_by BIGINT DEFAULT NULL COMMENT '授权人用户ID',
  granted_time DATETIME(3) DEFAULT NULL COMMENT '授权时间',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1有效 0撤销',
  create_by BIGINT DEFAULT NULL COMMENT '创建人用户ID',
  update_by BIGINT DEFAULT NULL COMMENT '更新人用户ID',
  delete_by BIGINT DEFAULT NULL COMMENT '删除人用户ID',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  lock_version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  gmt_modified DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  UNIQUE KEY uk_role_user (role_id, user_id, estab_id),
  KEY idx_user_role (user_id, estab_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色-用户关系';

DROP TABLE IF EXISTS scr_menu;
CREATE TABLE scr_menu (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  system_id BIGINT NOT NULL COMMENT '系统ID',
  parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父级菜单ID',
  menu_code VARCHAR(64) NOT NULL COMMENT '菜单编码',
  menu_name VARCHAR(128) NOT NULL COMMENT '菜单名称',
  menu_type TINYINT NOT NULL DEFAULT 1 COMMENT '菜单类型 0目录 1菜单 2按钮',
  path VARCHAR(255) DEFAULT NULL COMMENT '路由路径',
  component VARCHAR(255) DEFAULT NULL COMMENT '前端组件',
  permission_key VARCHAR(128) DEFAULT NULL COMMENT '权限标识(菜单级)',
  icon VARCHAR(64) DEFAULT NULL COMMENT '图标',
  visible TINYINT NOT NULL DEFAULT 1 COMMENT '是否可见 1可见 0隐藏',
  is_frame TINYINT NOT NULL DEFAULT 0 COMMENT '是否外链 1是 0否',
  is_cache TINYINT NOT NULL DEFAULT 0 COMMENT '是否缓存 1是 0否',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 2停用',
  sort INT NOT NULL DEFAULT 0 COMMENT '排序(升序)',
  ext_json JSON DEFAULT NULL COMMENT '扩展信息',
  create_by BIGINT DEFAULT NULL COMMENT '创建人用户ID',
  update_by BIGINT DEFAULT NULL COMMENT '更新人用户ID',
  delete_by BIGINT DEFAULT NULL COMMENT '删除人用户ID',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  lock_version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  gmt_modified DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  UNIQUE KEY uk_menu_code (system_id, menu_code),
  KEY idx_menu_parent (system_id, parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜单';

DROP TABLE IF EXISTS scr_menu_op;
CREATE TABLE scr_menu_op (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  menu_id BIGINT NOT NULL COMMENT '菜单ID',
  op_code VARCHAR(64) NOT NULL COMMENT '操作编码',
  op_name VARCHAR(64) NOT NULL COMMENT '操作名称',
  http_method VARCHAR(16) DEFAULT NULL COMMENT 'HTTP方法',
  path_pattern VARCHAR(255) DEFAULT NULL COMMENT '接口路径(支持通配)',
  permission_key VARCHAR(128) DEFAULT NULL COMMENT '权限标识(操作级)',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 2停用',
  sort INT NOT NULL DEFAULT 0 COMMENT '排序(升序)',
  ext_json JSON DEFAULT NULL COMMENT '扩展信息',
  create_by BIGINT DEFAULT NULL COMMENT '创建人用户ID',
  update_by BIGINT DEFAULT NULL COMMENT '更新人用户ID',
  delete_by BIGINT DEFAULT NULL COMMENT '删除人用户ID',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  lock_version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  gmt_modified DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  UNIQUE KEY uk_menu_op (menu_id, op_code),
  KEY idx_menu_op_perm (permission_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜单操作定义';

DROP TABLE IF EXISTS scr_role_menu;
CREATE TABLE scr_role_menu (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  menu_id BIGINT NOT NULL COMMENT '菜单ID',
  granted_by BIGINT DEFAULT NULL COMMENT '授权人用户ID',
  granted_time DATETIME(3) DEFAULT NULL COMMENT '授权时间',
  create_by BIGINT DEFAULT NULL COMMENT '创建人用户ID',
  update_by BIGINT DEFAULT NULL COMMENT '更新人用户ID',
  delete_by BIGINT DEFAULT NULL COMMENT '删除人用户ID',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  lock_version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  gmt_modified DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  UNIQUE KEY uk_role_menu (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色-菜单授权';

DROP TABLE IF EXISTS scr_role_menu_op;
CREATE TABLE scr_role_menu_op (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  menu_op_id BIGINT NOT NULL COMMENT '菜单操作ID',
  granted_by BIGINT DEFAULT NULL COMMENT '授权人用户ID',
  granted_time DATETIME(3) DEFAULT NULL COMMENT '授权时间',
  create_by BIGINT DEFAULT NULL COMMENT '创建人用户ID',
  update_by BIGINT DEFAULT NULL COMMENT '更新人用户ID',
  delete_by BIGINT DEFAULT NULL COMMENT '删除人用户ID',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  lock_version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  gmt_modified DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  UNIQUE KEY uk_role_menu_op (role_id, menu_op_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色-菜单操作授权';

DROP TABLE IF EXISTS scr_drs;
CREATE TABLE scr_drs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  system_id BIGINT NOT NULL COMMENT '系统ID',
  drs_code VARCHAR(64) NOT NULL COMMENT '数据资源编码',
  drs_name VARCHAR(128) NOT NULL COMMENT '数据资源名称',
  drs_type TINYINT NOT NULL DEFAULT 0 COMMENT '资源类型 0数据库表 1接口资源 2文件 3其他',
  resource_uri VARCHAR(255) DEFAULT NULL COMMENT '资源标识(表名/路径/URI)',
  owner_estab_id BIGINT NOT NULL DEFAULT 0 COMMENT '所属组织ID(平台级为0)',
  data_owner_type TINYINT NOT NULL DEFAULT 0 COMMENT '数据归属 0平台 1租户 2用户',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 2停用',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  ext_json JSON DEFAULT NULL COMMENT '扩展信息',
  create_by BIGINT DEFAULT NULL COMMENT '创建人用户ID',
  update_by BIGINT DEFAULT NULL COMMENT '更新人用户ID',
  delete_by BIGINT DEFAULT NULL COMMENT '删除人用户ID',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  lock_version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  gmt_modified DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  UNIQUE KEY uk_drs_code (system_id, drs_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数据资源定义';

DROP TABLE IF EXISTS scr_drs_interface;
CREATE TABLE scr_drs_interface (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  drs_id BIGINT NOT NULL COMMENT '数据资源ID',
  interface_code VARCHAR(64) NOT NULL COMMENT '接口编码',
  interface_name VARCHAR(128) NOT NULL COMMENT '接口名称',
  http_method VARCHAR(16) DEFAULT NULL COMMENT 'HTTP方法',
  path_pattern VARCHAR(255) DEFAULT NULL COMMENT '接口路径(支持通配)',
  permission_key VARCHAR(128) DEFAULT NULL COMMENT '权限标识(数据接口级)',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 2停用',
  sort INT NOT NULL DEFAULT 0 COMMENT '排序(升序)',
  ext_json JSON DEFAULT NULL COMMENT '扩展信息',
  create_by BIGINT DEFAULT NULL COMMENT '创建人用户ID',
  update_by BIGINT DEFAULT NULL COMMENT '更新人用户ID',
  delete_by BIGINT DEFAULT NULL COMMENT '删除人用户ID',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  lock_version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  gmt_modified DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  UNIQUE KEY uk_drs_interface (drs_id, interface_code),
  KEY idx_drs_interface_perm (permission_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数据资源接口';

DROP TABLE IF EXISTS scr_role_drs;
CREATE TABLE scr_role_drs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  drs_id BIGINT NOT NULL COMMENT '数据资源ID',
  scope_type TINYINT NOT NULL DEFAULT 0 COMMENT '数据范围 0全部 1本人 2团队/部门 3自定义',
  scope_rule JSON DEFAULT NULL COMMENT '自定义规则(JSON表达式)',
  create_by BIGINT DEFAULT NULL COMMENT '创建人用户ID',
  update_by BIGINT DEFAULT NULL COMMENT '更新人用户ID',
  delete_by BIGINT DEFAULT NULL COMMENT '删除人用户ID',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  lock_version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  gmt_modified DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  UNIQUE KEY uk_role_drs (role_id, drs_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色-数据资源授权';

DROP TABLE IF EXISTS scr_role_drs_interface;
CREATE TABLE scr_role_drs_interface (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  drs_interface_id BIGINT NOT NULL COMMENT '数据资源接口ID',
  create_by BIGINT DEFAULT NULL COMMENT '创建人用户ID',
  update_by BIGINT DEFAULT NULL COMMENT '更新人用户ID',
  delete_by BIGINT DEFAULT NULL COMMENT '删除人用户ID',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  lock_version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  gmt_modified DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  UNIQUE KEY uk_role_drs_interface (role_id, drs_interface_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色-数据资源接口授权';

-- ============================
-- 通知与验证码
-- ============================
DROP TABLE IF EXISTS app_notify_template;
CREATE TABLE app_notify_template (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  template_code VARCHAR(64) NOT NULL COMMENT '模板编码',
  template_name VARCHAR(128) NOT NULL COMMENT '模板名称',
  channel_type TINYINT NOT NULL COMMENT '通知通道 1短信 2邮件 3站内信',
  scene_code VARCHAR(32) NOT NULL COMMENT '业务场景编码(login/register/reset等)',
  locale VARCHAR(16) NOT NULL DEFAULT 'zh-CN' COMMENT '语言区域(如 zh-CN/en-US)',
  subject_template VARCHAR(255) DEFAULT NULL COMMENT '主题模板（邮件/站内信）',
  content_template MEDIUMTEXT NOT NULL COMMENT '正文模板(支持HTML)',
  content_type TINYINT NOT NULL DEFAULT 1 COMMENT '内容类型 1HTML 2TEXT',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
  sort INT NOT NULL DEFAULT 0 COMMENT '排序(升序)',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  ext_json JSON DEFAULT NULL COMMENT '扩展信息',
  create_by BIGINT DEFAULT NULL COMMENT '创建人用户ID',
  update_by BIGINT DEFAULT NULL COMMENT '更新人用户ID',
  delete_by BIGINT DEFAULT NULL COMMENT '删除人用户ID',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  lock_version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  gmt_modified DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  UNIQUE KEY uk_notify_tpl (template_code, channel_type, locale),
  KEY idx_notify_tpl_scene (scene_code, channel_type, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通知模板定义';

DROP TABLE IF EXISTS auth_verify_code;
CREATE TABLE auth_verify_code (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  channel_type TINYINT NOT NULL COMMENT '验证码通道 1短信 2邮件',
  scene_code VARCHAR(32) NOT NULL COMMENT '业务场景编码(login/register/reset等)',
  receiver VARCHAR(191) NOT NULL COMMENT '接收目标（手机号/邮箱）',
  code_hash CHAR(64) NOT NULL COMMENT '验证码哈希（建议SHA-256）',
  code_salt VARCHAR(32) DEFAULT NULL COMMENT '验证码盐值',
  expire_at DATETIME(3) NOT NULL COMMENT '过期时间',
  send_status TINYINT NOT NULL DEFAULT 1 COMMENT '发送状态 1成功 0失败',
  verify_status TINYINT NOT NULL DEFAULT 0 COMMENT '核验状态 0待核验 1已核验 2已过期 3已作废',
  verify_fail_count INT NOT NULL DEFAULT 0 COMMENT '核验失败次数',
  verified_time DATETIME(3) DEFAULT NULL COMMENT '核验通过时间',
  provider VARCHAR(64) DEFAULT NULL COMMENT '服务提供商标识',
  template_code VARCHAR(64) DEFAULT NULL COMMENT '模板编码',
  biz_id VARCHAR(128) DEFAULT NULL COMMENT '三方回执ID',
  request_id VARCHAR(64) DEFAULT NULL COMMENT '请求ID',
  ip VARCHAR(64) DEFAULT NULL COMMENT '请求IP',
  user_agent VARCHAR(512) DEFAULT NULL COMMENT 'UserAgent',
  device_id VARCHAR(128) DEFAULT NULL COMMENT '设备ID',
  client_id VARCHAR(64) DEFAULT NULL COMMENT '客户端ID',
  ext_json JSON DEFAULT NULL COMMENT '扩展信息',
  create_by BIGINT DEFAULT NULL COMMENT '创建人用户ID',
  update_by BIGINT DEFAULT NULL COMMENT '更新人用户ID',
  delete_by BIGINT DEFAULT NULL COMMENT '删除人用户ID',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  lock_version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  gmt_modified DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  KEY idx_verify_lookup (channel_type, scene_code, receiver, verify_status, expire_at),
  KEY idx_verify_time (gmt_create)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='验证码发放与核验记录';

DROP TABLE IF EXISTS log_notify;
CREATE TABLE log_notify (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  channel_type TINYINT NOT NULL COMMENT '通知通道 1短信 2邮件 3站内信',
  scene_code VARCHAR(32) NOT NULL COMMENT '业务场景编码(login/register/reset等)',
  receiver VARCHAR(191) NOT NULL COMMENT '接收目标（手机号/邮箱/用户ID）',
  subject VARCHAR(255) DEFAULT NULL COMMENT '通知主题（邮件/站内信）',
  content_digest VARCHAR(255) DEFAULT NULL COMMENT '内容摘要（脱敏）',
  provider VARCHAR(64) DEFAULT NULL COMMENT '服务提供商标识',
  template_code VARCHAR(64) DEFAULT NULL COMMENT '模板编码',
  biz_id VARCHAR(128) DEFAULT NULL COMMENT '三方回执ID',
  send_status TINYINT NOT NULL DEFAULT 1 COMMENT '发送状态 1成功 0失败',
  error_message VARCHAR(255) DEFAULT NULL COMMENT '失败原因',
  request_id VARCHAR(64) DEFAULT NULL COMMENT '请求ID',
  ip VARCHAR(64) DEFAULT NULL COMMENT '请求IP',
  user_agent VARCHAR(512) DEFAULT NULL COMMENT 'UserAgent',
  user_id BIGINT DEFAULT NULL COMMENT '用户ID',
  estab_id BIGINT DEFAULT NULL COMMENT '组织ID',
  ext_json JSON DEFAULT NULL COMMENT '扩展信息',
  create_by BIGINT DEFAULT NULL COMMENT '创建人用户ID',
  update_by BIGINT DEFAULT NULL COMMENT '更新人用户ID',
  delete_by BIGINT DEFAULT NULL COMMENT '删除人用户ID',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  lock_version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  gmt_modified DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  KEY idx_notify_channel_time (channel_type, gmt_create),
  KEY idx_notify_receiver_time (receiver, gmt_create),
  KEY idx_notify_status_time (send_status, gmt_create)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='统一通知发送日志';

-- ============================
-- 日志
-- ============================
DROP TABLE IF EXISTS log_login;
CREATE TABLE log_login (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  user_id BIGINT DEFAULT NULL COMMENT '用户ID',
  estab_id BIGINT DEFAULT NULL COMMENT '组织ID',
  identity_id BIGINT DEFAULT NULL COMMENT '身份ID',
  login_type TINYINT NOT NULL COMMENT '登录方式 1用户名密码 2手机号短信 3邮箱密码 4邮箱验证码 5微信扫码 6OAuth/OIDC 7SAML 8TOTP',
  source_type TINYINT NOT NULL COMMENT '登录来源 1Web 2App 3小程序 4API',
  success TINYINT NOT NULL DEFAULT 1 COMMENT '是否成功 1成功 0失败',
  failure_reason VARCHAR(255) DEFAULT NULL COMMENT '失败原因',
  ip VARCHAR(64) DEFAULT NULL COMMENT 'IP地址',
  user_agent VARCHAR(512) DEFAULT NULL COMMENT 'UserAgent',
  device_id VARCHAR(128) DEFAULT NULL COMMENT '设备ID',
  client_id VARCHAR(64) DEFAULT NULL COMMENT '客户端ID',
  request_id VARCHAR(64) DEFAULT NULL COMMENT '请求ID',
  ext_json JSON DEFAULT NULL COMMENT '扩展信息',
  create_by BIGINT DEFAULT NULL COMMENT '创建人用户ID',
  update_by BIGINT DEFAULT NULL COMMENT '更新人用户ID',
  delete_by BIGINT DEFAULT NULL COMMENT '删除人用户ID',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  lock_version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间(即登录时间)',
  gmt_modified DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  KEY idx_login_user_time (user_id, gmt_create),
  KEY idx_login_estab_time (estab_id, gmt_create),
  KEY idx_login_success_time (success, gmt_create)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='登录日志';

DROP TABLE IF EXISTS log_operate;
CREATE TABLE log_operate (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  user_id BIGINT DEFAULT NULL COMMENT '用户ID',
  estab_id BIGINT DEFAULT NULL COMMENT '组织ID',
  module_code VARCHAR(64) DEFAULT NULL COMMENT '模块编码',
  operation VARCHAR(64) DEFAULT NULL COMMENT '操作名称/动作',
  target_type VARCHAR(64) DEFAULT NULL COMMENT '目标类型',
  target_id VARCHAR(64) DEFAULT NULL COMMENT '目标ID',
  success TINYINT NOT NULL DEFAULT 1 COMMENT '是否成功 1成功 0失败',
  fail_reason VARCHAR(255) DEFAULT NULL COMMENT '失败原因',
  request_method VARCHAR(16) DEFAULT NULL COMMENT 'HTTP方法',
  request_path VARCHAR(255) DEFAULT NULL COMMENT '请求路径',
  request_params JSON DEFAULT NULL COMMENT '请求参数(脱敏后)',
  response_body JSON DEFAULT NULL COMMENT '响应摘要(脱敏后)',
  ip VARCHAR(64) DEFAULT NULL COMMENT 'IP地址',
  user_agent VARCHAR(512) DEFAULT NULL COMMENT 'UserAgent',
  duration_ms INT DEFAULT NULL COMMENT '耗时(ms)',
  trace_id VARCHAR(64) DEFAULT NULL COMMENT '链路追踪ID',
  span_id VARCHAR(64) DEFAULT NULL COMMENT '链路SpanID',
  ext_json JSON DEFAULT NULL COMMENT '扩展信息',
  create_by BIGINT DEFAULT NULL COMMENT '创建人用户ID',
  update_by BIGINT DEFAULT NULL COMMENT '更新人用户ID',
  delete_by BIGINT DEFAULT NULL COMMENT '删除人用户ID',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  lock_version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  gmt_modified DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  KEY idx_op_user_time (user_id, gmt_create),
  KEY idx_op_estab_time (estab_id, gmt_create),
  KEY idx_op_success_time (success, gmt_create)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志';

DROP TABLE IF EXISTS log_error;
CREATE TABLE log_error (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  service_name VARCHAR(64) DEFAULT NULL COMMENT '服务名',
  error_code VARCHAR(64) DEFAULT NULL COMMENT '错误编码',
  error_type VARCHAR(64) DEFAULT NULL COMMENT '错误类型',
  error_level TINYINT NOT NULL DEFAULT 1 COMMENT '错误级别 1ERROR 2WARN 3FATAL',
  message VARCHAR(512) DEFAULT NULL COMMENT '错误信息',
  stack_trace MEDIUMTEXT DEFAULT NULL COMMENT '堆栈信息',
  request_id VARCHAR(64) DEFAULT NULL COMMENT '请求ID',
  trace_id VARCHAR(64) DEFAULT NULL COMMENT '链路追踪ID',
  request_method VARCHAR(16) DEFAULT NULL COMMENT 'HTTP方法',
  request_path VARCHAR(255) DEFAULT NULL COMMENT '请求路径',
  request_params JSON DEFAULT NULL COMMENT '请求参数(脱敏后)',
  user_id BIGINT DEFAULT NULL COMMENT '用户ID',
  estab_id BIGINT DEFAULT NULL COMMENT '组织ID',
  ext_json JSON DEFAULT NULL COMMENT '扩展信息',
  create_by BIGINT DEFAULT NULL COMMENT '创建人用户ID',
  update_by BIGINT DEFAULT NULL COMMENT '更新人用户ID',
  delete_by BIGINT DEFAULT NULL COMMENT '删除人用户ID',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  lock_version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间(即错误时间)',
  gmt_modified DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  KEY idx_error_time (gmt_create),
  KEY idx_error_service (service_name, gmt_create)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='错误日志';

SET FOREIGN_KEY_CHECKS = 1;
