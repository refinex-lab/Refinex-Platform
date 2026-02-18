import { create } from 'zustand'
import { appConfig } from '@/config/app-config'
import type { LoginUser, LoginResponse } from '@/features/auth/api/auth-api'
import { getCookie, removeCookie, setCookie } from '@/lib/cookies'

const AUTH_COOKIE_KEY = 'refinex_admin_auth'

type StoredSession = {
  accessToken: string
  tokenName: string
  user: LoginUser | null
}

function readSessionFromCookie(): StoredSession {
  const cookieValue = getCookie(AUTH_COOKIE_KEY)
  if (!cookieValue) {
    return {
      accessToken: '',
      tokenName: appConfig.auth.tokenHeaderName,
      user: null,
    }
  }

  try {
    const parsed = JSON.parse(cookieValue) as Partial<StoredSession>
    return {
      accessToken: parsed.accessToken ?? '',
      tokenName: parsed.tokenName || appConfig.auth.tokenHeaderName,
      user: parsed.user ?? null,
    }
  } catch {
    return {
      accessToken: '',
      tokenName: appConfig.auth.tokenHeaderName,
      user: null,
    }
  }
}

function persistSession(session: StoredSession): void {
  setCookie(AUTH_COOKIE_KEY, JSON.stringify(session))
}

interface AuthState {
  auth: {
    user: LoginUser | null
    accessToken: string
    tokenName: string
    setUser: (user: LoginUser | null) => void
    setAccessToken: (accessToken: string, tokenName?: string) => void
    setSession: (payload: LoginResponse) => void
    resetAccessToken: () => void
    reset: () => void
    isAuthenticated: () => boolean
  }
}

export const useAuthStore = create<AuthState>()((set, get) => {
  const initialSession = readSessionFromCookie()

  return {
    auth: {
      user: initialSession.user,
      accessToken: initialSession.accessToken,
      tokenName: initialSession.tokenName,
      setUser: (user) =>
        set((state) => {
          const nextSession: StoredSession = {
            accessToken: state.auth.accessToken,
            tokenName: state.auth.tokenName,
            user,
          }
          persistSession(nextSession)
          return { ...state, auth: { ...state.auth, user } }
        }),
      setAccessToken: (accessToken, tokenName) =>
        set((state) => {
          const nextTokenName = tokenName || state.auth.tokenName || appConfig.auth.tokenHeaderName
          const nextSession: StoredSession = {
            accessToken,
            tokenName: nextTokenName,
            user: state.auth.user,
          }
          persistSession(nextSession)
          return {
            ...state,
            auth: { ...state.auth, accessToken, tokenName: nextTokenName },
          }
        }),
      setSession: (payload) =>
        set((state) => {
          const accessToken = payload.token?.tokenValue ?? ''
          const tokenName = payload.token?.tokenName || appConfig.auth.tokenHeaderName
          const user = payload.loginUser ?? null

          const nextSession: StoredSession = {
            accessToken,
            tokenName,
            user,
          }

          persistSession(nextSession)
          return {
            ...state,
            auth: {
              ...state.auth,
              accessToken,
              tokenName,
              user,
            },
          }
        }),
      resetAccessToken: () =>
        set((state) => {
          const nextSession: StoredSession = {
            accessToken: '',
            tokenName: appConfig.auth.tokenHeaderName,
            user: state.auth.user,
          }
          persistSession(nextSession)
          return {
            ...state,
            auth: {
              ...state.auth,
              accessToken: '',
              tokenName: appConfig.auth.tokenHeaderName,
            },
          }
        }),
      reset: () =>
        set((state) => {
          removeCookie(AUTH_COOKIE_KEY)
          return {
            ...state,
            auth: {
              ...state.auth,
              user: null,
              accessToken: '',
              tokenName: appConfig.auth.tokenHeaderName,
            },
          }
        }),
      isAuthenticated: () => Boolean(get().auth.accessToken),
    },
  }
})
