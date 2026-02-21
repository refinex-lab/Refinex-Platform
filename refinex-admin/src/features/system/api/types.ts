export interface PaginationQuery {
  currentPage?: number
  pageSize?: number
}

export interface PageData<T> {
  data?: T[]
  total?: number
  totalPage?: number
  page?: number
  size?: number
}

export interface SystemDefinition {
  id?: number
  systemCode?: string
  systemName?: string
  systemType?: number
  baseUrl?: string
  status?: number
  sort?: number
  remark?: string
}

export interface SystemListQuery extends PaginationQuery {
  status?: number
  keyword?: string
}

export interface SystemCreateRequest {
  systemCode: string
  systemName: string
  systemType?: number
  baseUrl?: string
  status?: number
  sort?: number
  remark?: string
}

export interface SystemUpdateRequest {
  systemName: string
  systemType?: number
  baseUrl?: string
  status?: number
  sort?: number
  remark?: string
}

export interface Role {
  id?: number
  estabId?: number
  roleCode?: string
  roleName?: string
  roleType?: number
  isBuiltin?: number
  status?: number
  sort?: number
  remark?: string
}

export interface RoleListQuery extends PaginationQuery {
  estabId?: number
  status?: number
  keyword?: string
}

export interface RoleCreateRequest {
  estabId?: number
  roleCode?: string
  roleName: string
  roleType?: number
  isBuiltin?: number
  status?: number
  sort?: number
  remark?: string
}

export interface RoleUpdateRequest {
  roleName: string
  roleType?: number
  isBuiltin?: number
  status?: number
  sort?: number
  remark?: string
}

export interface RoleBinding {
  users?: RoleBindingUser[]
  userIds?: number[]
  menuIds?: number[]
  menuOpIds?: number[]
  drsInterfaceIds?: number[]
}

export interface RoleBindingUser {
  userId?: number
  userCode?: string
  username?: string
  displayName?: string
}

export interface AssignRoleUsersRequest {
  userIds: number[]
}

export interface AssignRolePermissionsRequest {
  menuIds?: number[]
  menuOpIds?: number[]
  drsInterfaceIds?: number[]
}

export interface Menu {
  id?: number
  estabId?: number
  systemId?: number
  parentId?: number
  menuCode?: string
  menuName?: string
  menuType?: number
  path?: string
  icon?: string
  isBuiltin?: number
  visible?: number
  isFrame?: number
  status?: number
  sort?: number
}

export interface MenuOp {
  id?: number
  opCode?: string
  opName?: string
  status?: number
  sort?: number
  assigned?: boolean
}

export interface MenuOpManage {
  id?: number
  menuId?: number
  opCode?: string
  opName?: string
  status?: number
  sort?: number
}

export interface MenuTreeNode {
  id?: number
  parentId?: number
  menuCode?: string
  menuName?: string
  menuType?: number
  path?: string
  icon?: string
  isBuiltin?: number
  visible?: number
  isFrame?: number
  status?: number
  sort?: number
  assigned?: boolean
  operations?: MenuOp[]
  children?: MenuTreeNode[]
}

export interface MenuTreeQuery {
  estabId?: number
  systemId?: number
  roleId?: number
}

export interface MenuCreateRequest {
  estabId?: number
  systemId?: number
  parentId?: number
  menuCode?: string
  menuName: string
  menuType?: number
  path?: string
  icon?: string
  isBuiltin?: number
  visible?: number
  isFrame?: number
  status?: number
  sort?: number
}

export interface MenuUpdateRequest {
  systemId?: number
  parentId?: number
  menuCode: string
  menuName: string
  menuType?: number
  path?: string
  icon?: string
  isBuiltin?: number
  visible?: number
  isFrame?: number
  status?: number
  sort?: number
}

export interface MenuOpCreateRequest {
  opCode?: string
  opName: string
  status?: number
  sort?: number
}

export interface MenuOpUpdateRequest {
  opCode: string
  opName: string
  status?: number
  sort?: number
}

export interface OpDefinition {
  id?: number
  opCode?: string
  opName?: string
  opDesc?: string
  isBuiltin?: number
  status?: number
  sort?: number
}

export interface ValueSet {
  id?: number
  setCode?: string
  setName?: string
  status?: number
  sort?: number
  description?: string
}

