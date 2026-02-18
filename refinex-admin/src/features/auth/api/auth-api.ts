import { buildModulePath } from '@/config/app-config'
import { http } from '@/lib/http'

export const LoginTypeCode = {
  USERNAME_PASSWORD: 1,
  PHONE_SMS: 2,
  EMAIL_PASSWORD: 3,
  EMAIL_CODE: 4,
} as const

export const RegisterTypeCode = {
  PHONE: 2,
  EMAIL: 3,
} as const

export const ResetTypeCode = {
  PHONE: 2,
  EMAIL: 3,
} as const

export type VerificationScene = 'login' | 'register' | 'reset'

export interface LoginUser {
  userId?: number
  userCode?: string
  username?: string
  displayName?: string
  nickname?: string
  avatarUrl?: string
  estabId?: number
  teamId?: number
  primaryEstabId?: number
  estabAdmin?: boolean
  roleCodes?: string[]
  permissionCodes?: string[]
}

export interface TokenInfo {
  tokenName?: string
  tokenValue?: string
  tokenTimeout?: number
  activeTimeout?: number
  loginId?: number | string
}

export interface LoginResponse {
  token?: TokenInfo
  loginUser?: LoginUser
}

export interface LoginRequest {
  loginType: number
  identifier: string
  password?: string
  code?: string
  estabId?: number
  estabCode?: string
  sourceType?: number
  clientId?: string
  deviceId?: string
}

export interface RegisterRequest {
  registerType: number
  identifier: string
  code: string
  displayName?: string
  nickname?: string
  avatarUrl?: string
  estabId?: number
  estabCode?: string
  estabName?: string
  createEstab?: boolean
  userType?: number
}

export interface SmsSendRequest {
  phone: string
  scene: VerificationScene
  estabId?: number
}

export interface EmailSendRequest {
  email: string
  scene: VerificationScene
  estabId?: number
}

export interface ResetPasswordRequest {
  resetType: number
  identifier: string
  code: string
  newPassword: string
  estabId?: number
}

export async function sendSmsCode(payload: SmsSendRequest): Promise<void> {
  await http.post<void>(buildModulePath('auth', '/sms/send'), payload)
}

export async function sendEmailCode(payload: EmailSendRequest): Promise<void> {
  await http.post<void>(buildModulePath('auth', '/email/send'), payload)
}

export async function register(payload: RegisterRequest): Promise<number> {
  const response = await http.post<number>(buildModulePath('auth', '/register'), payload)
  return response.data
}

export async function login(payload: LoginRequest): Promise<LoginResponse> {
  const response = await http.post<LoginResponse>(buildModulePath('auth', '/login'), payload)
  return response.data
}

export async function logout(): Promise<void> {
  await http.post<void>(buildModulePath('auth', '/logout'))
}

export async function resetPassword(payload: ResetPasswordRequest): Promise<void> {
  await http.post<void>(buildModulePath('auth', '/password/reset'), payload)
}
