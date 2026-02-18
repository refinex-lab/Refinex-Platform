import axios, {type AxiosResponse, type InternalAxiosRequestConfig} from 'axios'
import {appConfig} from '@/config/app-config'
import {useAuthStore} from '@/stores/auth-store'

interface SingleResponseEnvelope<T> {
    success?: boolean
    responseCode?: string
    responseMessage?: string
    data?: T
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

function isSingleResponseEnvelope(payload: unknown): payload is SingleResponseEnvelope<unknown> {
    return payload !== null && typeof payload === 'object' && 'success' in payload
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

    if (isSingleResponseEnvelope(payload)) {
        if (payload.success) {
            response.data = payload.data
            return response
        }

        throw new ApiBusinessError(payload.responseMessage || '请求失败', {
            code: payload.responseCode,
            status: response.status,
            raw: payload,
        })
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
http.interceptors.response.use(unwrapResponse)
