import { http } from '@/lib/http'
import { buildSystemPath } from './client'
import type {
  DataResource,
  DataResourceCreateRequest,
  DataResourceInterface,
  DataResourceInterfaceCreateRequest,
  DataResourceInterfaceListQuery,
  DataResourceInterfaceUpdateRequest,
  DataResourceListQuery,
  DataResourceUpdateRequest,
} from './types'

export async function listDataResources(query?: DataResourceListQuery): Promise<DataResource[]> {
  const response = await http.get<DataResource[]>(buildSystemPath('/drs'), {
    params: query,
  })
  return response.data ?? []
}

export async function getDataResource(drsId: number): Promise<DataResource> {
  const response = await http.get<DataResource>(buildSystemPath(`/drs/${drsId}`))
  return response.data
}

export async function createDataResource(
  payload: DataResourceCreateRequest
): Promise<DataResource> {
  const response = await http.post<DataResource>(buildSystemPath('/drs'), payload)
  return response.data
}

export async function updateDataResource(
  drsId: number,
  payload: DataResourceUpdateRequest
): Promise<DataResource> {
  const response = await http.put<DataResource>(buildSystemPath(`/drs/${drsId}`), payload)
  return response.data
}

export async function deleteDataResource(drsId: number): Promise<void> {
  await http.delete<void>(buildSystemPath(`/drs/${drsId}`))
}

export async function listDataResourceInterfaces(
  query?: DataResourceInterfaceListQuery
): Promise<DataResourceInterface[]> {
  const response = await http.get<DataResourceInterface[]>(buildSystemPath('/drs/interfaces'), {
    params: query,
  })
  return response.data ?? []
}

export async function getDataResourceInterface(
  interfaceId: number
): Promise<DataResourceInterface> {
  const response = await http.get<DataResourceInterface>(
    buildSystemPath(`/drs/interfaces/${interfaceId}`)
  )
  return response.data
}

export async function createDataResourceInterface(
  drsId: number,
  payload: DataResourceInterfaceCreateRequest
): Promise<DataResourceInterface> {
  const response = await http.post<DataResourceInterface>(
    buildSystemPath(`/drs/${drsId}/interfaces`),
    payload
  )
  return response.data
}

export async function updateDataResourceInterface(
  interfaceId: number,
  payload: DataResourceInterfaceUpdateRequest
): Promise<DataResourceInterface> {
  const response = await http.put<DataResourceInterface>(
    buildSystemPath(`/drs/interfaces/${interfaceId}`),
    payload
  )
  return response.data
}

export async function deleteDataResourceInterface(interfaceId: number): Promise<void> {
  await http.delete<void>(buildSystemPath(`/drs/interfaces/${interfaceId}`))
}