export interface ValueSetListQuery extends PaginationQuery {
  status?: number
  keyword?: string
}

export interface ValueSetCreateRequest {
  setCode: string
  setName: string
  status?: number
  sort?: number
  description?: string
}

export interface ValueSetUpdateRequest {
  setName: string
  status?: number
  sort?: number
  description?: string
}

export interface ValueItem {
  id?: number
  setCode?: string
  valueCode?: string
  valueName?: string
  valueDesc?: string
  status?: number
  isDefault?: number
  sort?: number
}

export interface ValueListQuery extends PaginationQuery {
  setCode?: string
  status?: number
  keyword?: string
}

export interface ValueCreateRequest {
  valueCode: string
  valueName: string
  valueDesc?: string
  status?: number
  isDefault?: number
  sort?: number
}

export interface ValueUpdateRequest {
  valueCode: string
  valueName: string
  valueDesc?: string
  status?: number
  isDefault?: number
  sort?: number
}

export interface DataResource {
  id?: number
  drsCode?: string
  drsName?: string
  ownerEstabId?: number
  ownerEstabName?: string
  dataOwnerType?: number
  status?: number
  remark?: string
}

export interface DataResourceListQuery extends PaginationQuery {
  status?: number
  ownerEstabId?: number
  dataOwnerType?: number
  keyword?: string
}

export interface DataResourceCreateRequest {
  drsCode?: string
  drsName: string
  ownerEstabId?: number
  dataOwnerType?: number
  status?: number
  remark?: string
}

export interface DataResourceUpdateRequest {
  drsName: string
  ownerEstabId?: number
  dataOwnerType?: number
  status?: number
  remark?: string
}

export interface DataResourceInterface {
  id?: number
  drsId?: number
  interfaceCode?: string
  interfaceName?: string
  interfaceSql?: string
  status?: number
  sort?: number
}

export interface DataResourceInterfaceListQuery extends PaginationQuery {
  drsId?: number
  status?: number
  keyword?: string
}

export interface DataResourceInterfaceCreateRequest {
  interfaceCode?: string
  interfaceName: string
  interfaceSql?: string
  status?: number
  sort?: number
}

export interface DataResourceInterfaceUpdateRequest {
  interfaceCode: string
  interfaceName: string
  interfaceSql?: string
  status?: number
  sort?: number
}

export interface LoginLog {
  id?: number
  userId?: number
  username?: string
  estabId?: number
  identityId?: number
  loginType?: number
  sourceType?: number
  success?: number
  failureReason?: string
  ip?: string
  userAgent?: string
  deviceId?: string
  clientId?: string
  requestId?: string
  gmtCreate?: string
}

export interface LoginLogListQuery extends PaginationQuery {
  userId?: number
  estabId?: number
  success?: number
  loginType?: number
  sourceType?: number
  startTime?: string
  endTime?: string
}

export interface OperateLog {
  id?: number
  userId?: number
  username?: string
  estabId?: number
  moduleCode?: string
  operation?: string
  targetType?: string
  targetId?: string
  success?: number
  failReason?: string
  requestMethod?: string
  requestPath?: string
  requestParams?: string
  responseBody?: string
  ip?: string
  userAgent?: string
  durationMs?: number
  traceId?: string
  spanId?: string
  gmtCreate?: string
}

export interface OperateLogListQuery extends PaginationQuery {
  userId?: number
  estabId?: number
  success?: number
  moduleCode?: string
  requestPath?: string
  startTime?: string
  endTime?: string
}

export interface ErrorLog {
  id?: number
  serviceName?: string
  errorCode?: string
  errorType?: string
  errorLevel?: number
  message?: string
  stackTrace?: string
  requestId?: string
  traceId?: string
  requestMethod?: string
  requestPath?: string
  requestParams?: string
  userId?: number
  estabId?: number
  gmtCreate?: string
}

export interface ErrorLogListQuery extends PaginationQuery {
  serviceName?: string
  errorLevel?: number
  requestPath?: string
  startTime?: string
  endTime?: string
}

export interface NotifyLog {
  id?: number
  channelType?: number
  sceneCode?: string
  receiver?: string
  subject?: string
  contentDigest?: string
  provider?: string
  templateCode?: string
  bizId?: string
  sendStatus?: number
  errorMessage?: string
  requestId?: string
  ip?: string
  userAgent?: string
  userId?: number
  estabId?: number
  gmtCreate?: string
}

