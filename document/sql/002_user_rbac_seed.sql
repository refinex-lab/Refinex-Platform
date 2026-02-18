-- ============================================================================
-- Refinex Platform 用户与 RBAC 初始化数据（幂等）
-- 说明：
-- 1) 本脚本依赖 document/sql/001_user_rbac_schema.sql 已执行完成。
-- 2) 目标：初始化超级管理员 Refinex，并完成企业/团队/部门/角色绑定。
-- 3) 本脚本使用 UPSERT（ON DUPLICATE KEY UPDATE）与 NOT EXISTS，支持重复执行。
-- ============================================================================

USE refinex_platform;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

START TRANSACTION;

-- ----------------------------------------
-- 常量
-- ----------------------------------------
SET @seed_operator_id := 0;
SET @seed_now := NOW(3);
SET @seed_password_hash := '$2y$10$.4mReaBZKJN9VfE9AxAotuNFz86HrxxVTJBg7sopQaXT1joqZ6QWq';
-- 默认密码（仅用于初始化）: Refinex@2026!
-- 强烈建议：首次登录后立即修改密码，并轮换为企业安全策略口令。

-- ----------------------------------------
-- 通用字典（值集）初始化
-- ----------------------------------------
INSERT INTO app_valueset
(
  set_code, set_name, status, sort, description,
  create_by, update_by, deleted, lock_version
)
VALUES
  ('gender', '性别', 1, 10, '用户性别', @seed_operator_id, @seed_operator_id, 0, 0),
  ('user_status', '用户状态', 1, 20, '用户状态枚举', @seed_operator_id, @seed_operator_id, 0, 0),
  ('user_type', '用户类型', 1, 30, '用户类型枚举', @seed_operator_id, @seed_operator_id, 0, 0),
  ('login_type', '登录方式', 1, 40, '登录方式枚举', @seed_operator_id, @seed_operator_id, 0, 0),
  ('identity_type', '身份类型', 1, 50, '登录身份类型', @seed_operator_id, @seed_operator_id, 0, 0),
  ('login_source', '登录来源', 1, 60, '登录来源枚举', @seed_operator_id, @seed_operator_id, 0, 0),
  ('role_type', '角色类型', 1, 70, '角色类型枚举', @seed_operator_id, @seed_operator_id, 0, 0),
  ('menu_type', '菜单类型', 1, 80, '菜单类型枚举', @seed_operator_id, @seed_operator_id, 0, 0),
  ('data_scope', '数据范围', 1, 90, '数据范围枚举', @seed_operator_id, @seed_operator_id, 0, 0),
  ('estab_type', '组织类型', 1, 100, '组织类型枚举', @seed_operator_id, @seed_operator_id, 0, 0)
ON DUPLICATE KEY UPDATE
  set_name = VALUES(set_name),
  status = VALUES(status),
  sort = VALUES(sort),
  description = VALUES(description),
  deleted = 0,
  update_by = VALUES(update_by);

