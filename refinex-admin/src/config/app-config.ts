function normalizePrefix(prefix: string | undefined, fallback: string): string {
  const raw = (prefix ?? fallback).trim()
  if (!raw) return fallback
  return raw.startsWith('/') ? raw : `/${raw}`
}

function normalizeBaseUrl(url: string | undefined, fallback: string): string {
  const raw = (url ?? fallback).trim()
  if (!raw) return fallback
  return raw.replace(/\/$/, '')
}

function parsePositiveInt(value: string | undefined, fallback: number): number {
  if (!value) return fallback
  const parsed = Number(value)
  return Number.isFinite(parsed) && parsed > 0 ? parsed : fallback
}

const gatewayBaseUrl = normalizeBaseUrl(
  import.meta.env.VITE_API_BASE_URL,
  'http://127.0.0.1:8080'
)

export const appConfig = {
  app: {
    name: import.meta.env.VITE_APP_NAME || 'Refinex 管理后台',
    subtitle: import.meta.env.VITE_APP_SUBTITLE || 'Enterprise Console',
    logoUrl: import.meta.env.VITE_APP_LOGO_URL || '/images/favicon.svg',
  },
  api: {
    gatewayBaseUrl,
    timeoutMs: parsePositiveInt(import.meta.env.VITE_API_TIMEOUT_MS, 12000),
    prefixes: {
      auth: normalizePrefix(import.meta.env.VITE_API_PREFIX_AUTH, '/refinex-auth'),
      user: normalizePrefix(import.meta.env.VITE_API_PREFIX_USER, '/refinex-user'),
      token: normalizePrefix(import.meta.env.VITE_API_PREFIX_TOKEN, '/refinex-auth'),
      system: normalizePrefix(import.meta.env.VITE_API_PREFIX_SYSTEM, '/refinex-system'),
    },
  },
  auth: {
    tokenHeaderName: import.meta.env.VITE_AUTH_TOKEN_HEADER || 'Refinex-Token',
  },
} as const

export type ApiModule = keyof typeof appConfig.api.prefixes

export function buildModulePath(module: ApiModule, path: string): string {
  const prefix = appConfig.api.prefixes[module]
  const normalizedPath = path.startsWith('/') ? path : `/${path}`
  return `${prefix}${normalizedPath}`
}
