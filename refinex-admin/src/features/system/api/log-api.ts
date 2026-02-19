import { http } from '@/lib/http'
import { buildSystemPath } from './client'
import type {
  ErrorLog,
  ErrorLogListQuery,
  LoginLog,
  LoginLogListQuery,
  NotifyLog,
  NotifyLogListQuery,
  OperateLog,
  OperateLogListQuery,
} from './types'

export async function listLoginLogs(query?: LoginLogListQuery): Promise<LoginLog[]> {
  const response = await http.get<LoginLog[]>(buildSystemPath('/logs/login'), {
    params: query,
  })
  return response.data ?? []
}

export async function getLoginLog(logId: number): Promise<LoginLog> {
  const response = await http.get<LoginLog>(buildSystemPath(`/logs/login/${logId}`))
  return response.data
}

export async function listOperateLogs(query?: OperateLogListQuery): Promise<OperateLog[]> {
  const response = await http.get<OperateLog[]>(buildSystemPath('/logs/operate'), {
    params: query,
  })
  return response.data ?? []
}

export async function getOperateLog(logId: number): Promise<OperateLog> {
  const response = await http.get<OperateLog>(buildSystemPath(`/logs/operate/${logId}`))
  return response.data
}

export async function listErrorLogs(query?: ErrorLogListQuery): Promise<ErrorLog[]> {
  const response = await http.get<ErrorLog[]>(buildSystemPath('/logs/error'), {
    params: query,
  })
  return response.data ?? []
}

export async function getErrorLog(logId: number): Promise<ErrorLog> {
  const response = await http.get<ErrorLog>(buildSystemPath(`/logs/error/${logId}`))
  return response.data
}

export async function listNotifyLogs(query?: NotifyLogListQuery): Promise<NotifyLog[]> {
  const response = await http.get<NotifyLog[]>(buildSystemPath('/logs/notify'), {
    params: query,
  })
  return response.data ?? []
}

export async function getNotifyLog(logId: number): Promise<NotifyLog> {
  const response = await http.get<NotifyLog>(buildSystemPath(`/logs/notify/${logId}`))
  return response.data
}