INSERT INTO app_value
(
  set_code, value_code, value_name, value_desc, status, is_default, sort,
  create_by, update_by, deleted, lock_version
)
VALUES
  ('gender', '0', '未知', NULL, 1, 1, 10, @seed_operator_id, @seed_operator_id, 0, 0),
  ('gender', '1', '男', NULL, 1, 0, 20, @seed_operator_id, @seed_operator_id, 0, 0),
  ('gender', '2', '女', NULL, 1, 0, 30, @seed_operator_id, @seed_operator_id, 0, 0),
  ('gender', '3', '其他', NULL, 1, 0, 40, @seed_operator_id, @seed_operator_id, 0, 0),

  ('user_status', '1', '启用', NULL, 1, 1, 10, @seed_operator_id, @seed_operator_id, 0, 0),
  ('user_status', '2', '停用', NULL, 1, 0, 20, @seed_operator_id, @seed_operator_id, 0, 0),
  ('user_status', '3', '锁定', NULL, 1, 0, 30, @seed_operator_id, @seed_operator_id, 0, 0),

  ('user_type', '0', '平台', NULL, 1, 1, 10, @seed_operator_id, @seed_operator_id, 0, 0),
  ('user_type', '1', '租户', NULL, 1, 0, 20, @seed_operator_id, @seed_operator_id, 0, 0),
  ('user_type', '2', '合作方', NULL, 1, 0, 30, @seed_operator_id, @seed_operator_id, 0, 0),

  ('login_type', '1', '用户名密码', NULL, 1, 0, 10, @seed_operator_id, @seed_operator_id, 0, 0),
  ('login_type', '2', '手机号短信', NULL, 1, 1, 20, @seed_operator_id, @seed_operator_id, 0, 0),
  ('login_type', '3', '邮箱密码', NULL, 1, 0, 30, @seed_operator_id, @seed_operator_id, 0, 0),
  ('login_type', '4', '邮箱验证码', NULL, 1, 0, 40, @seed_operator_id, @seed_operator_id, 0, 0),
  ('login_type', '5', '微信扫码', NULL, 1, 0, 50, @seed_operator_id, @seed_operator_id, 0, 0),
  ('login_type', '6', 'OAuth/OIDC', NULL, 1, 0, 60, @seed_operator_id, @seed_operator_id, 0, 0),
  ('login_type', '7', 'SAML', NULL, 1, 0, 70, @seed_operator_id, @seed_operator_id, 0, 0),
  ('login_type', '8', 'TOTP', NULL, 1, 0, 80, @seed_operator_id, @seed_operator_id, 0, 0),

  ('identity_type', '1', '用户名密码', NULL, 1, 0, 10, @seed_operator_id, @seed_operator_id, 0, 0),
  ('identity_type', '2', '手机号短信', NULL, 1, 1, 20, @seed_operator_id, @seed_operator_id, 0, 0),
  ('identity_type', '3', '邮箱密码', NULL, 1, 0, 30, @seed_operator_id, @seed_operator_id, 0, 0),
  ('identity_type', '4', '邮箱验证码', NULL, 1, 0, 40, @seed_operator_id, @seed_operator_id, 0, 0),
  ('identity_type', '5', '微信扫码', NULL, 1, 0, 50, @seed_operator_id, @seed_operator_id, 0, 0),
  ('identity_type', '6', 'OIDC', NULL, 1, 0, 60, @seed_operator_id, @seed_operator_id, 0, 0),
  ('identity_type', '7', 'SAML', NULL, 1, 0, 70, @seed_operator_id, @seed_operator_id, 0, 0),
  ('identity_type', '8', 'TOTP', NULL, 1, 0, 80, @seed_operator_id, @seed_operator_id, 0, 0),

  ('login_source', '1', 'Web', NULL, 1, 1, 10, @seed_operator_id, @seed_operator_id, 0, 0),
  ('login_source', '2', 'App', NULL, 1, 0, 20, @seed_operator_id, @seed_operator_id, 0, 0),
  ('login_source', '3', '小程序', NULL, 1, 0, 30, @seed_operator_id, @seed_operator_id, 0, 0),
  ('login_source', '4', 'API', NULL, 1, 0, 40, @seed_operator_id, @seed_operator_id, 0, 0),

  ('role_type', '0', '系统内置', NULL, 1, 1, 10, @seed_operator_id, @seed_operator_id, 0, 0),
  ('role_type', '1', '租户内置', NULL, 1, 0, 20, @seed_operator_id, @seed_operator_id, 0, 0),
  ('role_type', '2', '自定义', NULL, 1, 0, 30, @seed_operator_id, @seed_operator_id, 0, 0),

  ('menu_type', '0', '目录', NULL, 1, 0, 10, @seed_operator_id, @seed_operator_id, 0, 0),
  ('menu_type', '1', '菜单', NULL, 1, 1, 20, @seed_operator_id, @seed_operator_id, 0, 0),
  ('menu_type', '2', '按钮', NULL, 1, 0, 30, @seed_operator_id, @seed_operator_id, 0, 0),

  ('data_scope', '0', '全部', NULL, 1, 1, 10, @seed_operator_id, @seed_operator_id, 0, 0),
  ('data_scope', '1', '本人', NULL, 1, 0, 20, @seed_operator_id, @seed_operator_id, 0, 0),
  ('data_scope', '2', '团队/部门', NULL, 1, 0, 30, @seed_operator_id, @seed_operator_id, 0, 0),
  ('data_scope', '3', '自定义', NULL, 1, 0, 40, @seed_operator_id, @seed_operator_id, 0, 0),

  ('estab_type', '0', '平台', NULL, 1, 0, 10, @seed_operator_id, @seed_operator_id, 0, 0),
  ('estab_type', '1', '租户', NULL, 1, 1, 20, @seed_operator_id, @seed_operator_id, 0, 0),
  ('estab_type', '2', '合作方', NULL, 1, 0, 30, @seed_operator_id, @seed_operator_id, 0, 0)
ON DUPLICATE KEY UPDATE
  value_name = VALUES(value_name),
  value_desc = VALUES(value_desc),
  status = VALUES(status),
  is_default = VALUES(is_default),
  sort = VALUES(sort),
  deleted = 0,
  update_by = VALUES(update_by);

-- ----------------------------------------
-- 系统定义（平台 + 租户）
-- ----------------------------------------
INSERT INTO scr_system
(
  system_code, system_name, system_type, base_url, status, sort, remark,
  create_by, update_by, deleted, lock_version
)
VALUES
  ('platform', '平台管理系统', 0, NULL, 1, 1, '平台级后台系统', @seed_operator_id, @seed_operator_id, 0, 0),
  ('tenant',   '租户业务系统', 1, NULL, 1, 2, '租户级业务系统', @seed_operator_id, @seed_operator_id, 0, 0)
ON DUPLICATE KEY UPDATE
  system_name = VALUES(system_name),
  system_type = VALUES(system_type),
  status = VALUES(status),
  sort = VALUES(sort),
  remark = VALUES(remark),
  deleted = 0,
  update_by = VALUES(update_by);

SELECT id INTO @system_platform_id FROM scr_system WHERE system_code = 'platform' AND deleted = 0 LIMIT 1;
SELECT id INTO @system_tenant_id FROM scr_system WHERE system_code = 'tenant' AND deleted = 0 LIMIT 1;

-- ----------------------------------------
-- 企业（组织）初始化
-- ----------------------------------------
INSERT INTO def_estab
(
  estab_code, estab_name, estab_short_name, estab_type, status,
  owner_user_id, contact_name, contact_phone, contact_email,
  remark, ext_json,
  create_by, update_by, deleted, lock_version
)
VALUES
(
  'ESTAB_REFINEX',
  'Refinex 科技有限公司',
  'Refinex',
  1,
  1,
  NULL,
  'Refinex',
  '13386271152',
  'refinex@163.com',
  '系统初始化默认企业',
  JSON_OBJECT('seed', '002_user_rbac_seed'),
  @seed_operator_id, @seed_operator_id, 0, 0
)
ON DUPLICATE KEY UPDATE
  estab_name = VALUES(estab_name),
  estab_short_name = VALUES(estab_short_name),
  estab_type = VALUES(estab_type),
  status = VALUES(status),
  contact_name = VALUES(contact_name),
  contact_phone = VALUES(contact_phone),
  contact_email = VALUES(contact_email),
  remark = VALUES(remark),
  deleted = 0,
  update_by = VALUES(update_by);

