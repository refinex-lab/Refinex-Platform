import { buildModulePath } from '@/config/app-config'
import { http } from '@/lib/http'

export interface UserInfo {
  userId?: number
  userCode?: string
  username?: string
  displayName?: string
  nickname?: string
  avatarUrl?: string
  gender?: number
  birthday?: string
  primaryPhone?: string
  phoneVerified?: boolean
  primaryEmail?: string
  emailVerified?: boolean
  status?: string
  userType?: string
  registerTime?: string
  lastLoginTime?: string
  lastLoginIp?: string
  primaryEstabId?: number
  primaryTeamId?: number
  estabAdmin?: boolean
}

export interface UserEstab {
  estabId: number
  estabCode?: string
  estabName: string
  estabShortName?: string
  logoUrl?: string
  estabType?: number
  admin?: boolean
  current?: boolean
}

export interface UpdateProfileRequest {
  displayName: string
  nickname?: string
  avatarUrl?: string
  gender?: number
  birthday?: string
}

export async function getCurrentUserInfo(): Promise<UserInfo> {
  const response = await http.get<UserInfo>(buildModulePath('user', '/users/me/info'))
  return response.data
}

export async function getCurrentUserEstabs(): Promise<UserEstab[]> {
  const response = await http.get<UserEstab[]>(buildModulePath('user', '/users/me/estabs'))
  return response.data ?? []
}

export async function updateCurrentUserProfile(payload: UpdateProfileRequest): Promise<UserInfo> {
  const response = await http.put<UserInfo>(buildModulePath('user', '/users/me/profile'), payload)
  return response.data
}
