import axios, {type AxiosResponse, type InternalAxiosRequestConfig} from 'axios'
import {appConfig} from '@/config/app-config'
import {useAuthStore} from '@/stores/auth-store'

interface ResultEnvelope<T> {
    success?: boolean
    code?: string
    message?: string
    responseCode?: string
    responseMessage?: string
    data?: T
}

interface PageResultEnvelope<T> extends ResultEnvelope<T[]> {
    total?: number
    totalPage?: number
    page?: number
    size?: number
}

interface GatewayEnvelope<T> {
    code?: number
    msg?: string
    data?: T
}

export class ApiBusinessError extends Error {
    code?: string
    status?: number
    raw?: unknown

    constructor(message: string, options?: { code?: string; status?: number; raw?: unknown }) {
        super(message)
        this.name = 'ApiBusinessError'
        this.code = options?.code
        this.status = options?.status
        this.raw = options?.raw
    }
}

function isResultEnvelope(payload: unknown): payload is ResultEnvelope<unknown> {
    return payload !== null && typeof payload === 'object' && 'success' in payload
}

function isPageResultEnvelope(payload: unknown): payload is PageResultEnvelope<unknown> {
    return (
        isResultEnvelope(payload) &&
        'total' in payload &&
        'page' in payload &&
        'size' in payload
    )
}

function isGatewayEnvelope(payload: unknown): payload is GatewayEnvelope<unknown> {
    return (
        payload !== null &&
        typeof payload === 'object' &&
        'code' in payload &&
        'msg' in payload
    )
}

function attachToken(config: InternalAxiosRequestConfig): InternalAxiosRequestConfig {
    const {auth} = useAuthStore.getState()
    if (!auth.accessToken) {
        return config
    }

    const headerName = auth.tokenName || appConfig.auth.tokenHeaderName
    const headers = config.headers
    if (headers && typeof headers.set === 'function') {
        headers.set(headerName, auth.accessToken)
        // 兼容部分服务仍读取 Authorization 的场景
        headers.set('Authorization', auth.accessToken)
    } else {
        config.headers = {
            ...(config.headers ?? {}),
            [headerName]: auth.accessToken,
            Authorization: auth.accessToken,
        } as InternalAxiosRequestConfig['headers']
    }
    return config
}

function unwrapResponse(response: AxiosResponse): AxiosResponse {
    const payload = response.data

    // 网关返回 401 但 HTTP 状态码是 200 的情况（Sa-Token 网关过滤器的响应格式）
    if (isGatewayEnvelope(payload) && payload.code === 401) {
        handleUnauthorized()
        throw new ApiBusinessError(payload.msg || '登录已过期，请重新登录', {
            code: '401',
            status: 401,
            raw: payload,
        })
    }

    if (isResultEnvelope(payload)) {
        if (payload.success) {
            response.data = isPageResultEnvelope(payload) ? payload : payload.data
            return response
        }

        throw new ApiBusinessError(
            payload.message || payload.responseMessage || '请求失败',
            {
                code: payload.code || payload.responseCode,
                status: response.status,
                raw: payload,
            }
        )
    }

    if (isGatewayEnvelope(payload)) {
        if (payload.code === 200) {
            response.data = payload.data
            return response
        }

        throw new ApiBusinessError(payload.msg || '请求失败', {
            code: payload.code == null ? undefined : String(payload.code),
            status: payload.code,
            raw: payload,
        })
    }

    return response
}

export const http = axios.create({
    baseURL: appConfig.api.gatewayBaseUrl,
    timeout: appConfig.api.timeoutMs,
})

http.interceptors.request.use(attachToken)
http.interceptors.response.use(unwrapResponse, handleResponseError)

let redirecting = false

function handleUnauthorized() {
    if (redirecting) return
    redirecting = true
    useAuthStore.getState().auth.reset()
    const currentPath = globalThis.location.pathname + globalThis.location.search
    const signInUrl = currentPath && currentPath !== '/sign-in'
        ? `/sign-in?redirect=${encodeURIComponent(currentPath)}`
        : '/sign-in'
    globalThis.location.href = signInUrl
}

function handleResponseError(error: unknown) {
    if (
        error &&
        typeof error === 'object' &&
        'response' in error &&
        (error as { response?: { status?: number } }).response?.status === 401
    ) {
        handleUnauthorized()
    }
    return Promise.reject(error)
}