SELECT id INTO @estab_id FROM def_estab WHERE estab_code = 'ESTAB_REFINEX' AND deleted = 0 LIMIT 1;

INSERT INTO def_estab_auth_policy
(
  estab_id,
  password_login_enabled, sms_login_enabled, email_login_enabled, wechat_login_enabled,
  mfa_required, mfa_methods, password_min_len, password_strength, password_expire_days,
  login_fail_threshold, lock_minutes, session_timeout_minutes,
  remark,
  create_by, update_by, deleted, lock_version
)
VALUES
(
  @estab_id,
  1, 1, 1, 0,
  0, NULL, 8, 1, 0,
  5, 30, 120,
  '系统初始化默认认证策略',
  @seed_operator_id, @seed_operator_id, 0, 0
)
ON DUPLICATE KEY UPDATE
  password_login_enabled = VALUES(password_login_enabled),
  sms_login_enabled = VALUES(sms_login_enabled),
  email_login_enabled = VALUES(email_login_enabled),
  wechat_login_enabled = VALUES(wechat_login_enabled),
  mfa_required = VALUES(mfa_required),
  mfa_methods = VALUES(mfa_methods),
  password_min_len = VALUES(password_min_len),
  password_strength = VALUES(password_strength),
  password_expire_days = VALUES(password_expire_days),
  login_fail_threshold = VALUES(login_fail_threshold),
  lock_minutes = VALUES(lock_minutes),
  session_timeout_minutes = VALUES(session_timeout_minutes),
  deleted = 0,
  update_by = VALUES(update_by);

INSERT INTO def_estab_address
(
  estab_id, addr_type, country_code,
  province_name, city_name, district_name,
  address_line1, is_default, remark,
  create_by, update_by, deleted, lock_version
)
SELECT
  @estab_id, 2, 'CN',
  '上海市', '上海市', '浦东新区',
  '张江高科技园区', 1, '系统初始化默认办公地址',
  @seed_operator_id, @seed_operator_id, 0, 0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1
  FROM def_estab_address
  WHERE estab_id = @estab_id
    AND addr_type = 2
    AND is_default = 1
    AND deleted = 0
);

-- ----------------------------------------
-- 超级管理员用户初始化
-- ----------------------------------------
-- 年龄 25：以 2026-02-18 为参考，落库生日取 2001-01-01（年龄按实时日期自动变化）
INSERT INTO def_user
(
  user_code, username, display_name, nickname, avatar_url,
  gender, birthday, user_type, status,
  primary_estab_id, primary_phone, phone_verified, primary_email, email_verified,
  login_fail_count, lock_until,
  remark, ext_json,
  create_by, update_by, deleted, lock_version
)
VALUES
(
  'U_REFINEX_SUPER_ADMIN',
  'refinex',
  'Refinex',
  'Refinex',
  NULL,
  1,
  '2001-01-01',
  0,
  1,
  @estab_id,
  '13386271152',
  1,
  'refinex@163.com',
  1,
  0,
  NULL,
  '系统初始化超级管理员',
  JSON_OBJECT('seed', '002_user_rbac_seed', 'age_at_seed', 25, 'age_reference_date', '2026-02-18'),
  @seed_operator_id, @seed_operator_id, 0, 0
)
ON DUPLICATE KEY UPDATE
  display_name = VALUES(display_name),
  nickname = VALUES(nickname),
  gender = VALUES(gender),
  birthday = VALUES(birthday),
  user_type = VALUES(user_type),
  status = VALUES(status),
  primary_estab_id = VALUES(primary_estab_id),
  primary_phone = VALUES(primary_phone),
  phone_verified = VALUES(phone_verified),
  primary_email = VALUES(primary_email),
  email_verified = VALUES(email_verified),
  remark = VALUES(remark),
  ext_json = VALUES(ext_json),
  deleted = 0,
  update_by = VALUES(update_by);

SELECT id INTO @super_user_id FROM def_user WHERE username = 'refinex' AND deleted = 0 LIMIT 1;

UPDATE def_estab
SET owner_user_id = @super_user_id,
    update_by = @super_user_id,
    deleted = 0
WHERE id = @estab_id;

UPDATE def_user
SET primary_estab_id = @estab_id,
    update_by = @super_user_id,
    deleted = 0
WHERE id = @super_user_id;

-- 身份 1：用户名密码（用于后台首次登录）
INSERT INTO def_user_identity
(
  user_id, identity_type, identifier, issuer,
  credential, credential_alg,
  is_primary, verified, verified_at, bind_time,
  status, ext_json,
  create_by, update_by, deleted, lock_version
)
VALUES
(
  @super_user_id, 1, 'refinex', '',
  @seed_password_hash, 'bcrypt',
  1, 1, @seed_now, @seed_now,
  1, JSON_OBJECT('seed', '002_user_rbac_seed'),
  @super_user_id, @super_user_id, 0, 0
)
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  credential = VALUES(credential),
  credential_alg = VALUES(credential_alg),
  is_primary = VALUES(is_primary),
  verified = VALUES(verified),
  verified_at = VALUES(verified_at),
  bind_time = VALUES(bind_time),
  status = VALUES(status),
  deleted = 0,
  update_by = VALUES(update_by);

-- 身份 2：手机号短信登录
INSERT INTO def_user_identity
(
  user_id, identity_type, identifier, issuer,
  credential, credential_alg,
  is_primary, verified, verified_at, bind_time,
  status, ext_json,
  create_by, update_by, deleted, lock_version
)
VALUES
(
  @super_user_id, 2, '13386271152', '',
  NULL, NULL,
  0, 1, @seed_now, @seed_now,
  1, JSON_OBJECT('seed', '002_user_rbac_seed'),
  @super_user_id, @super_user_id, 0, 0
)
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  verified = VALUES(verified),
  verified_at = VALUES(verified_at),
  bind_time = VALUES(bind_time),
  status = VALUES(status),
  deleted = 0,
  update_by = VALUES(update_by);

