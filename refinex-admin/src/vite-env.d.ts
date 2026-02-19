/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_APP_NAME?: string
  readonly VITE_APP_SUBTITLE?: string
  readonly VITE_APP_LOGO_URL?: string

  readonly VITE_API_BASE_URL?: string
  readonly VITE_API_TIMEOUT_MS?: string
  readonly VITE_API_PREFIX_AUTH?: string
  readonly VITE_API_PREFIX_USER?: string
  readonly VITE_API_PREFIX_TOKEN?: string
  readonly VITE_API_PREFIX_SYSTEM?: string

  readonly VITE_AUTH_TOKEN_HEADER?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
