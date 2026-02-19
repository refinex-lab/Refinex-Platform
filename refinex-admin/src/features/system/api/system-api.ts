import { http } from '@/lib/http'
import { buildSystemPath } from './client'
import type {
  PageData,
  SystemCreateRequest,
  SystemDefinition,
  SystemListQuery,
  SystemUpdateRequest,
} from './types'

export async function listSystems(query?: SystemListQuery): Promise<PageData<SystemDefinition>> {
  const response = await http.get<PageData<SystemDefinition>>(buildSystemPath('/systems'), {
    params: query,
  })
  return response.data ?? { data: [] }
}

export async function getSystem(systemId: number): Promise<SystemDefinition> {
  const response = await http.get<SystemDefinition>(buildSystemPath(`/systems/${systemId}`))
  return response.data
}

export async function createSystem(payload: SystemCreateRequest): Promise<SystemDefinition> {
  const response = await http.post<SystemDefinition>(buildSystemPath('/systems'), payload)
  return response.data
}

export async function updateSystem(
  systemId: number,
  payload: SystemUpdateRequest
): Promise<SystemDefinition> {
  const response = await http.put<SystemDefinition>(
    buildSystemPath(`/systems/${systemId}`),
    payload
  )
  return response.data
}