-- 身份 3：邮箱密码登录
INSERT INTO def_user_identity
(
  user_id, identity_type, identifier, issuer,
  credential, credential_alg,
  is_primary, verified, verified_at, bind_time,
  status, ext_json,
  create_by, update_by, deleted, lock_version
)
VALUES
(
  @super_user_id, 3, 'refinex@163.com', '',
  @seed_password_hash, 'bcrypt',
  0, 1, @seed_now, @seed_now,
  1, JSON_OBJECT('seed', '002_user_rbac_seed'),
  @super_user_id, @super_user_id, 0, 0
)
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  credential = VALUES(credential),
  credential_alg = VALUES(credential_alg),
  verified = VALUES(verified),
  verified_at = VALUES(verified_at),
  bind_time = VALUES(bind_time),
  status = VALUES(status),
  deleted = 0,
  update_by = VALUES(update_by);

-- 企业成员关系（超级管理员）
INSERT INTO def_estab_user
(
  estab_id, user_id, member_type, is_admin, status, join_time, leave_time, position_title,
  ext_json, create_by, update_by, deleted, lock_version
)
VALUES
(
  @estab_id, @super_user_id, 0, 1, 1, @seed_now, NULL, '创始人/超级管理员',
  JSON_OBJECT('seed', '002_user_rbac_seed'),
  @super_user_id, @super_user_id, 0, 0
)
ON DUPLICATE KEY UPDATE
  member_type = VALUES(member_type),
  is_admin = VALUES(is_admin),
  status = VALUES(status),
  join_time = VALUES(join_time),
  leave_time = VALUES(leave_time),
  position_title = VALUES(position_title),
  deleted = 0,
  update_by = VALUES(update_by);

-- ----------------------------------------
-- 团队/部门初始化并绑定用户
-- ----------------------------------------
INSERT INTO def_team
(
  estab_id, team_code, team_name, parent_id, leader_user_id, status, sort, remark,
  ext_json, create_by, update_by, deleted, lock_version
)
VALUES
(
  @estab_id, 'TEAM_REFINEX', 'Refinex 团队', 0, @super_user_id, 1, 10, '系统初始化默认团队',
  JSON_OBJECT('seed', '002_user_rbac_seed'),
  @super_user_id, @super_user_id, 0, 0
)
ON DUPLICATE KEY UPDATE
  team_name = VALUES(team_name),
  leader_user_id = VALUES(leader_user_id),
  status = VALUES(status),
  sort = VALUES(sort),
  remark = VALUES(remark),
  deleted = 0,
  update_by = VALUES(update_by);

SELECT id INTO @team_root_id
FROM def_team
WHERE estab_id = @estab_id AND team_code = 'TEAM_REFINEX' AND deleted = 0
LIMIT 1;

INSERT INTO def_team
(
  estab_id, team_code, team_name, parent_id, leader_user_id, status, sort, remark,
  ext_json, create_by, update_by, deleted, lock_version
)
VALUES
(
  @estab_id, 'DEPT_PLATFORM_RD', '平台研发部', IFNULL(@team_root_id, 0), @super_user_id, 1, 20, '系统初始化默认部门',
  JSON_OBJECT('seed', '002_user_rbac_seed'),
  @super_user_id, @super_user_id, 0, 0
)
ON DUPLICATE KEY UPDATE
  team_name = VALUES(team_name),
  parent_id = VALUES(parent_id),
  leader_user_id = VALUES(leader_user_id),
  status = VALUES(status),
  sort = VALUES(sort),
  remark = VALUES(remark),
  deleted = 0,
  update_by = VALUES(update_by);

SELECT id INTO @team_dept_id
FROM def_team
WHERE estab_id = @estab_id AND team_code = 'DEPT_PLATFORM_RD' AND deleted = 0
LIMIT 1;

INSERT INTO def_team_user
(
  team_id, user_id, role_in_team, status, join_time,
  ext_json, create_by, update_by, deleted, lock_version
)
VALUES
(
  @team_root_id, @super_user_id, 1, 1, @seed_now,
  JSON_OBJECT('seed', '002_user_rbac_seed', 'scope', 'team'),
  @super_user_id, @super_user_id, 0, 0
)
ON DUPLICATE KEY UPDATE
  role_in_team = VALUES(role_in_team),
  status = VALUES(status),
  join_time = VALUES(join_time),
  deleted = 0,
  update_by = VALUES(update_by);

INSERT INTO def_team_user
(
  team_id, user_id, role_in_team, status, join_time,
  ext_json, create_by, update_by, deleted, lock_version
)
VALUES
(
  @team_dept_id, @super_user_id, 1, 1, @seed_now,
  JSON_OBJECT('seed', '002_user_rbac_seed', 'scope', 'department'),
  @super_user_id, @super_user_id, 0, 0
)
ON DUPLICATE KEY UPDATE
  role_in_team = VALUES(role_in_team),
  status = VALUES(status),
  join_time = VALUES(join_time),
  deleted = 0,
  update_by = VALUES(update_by);