export interface NotifyLogListQuery extends PaginationQuery {
  channelType?: number
  sceneCode?: string
  receiver?: string
  sendStatus?: number
  startTime?: string
  endTime?: string
}

export interface Estab {
  id?: number
  estabCode?: string
  estabName?: string
  estabShortName?: string
  estabType?: number
  status?: number
  creditCode?: string
  industryCode?: string
  sizeRange?: string
  ownerUserId?: number
  ownerUsername?: string
  contactName?: string
  contactPhone?: string
  contactEmail?: string
  websiteUrl?: string
  logoUrl?: string
  licenseUrl?: string
  remark?: string
}

export interface EstabListQuery extends PaginationQuery {
  status?: number
  estabType?: number
  keyword?: string
}

export interface EstabCreateRequest {
  estabCode?: string
  estabName: string
  estabShortName?: string
  estabType?: number
  status?: number
  creditCode?: string
  industryCode?: string
  sizeRange?: string
  ownerUserId?: number
  contactName?: string
  contactPhone?: string
  contactEmail?: string
  websiteUrl?: string
  logoUrl?: string
  licenseUrl?: string
  remark?: string
}

export interface EstabUpdateRequest {
  estabName: string
  estabShortName?: string
  estabType?: number
  status?: number
  creditCode?: string
  industryCode?: string
  sizeRange?: string
  ownerUserId?: number
  contactName?: string
  contactPhone?: string
  contactEmail?: string
  websiteUrl?: string
  logoUrl?: string
  licenseUrl?: string
  remark?: string
}

export interface EstabAddress {
  id?: number
  estabId?: number
  addrType?: number
  countryCode?: string
  provinceCode?: string
  cityCode?: string
  districtCode?: string
  provinceName?: string
  cityName?: string
  districtName?: string
  addressLine1?: string
  addressLine2?: string
  postalCode?: string
  latitude?: number
  longitude?: number
  isDefault?: number
  remark?: string
}

export interface EstabAddressListQuery extends PaginationQuery {
  addrType?: number
}

export interface EstabAddressCreateRequest {
  addrType?: number
  countryCode?: string
  provinceCode?: string
  cityCode?: string
  districtCode?: string
  provinceName?: string
  cityName?: string
  districtName?: string
  addressLine1?: string
  addressLine2?: string
  postalCode?: string
  latitude?: number
  longitude?: number
  isDefault?: number
  remark?: string
}

export interface EstabAddressUpdateRequest {
  addrType?: number
  countryCode?: string
  provinceCode?: string
  cityCode?: string
  districtCode?: string
  provinceName?: string
  cityName?: string
  districtName?: string
  addressLine1?: string
  addressLine2?: string
  postalCode?: string
  latitude?: number
  longitude?: number
  isDefault?: number
  remark?: string
}

export interface EstabAuthPolicy {
  id?: number
  estabId?: number
  passwordLoginEnabled?: number
  smsLoginEnabled?: number
  emailLoginEnabled?: number
  wechatLoginEnabled?: number
  mfaRequired?: number
  mfaMethods?: string
  passwordMinLen?: number
  passwordStrength?: number
  passwordExpireDays?: number
  loginFailThreshold?: number
  lockMinutes?: number
  sessionTimeoutMinutes?: number
  remark?: string
}

export interface EstabAuthPolicyUpdateRequest {
  passwordLoginEnabled?: number
  smsLoginEnabled?: number
  emailLoginEnabled?: number
  wechatLoginEnabled?: number
  mfaRequired?: number
  mfaMethods?: string
  passwordMinLen?: number
  passwordStrength?: number
  passwordExpireDays?: number
  loginFailThreshold?: number
  lockMinutes?: number
  sessionTimeoutMinutes?: number
  remark?: string
}

export interface EstabUser {
  id?: number
  estabId?: number
  userId?: number
  username?: string
  userCode?: string
  displayName?: string
  memberType?: number
  isAdmin?: number
  status?: number
  joinTime?: string
  leaveTime?: string
  positionTitle?: string
}

export interface EstabUserListQuery extends PaginationQuery {
  status?: number
}

export interface EstabUserCreateRequest {
  userId: number
  memberType?: number
  isAdmin?: number
  status?: number
  joinTime?: string
  leaveTime?: string
  positionTitle?: string
}