-- 平台操作员档案
INSERT INTO def_op
(
  user_id, op_code, op_name, op_type, status, remark, ext_json,
  create_by, update_by, deleted, lock_version
)
VALUES
(
  @super_user_id, 'OP_REFINEX_SUPER_ADMIN', 'Refinex 平台超管', 0, 1, '系统初始化超级管理员操作员档案',
  JSON_OBJECT('seed', '002_user_rbac_seed'),
  @super_user_id, @super_user_id, 0, 0
)
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  op_name = VALUES(op_name),
  op_type = VALUES(op_type),
  status = VALUES(status),
  remark = VALUES(remark),
  deleted = 0,
  update_by = VALUES(update_by);

-- ----------------------------------------
-- 角色初始化（所有人员 + 超级管理员）
-- ----------------------------------------
-- 平台级：超级管理员
INSERT INTO scr_role
(
  system_id, estab_id, role_code, role_name, role_type,
  data_scope_type, parent_role_id, is_builtin, status, sort, remark, ext_json,
  create_by, update_by, deleted, lock_version
)
VALUES
(
  @system_platform_id, 0, 'PLATFORM_SUPER_ADMIN', '平台超级管理员', 0,
  0, 0, 1, 1, 10, '系统初始化内置角色',
  JSON_OBJECT('seed', '002_user_rbac_seed'),
  @super_user_id, @super_user_id, 0, 0
)
ON DUPLICATE KEY UPDATE
  role_name = VALUES(role_name),
  role_type = VALUES(role_type),
  data_scope_type = VALUES(data_scope_type),
  is_builtin = VALUES(is_builtin),
  status = VALUES(status),
  sort = VALUES(sort),
  remark = VALUES(remark),
  deleted = 0,
  update_by = VALUES(update_by);

-- 租户级：超级管理员（企业管理员）
INSERT INTO scr_role
(
  system_id, estab_id, role_code, role_name, role_type,
  data_scope_type, parent_role_id, is_builtin, status, sort, remark, ext_json,
  create_by, update_by, deleted, lock_version
)
VALUES
(
  @system_tenant_id, @estab_id, 'TENANT_ADMIN', '企业超级管理员', 1,
  0, 0, 1, 1, 10, '系统初始化内置角色',
  JSON_OBJECT('seed', '002_user_rbac_seed'),
  @super_user_id, @super_user_id, 0, 0
)
ON DUPLICATE KEY UPDATE
  role_name = VALUES(role_name),
  role_type = VALUES(role_type),
  data_scope_type = VALUES(data_scope_type),
  is_builtin = VALUES(is_builtin),
  status = VALUES(status),
  sort = VALUES(sort),
  remark = VALUES(remark),
  deleted = 0,
  update_by = VALUES(update_by);

-- 租户级：所有人员默认角色
INSERT INTO scr_role
(
  system_id, estab_id, role_code, role_name, role_type,
  data_scope_type, parent_role_id, is_builtin, status, sort, remark, ext_json,
  create_by, update_by, deleted, lock_version
)
VALUES
(
  @system_tenant_id, @estab_id, 'TENANT_USER', '所有人员', 1,
  1, 0, 1, 1, 20, '系统初始化内置角色（默认成员角色）',
  JSON_OBJECT('seed', '002_user_rbac_seed'),
  @super_user_id, @super_user_id, 0, 0
)
ON DUPLICATE KEY UPDATE
  role_name = VALUES(role_name),
  role_type = VALUES(role_type),
  data_scope_type = VALUES(data_scope_type),
  is_builtin = VALUES(is_builtin),
  status = VALUES(status),
  sort = VALUES(sort),
  remark = VALUES(remark),
  deleted = 0,
  update_by = VALUES(update_by);

SELECT id INTO @role_platform_super_id
FROM scr_role
WHERE system_id = @system_platform_id AND estab_id = 0 AND role_code = 'PLATFORM_SUPER_ADMIN' AND deleted = 0
LIMIT 1;

SELECT id INTO @role_tenant_admin_id
FROM scr_role
WHERE system_id = @system_tenant_id AND estab_id = @estab_id AND role_code = 'TENANT_ADMIN' AND deleted = 0
LIMIT 1;

SELECT id INTO @role_tenant_user_id
FROM scr_role
WHERE system_id = @system_tenant_id AND estab_id = @estab_id AND role_code = 'TENANT_USER' AND deleted = 0
LIMIT 1;

-- 超级管理员绑定角色：平台超管 + 企业超管 + 企业所有人员
INSERT INTO scr_role_user
(
  role_id, user_id, estab_id, granted_by, granted_time, status,
  create_by, update_by, deleted, lock_version
)
VALUES
(
  @role_platform_super_id, @super_user_id, 0, @super_user_id, @seed_now, 1,
  @super_user_id, @super_user_id, 0, 0
)
ON DUPLICATE KEY UPDATE
  status = VALUES(status),
  granted_by = VALUES(granted_by),
  granted_time = VALUES(granted_time),
  deleted = 0,
  update_by = VALUES(update_by);

INSERT INTO scr_role_user
(
  role_id, user_id, estab_id, granted_by, granted_time, status,
  create_by, update_by, deleted, lock_version
)
VALUES
(
  @role_tenant_admin_id, @super_user_id, @estab_id, @super_user_id, @seed_now, 1,
  @super_user_id, @super_user_id, 0, 0
)
ON DUPLICATE KEY UPDATE
  status = VALUES(status),
  granted_by = VALUES(granted_by),
  granted_time = VALUES(granted_time),
  deleted = 0,
  update_by = VALUES(update_by);

INSERT INTO scr_role_user
(
  role_id, user_id, estab_id, granted_by, granted_time, status,
  create_by, update_by, deleted, lock_version
)
VALUES
(
  @role_tenant_user_id, @super_user_id, @estab_id, @super_user_id, @seed_now, 1,
  @super_user_id, @super_user_id, 0, 0
)
ON DUPLICATE KEY UPDATE
  status = VALUES(status),
  granted_by = VALUES(granted_by),
  granted_time = VALUES(granted_time),
  deleted = 0,
  update_by = VALUES(update_by);

-- ----------------------------------------
-- 通用菜单与操作初始化
-- ----------------------------------------
INSERT INTO scr_menu
(
  system_id, parent_id, menu_code, menu_name, menu_type, path, component, permission_key,
  icon, visible, is_frame, is_cache, status, sort, ext_json,
  create_by, update_by, deleted, lock_version
)
VALUES
  (@system_platform_id, 0, 'PLATFORM_ROOT', '平台管理', 0, '/platform', NULL, NULL, 'setting', 1, 0, 0, 1, 10, JSON_OBJECT('seed', '002_user_rbac_seed'), @super_user_id, @super_user_id, 0, 0),
  (@system_platform_id, 0, 'PLATFORM_USER_MGMT', '平台用户管理', 1, '/platform/users', 'platform/user/index', 'platform:user:view', 'user', 1, 0, 0, 1, 20, JSON_OBJECT('seed', '002_user_rbac_seed'), @super_user_id, @super_user_id, 0, 0),
  (@system_platform_id, 0, 'PLATFORM_ROLE_MGMT', '平台角色管理', 1, '/platform/roles', 'platform/role/index', 'platform:role:view', 'lock', 1, 0, 0, 1, 30, JSON_OBJECT('seed', '002_user_rbac_seed'), @super_user_id, @super_user_id, 0, 0),

  (@system_tenant_id, 0, 'TENANT_ROOT', '租户管理', 0, '/tenant', NULL, NULL, 'office', 1, 0, 0, 1, 10, JSON_OBJECT('seed', '002_user_rbac_seed'), @super_user_id, @super_user_id, 0, 0),
  (@system_tenant_id, 0, 'TENANT_DASHBOARD', '租户首页', 1, '/tenant/dashboard', 'tenant/dashboard/index', 'tenant:dashboard:view', 'dashboard', 1, 0, 0, 1, 20, JSON_OBJECT('seed', '002_user_rbac_seed'), @super_user_id, @super_user_id, 0, 0),
  (@system_tenant_id, 0, 'TENANT_MEMBER_MGMT', '成员管理', 1, '/tenant/members', 'tenant/member/index', 'tenant:member:view', 'team', 1, 0, 0, 1, 30, JSON_OBJECT('seed', '002_user_rbac_seed'), @super_user_id, @super_user_id, 0, 0),
  (@system_tenant_id, 0, 'TENANT_ROLE_MGMT', '角色管理', 1, '/tenant/roles', 'tenant/role/index', 'tenant:role:view', 'safety', 1, 0, 0, 1, 40, JSON_OBJECT('seed', '002_user_rbac_seed'), @super_user_id, @super_user_id, 0, 0)
ON DUPLICATE KEY UPDATE
  parent_id = VALUES(parent_id),
  menu_name = VALUES(menu_name),
  menu_type = VALUES(menu_type),
  path = VALUES(path),
  component = VALUES(component),
  permission_key = VALUES(permission_key),
  icon = VALUES(icon),
  visible = VALUES(visible),
  is_frame = VALUES(is_frame),
  is_cache = VALUES(is_cache),
  status = VALUES(status),
  sort = VALUES(sort),
  deleted = 0,
  update_by = VALUES(update_by);

SELECT id INTO @menu_platform_root_id FROM scr_menu WHERE system_id = @system_platform_id AND menu_code = 'PLATFORM_ROOT' AND deleted = 0 LIMIT 1;
SELECT id INTO @menu_platform_user_id FROM scr_menu WHERE system_id = @system_platform_id AND menu_code = 'PLATFORM_USER_MGMT' AND deleted = 0 LIMIT 1;
SELECT id INTO @menu_platform_role_id FROM scr_menu WHERE system_id = @system_platform_id AND menu_code = 'PLATFORM_ROLE_MGMT' AND deleted = 0 LIMIT 1;
SELECT id INTO @menu_tenant_root_id FROM scr_menu WHERE system_id = @system_tenant_id AND menu_code = 'TENANT_ROOT' AND deleted = 0 LIMIT 1;
SELECT id INTO @menu_tenant_dashboard_id FROM scr_menu WHERE system_id = @system_tenant_id AND menu_code = 'TENANT_DASHBOARD' AND deleted = 0 LIMIT 1;
SELECT id INTO @menu_tenant_member_id FROM scr_menu WHERE system_id = @system_tenant_id AND menu_code = 'TENANT_MEMBER_MGMT' AND deleted = 0 LIMIT 1;
SELECT id INTO @menu_tenant_role_id FROM scr_menu WHERE system_id = @system_tenant_id AND menu_code = 'TENANT_ROLE_MGMT' AND deleted = 0 LIMIT 1;