export interface EstabUserUpdateRequest {
  memberType?: number
  isAdmin?: number
  status?: number
  joinTime?: string
  leaveTime?: string
  positionTitle?: string
}

export interface Team {
  id?: number
  estabId?: number
  teamCode?: string
  teamName?: string
  parentId?: number
  leaderUserId?: number
  leaderUsername?: string
  leaderUserCode?: string
  leaderDisplayName?: string
  status?: number
  sort?: number
  remark?: string
}

export interface TeamListQuery extends PaginationQuery {
  estabId?: number
  parentId?: number
  status?: number
  keyword?: string
}

export interface TeamCreateRequest {
  teamName: string
  parentId?: number
  leaderUserId?: number
  status?: number
  sort?: number
  remark?: string
}

export interface TeamUpdateRequest {
  teamName: string
  parentId?: number
  leaderUserId?: number
  status?: number
  sort?: number
  remark?: string
}

export interface TeamUser {
  id?: number
  teamId?: number
  userId?: number
  username?: string
  userCode?: string
  displayName?: string
  roleInTeam?: number
  status?: number
  joinTime?: string
}

export interface TeamUserListQuery extends PaginationQuery {
  status?: number
}

export interface TeamUserCreateRequest {
  userId: number
  roleInTeam?: number
  status?: number
  joinTime?: string
}

export interface TeamUserUpdateRequest {
  roleInTeam?: number
  status?: number
  joinTime?: string
}

export interface TeamUserCandidate {
  userId?: number
  username?: string
  userCode?: string
  displayName?: string
}

export interface TeamUserCandidateQuery {
  keyword: string
  limit?: number
}

export interface SystemUser {
  userId?: number
  userCode?: string
  username?: string
  displayName?: string
  nickname?: string
  avatarUrl?: string
  gender?: number
  birthday?: string
  userType?: number
  status?: number
  primaryEstabId?: number
  primaryEstabName?: string
  primaryPhone?: string
  phoneVerified?: number
  primaryEmail?: string
  emailVerified?: number
  lastLoginTime?: string
  lastLoginIp?: string
  loginFailCount?: number
  lockUntil?: string
  remark?: string
}

export interface SystemUserListQuery extends PaginationQuery {
  userIds?: number[]
  primaryEstabId?: number
  status?: number
  userType?: number
  userCode?: string
  username?: string
  displayName?: string
  nickname?: string
  primaryPhone?: string
  primaryEmail?: string
  keyword?: string
  sortBy?: string
  sortDirection?: 'asc' | 'desc'
}

export interface SystemUserCreateRequest {
  userCode?: string
  username?: string
  displayName: string
  nickname?: string
  avatarUrl?: string
  gender?: number
  birthday?: string
  userType?: number
  status?: number
  primaryEstabId?: number
  primaryPhone?: string
  phoneVerified?: number
  primaryEmail?: string
  emailVerified?: number
  remark?: string
}

export interface SystemUserUpdateRequest {
  displayName: string
  nickname?: string
  avatarUrl?: string
  gender?: number
  birthday?: string
  userType?: number
  status?: number
  primaryEstabId?: number
  primaryPhone?: string
  phoneVerified?: number
  primaryEmail?: string
  emailVerified?: number
  remark?: string
}

export interface SystemUserIdentity {
  identityId?: number
  userId?: number
  identityType?: number
  identifier?: string
  issuer?: string
  credentialAlg?: string
  isPrimary?: number
  verified?: number
  verifiedAt?: string
  bindTime?: string
  lastLoginTime?: string
  lastLoginIp?: string
  status?: number
}

export interface SystemUserIdentityCreateRequest {
  identityType: number
  identifier: string
  issuer?: string
  credential?: string
  credentialAlg?: string
  isPrimary?: number
  verified?: number
  status?: number
}

export interface SystemUserIdentityUpdateRequest {
  identifier: string
  issuer?: string
  credential?: string
  credentialAlg?: string
  isPrimary?: number
  verified?: number
  status?: number
}

export interface SystemUserEstab {
  estabId?: number
  estabCode?: string
  estabName?: string
  estabShortName?: string
  logoUrl?: string
  estabType?: number
  admin?: boolean
  current?: boolean
}

export interface SystemUserBatchDeleteRequest {
  userIds: number[]
}