INSERT INTO scr_menu_op
(
  menu_id, op_code, op_name, http_method, path_pattern, permission_key,
  status, sort, ext_json,
  create_by, update_by, deleted, lock_version
)
VALUES
  (@menu_platform_user_id, 'VIEW',   '查看', 'GET',    '/api/platform/users/**', 'platform:user:view',   1, 10, JSON_OBJECT('seed', '002_user_rbac_seed'), @super_user_id, @super_user_id, 0, 0),
  (@menu_platform_user_id, 'CREATE', '新增', 'POST',   '/api/platform/users',    'platform:user:create', 1, 20, JSON_OBJECT('seed', '002_user_rbac_seed'), @super_user_id, @super_user_id, 0, 0),
  (@menu_platform_user_id, 'UPDATE', '修改', 'PUT',    '/api/platform/users/**', 'platform:user:update', 1, 30, JSON_OBJECT('seed', '002_user_rbac_seed'), @super_user_id, @super_user_id, 0, 0),
  (@menu_platform_user_id, 'DELETE', '删除', 'DELETE', '/api/platform/users/**', 'platform:user:delete', 1, 40, JSON_OBJECT('seed', '002_user_rbac_seed'), @super_user_id, @super_user_id, 0, 0),

  (@menu_platform_role_id, 'VIEW',   '查看', 'GET',    '/api/platform/roles/**', 'platform:role:view',   1, 10, JSON_OBJECT('seed', '002_user_rbac_seed'), @super_user_id, @super_user_id, 0, 0),
  (@menu_platform_role_id, 'CREATE', '新增', 'POST',   '/api/platform/roles',    'platform:role:create', 1, 20, JSON_OBJECT('seed', '002_user_rbac_seed'), @super_user_id, @super_user_id, 0, 0),
  (@menu_platform_role_id, 'UPDATE', '修改', 'PUT',    '/api/platform/roles/**', 'platform:role:update', 1, 30, JSON_OBJECT('seed', '002_user_rbac_seed'), @super_user_id, @super_user_id, 0, 0),
  (@menu_platform_role_id, 'DELETE', '删除', 'DELETE', '/api/platform/roles/**', 'platform:role:delete', 1, 40, JSON_OBJECT('seed', '002_user_rbac_seed'), @super_user_id, @super_user_id, 0, 0),

  (@menu_tenant_dashboard_id, 'VIEW', '查看', 'GET', '/api/tenant/dashboard/**', 'tenant:dashboard:view', 1, 10, JSON_OBJECT('seed', '002_user_rbac_seed'), @super_user_id, @super_user_id, 0, 0),

  (@menu_tenant_member_id, 'VIEW',   '查看', 'GET',    '/api/tenant/members/**', 'tenant:member:view',   1, 10, JSON_OBJECT('seed', '002_user_rbac_seed'), @super_user_id, @super_user_id, 0, 0),
  (@menu_tenant_member_id, 'CREATE', '新增', 'POST',   '/api/tenant/members',    'tenant:member:create', 1, 20, JSON_OBJECT('seed', '002_user_rbac_seed'), @super_user_id, @super_user_id, 0, 0),
  (@menu_tenant_member_id, 'UPDATE', '修改', 'PUT',    '/api/tenant/members/**', 'tenant:member:update', 1, 30, JSON_OBJECT('seed', '002_user_rbac_seed'), @super_user_id, @super_user_id, 0, 0),
  (@menu_tenant_member_id, 'DELETE', '删除', 'DELETE', '/api/tenant/members/**', 'tenant:member:delete', 1, 40, JSON_OBJECT('seed', '002_user_rbac_seed'), @super_user_id, @super_user_id, 0, 0),

  (@menu_tenant_role_id, 'VIEW',   '查看', 'GET',    '/api/tenant/roles/**', 'tenant:role:view',   1, 10, JSON_OBJECT('seed', '002_user_rbac_seed'), @super_user_id, @super_user_id, 0, 0),
  (@menu_tenant_role_id, 'CREATE', '新增', 'POST',   '/api/tenant/roles',    'tenant:role:create', 1, 20, JSON_OBJECT('seed', '002_user_rbac_seed'), @super_user_id, @super_user_id, 0, 0),
  (@menu_tenant_role_id, 'UPDATE', '修改', 'PUT',    '/api/tenant/roles/**', 'tenant:role:update', 1, 30, JSON_OBJECT('seed', '002_user_rbac_seed'), @super_user_id, @super_user_id, 0, 0),
  (@menu_tenant_role_id, 'DELETE', '删除', 'DELETE', '/api/tenant/roles/**', 'tenant:role:delete', 1, 40, JSON_OBJECT('seed', '002_user_rbac_seed'), @super_user_id, @super_user_id, 0, 0)
ON DUPLICATE KEY UPDATE
  op_name = VALUES(op_name),
  http_method = VALUES(http_method),
  path_pattern = VALUES(path_pattern),
  permission_key = VALUES(permission_key),
  status = VALUES(status),
  sort = VALUES(sort),
  deleted = 0,
  update_by = VALUES(update_by);

-- 平台超管 -> 全平台菜单
INSERT INTO scr_role_menu
(
  role_id, menu_id, granted_by, granted_time,
  create_by, update_by, deleted, lock_version
)
SELECT
  @role_platform_super_id, m.id, @super_user_id, @seed_now,
  @super_user_id, @super_user_id, 0, 0
FROM scr_menu m
WHERE m.system_id = @system_platform_id
  AND m.deleted = 0
ON DUPLICATE KEY UPDATE
  granted_by = VALUES(granted_by),
  granted_time = VALUES(granted_time),
  deleted = 0,
  update_by = VALUES(update_by);

-- 平台超管 -> 全平台操作
INSERT INTO scr_role_menu_op
(
  role_id, menu_op_id, granted_by, granted_time,
  create_by, update_by, deleted, lock_version
)
SELECT
  @role_platform_super_id, mo.id, @super_user_id, @seed_now,
  @super_user_id, @super_user_id, 0, 0
FROM scr_menu_op mo
JOIN scr_menu m ON m.id = mo.menu_id
WHERE m.system_id = @system_platform_id
  AND m.deleted = 0
  AND mo.deleted = 0
ON DUPLICATE KEY UPDATE
  granted_by = VALUES(granted_by),
  granted_time = VALUES(granted_time),
  deleted = 0,
  update_by = VALUES(update_by);

-- 租户管理员 -> 全租户菜单
INSERT INTO scr_role_menu
(
  role_id, menu_id, granted_by, granted_time,
  create_by, update_by, deleted, lock_version
)
SELECT
  @role_tenant_admin_id, m.id, @super_user_id, @seed_now,
  @super_user_id, @super_user_id, 0, 0
FROM scr_menu m
WHERE m.system_id = @system_tenant_id
  AND m.deleted = 0
ON DUPLICATE KEY UPDATE
  granted_by = VALUES(granted_by),
  granted_time = VALUES(granted_time),
  deleted = 0,
  update_by = VALUES(update_by);

-- 租户管理员 -> 全租户操作
INSERT INTO scr_role_menu_op
(
  role_id, menu_op_id, granted_by, granted_time,
  create_by, update_by, deleted, lock_version
)
SELECT
  @role_tenant_admin_id, mo.id, @super_user_id, @seed_now,
  @super_user_id, @super_user_id, 0, 0
FROM scr_menu_op mo
JOIN scr_menu m ON m.id = mo.menu_id
WHERE m.system_id = @system_tenant_id
  AND m.deleted = 0
  AND mo.deleted = 0
ON DUPLICATE KEY UPDATE
  granted_by = VALUES(granted_by),
  granted_time = VALUES(granted_time),
  deleted = 0,
  update_by = VALUES(update_by);

-- 租户所有人员 -> 租户首页菜单与查看操作
INSERT INTO scr_role_menu
(
  role_id, menu_id, granted_by, granted_time,
  create_by, update_by, deleted, lock_version
)
VALUES
  (@role_tenant_user_id, @menu_tenant_root_id, @super_user_id, @seed_now, @super_user_id, @super_user_id, 0, 0),
  (@role_tenant_user_id, @menu_tenant_dashboard_id, @super_user_id, @seed_now, @super_user_id, @super_user_id, 0, 0)
ON DUPLICATE KEY UPDATE
  granted_by = VALUES(granted_by),
  granted_time = VALUES(granted_time),
  deleted = 0,
  update_by = VALUES(update_by);

INSERT INTO scr_role_menu_op
(
  role_id, menu_op_id, granted_by, granted_time,
  create_by, update_by, deleted, lock_version
)
SELECT
  @role_tenant_user_id, mo.id, @super_user_id, @seed_now,
  @super_user_id, @super_user_id, 0, 0
FROM scr_menu_op mo
WHERE mo.menu_id = @menu_tenant_dashboard_id
  AND mo.op_code = 'VIEW'
  AND mo.deleted = 0
ON DUPLICATE KEY UPDATE
  granted_by = VALUES(granted_by),
  granted_time = VALUES(granted_time),
  deleted = 0,
  update_by = VALUES(update_by);

-- ----------------------------------------
-- 数据资源初始化与角色数据授权
-- ----------------------------------------
INSERT INTO scr_drs
(
  system_id, drs_code, drs_name, drs_type, resource_uri,
  owner_estab_id, data_owner_type, status, remark, ext_json,
  create_by, update_by, deleted, lock_version
)
VALUES
  (@system_tenant_id, 'TENANT_USER_TABLE', '租户用户数据', 0, 'def_user', @estab_id, 1, 1, '系统初始化默认数据资源', JSON_OBJECT('seed', '002_user_rbac_seed'), @super_user_id, @super_user_id, 0, 0),
  (@system_tenant_id, 'TENANT_TEAM_TABLE', '租户团队数据', 0, 'def_team', @estab_id, 1, 1, '系统初始化默认数据资源', JSON_OBJECT('seed', '002_user_rbac_seed'), @super_user_id, @super_user_id, 0, 0),
  (@system_tenant_id, 'TENANT_ROLE_TABLE', '租户角色数据', 0, 'scr_role', @estab_id, 1, 1, '系统初始化默认数据资源', JSON_OBJECT('seed', '002_user_rbac_seed'), @super_user_id, @super_user_id, 0, 0)
ON DUPLICATE KEY UPDATE
  drs_name = VALUES(drs_name),
  drs_type = VALUES(drs_type),
  resource_uri = VALUES(resource_uri),
  owner_estab_id = VALUES(owner_estab_id),
  data_owner_type = VALUES(data_owner_type),
  status = VALUES(status),
  remark = VALUES(remark),
  deleted = 0,
  update_by = VALUES(update_by);

-- 租户管理员：全部数据范围
INSERT INTO scr_role_drs
(
  role_id, drs_id, scope_type, scope_rule,
  create_by, update_by, deleted, lock_version
)
SELECT
  @role_tenant_admin_id, d.id, 0, NULL,
  @super_user_id, @super_user_id, 0, 0
FROM scr_drs d
WHERE d.system_id = @system_tenant_id
  AND d.owner_estab_id = @estab_id
  AND d.deleted = 0
ON DUPLICATE KEY UPDATE
  scope_type = VALUES(scope_type),
  scope_rule = VALUES(scope_rule),
  deleted = 0,
  update_by = VALUES(update_by);

-- 租户所有人员：仅本人范围（仅限用户数据）
INSERT INTO scr_role_drs
(
  role_id, drs_id, scope_type, scope_rule,
  create_by, update_by, deleted, lock_version
)
SELECT
  @role_tenant_user_id, d.id, 1, JSON_OBJECT('field', 'id', 'operator', '=', 'valueFrom', 'loginUserId'),
  @super_user_id, @super_user_id, 0, 0
FROM scr_drs d
WHERE d.system_id = @system_tenant_id
  AND d.owner_estab_id = @estab_id
  AND d.drs_code = 'TENANT_USER_TABLE'
  AND d.deleted = 0
ON DUPLICATE KEY UPDATE
  scope_type = VALUES(scope_type),
  scope_rule = VALUES(scope_rule),
  deleted = 0,
  update_by = VALUES(update_by);

COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
