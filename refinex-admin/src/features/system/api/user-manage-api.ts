import { http } from '@/lib/http'
import { buildSystemPath } from './client'
import type {
  PageData,
  SystemUser,
  SystemUserCreateRequest,
  SystemUserIdentity,
  SystemUserIdentityCreateRequest,
  SystemUserIdentityUpdateRequest,
  SystemUserListQuery,
  SystemUserUpdateRequest,
} from './types'

export async function listSystemUsers(query?: SystemUserListQuery): Promise<PageData<SystemUser>> {
  const response = await http.get<PageData<SystemUser>>(buildSystemPath('/system-users'), {
    params: query,
  })
  return response.data ?? { data: [] }
}

export async function getSystemUser(userId: number): Promise<SystemUser> {
  const response = await http.get<SystemUser>(buildSystemPath(`/system-users/${userId}`))
  return response.data
}

export async function createSystemUser(payload: SystemUserCreateRequest): Promise<SystemUser> {
  const response = await http.post<SystemUser>(buildSystemPath('/system-users'), payload)
  return response.data
}

export async function updateSystemUser(
  userId: number,
  payload: SystemUserUpdateRequest
): Promise<SystemUser> {
  const response = await http.put<SystemUser>(buildSystemPath(`/system-users/${userId}`), payload)
  return response.data
}

export async function listSystemUserIdentities(
  userId: number,
  query?: { currentPage?: number; pageSize?: number }
): Promise<PageData<SystemUserIdentity>> {
  const response = await http.get<PageData<SystemUserIdentity>>(
    buildSystemPath(`/system-users/${userId}/identities`),
    { params: query }
  )
  return response.data ?? { data: [] }
}

export async function createSystemUserIdentity(
  userId: number,
  payload: SystemUserIdentityCreateRequest
): Promise<SystemUserIdentity> {
  const response = await http.post<SystemUserIdentity>(
    buildSystemPath(`/system-users/${userId}/identities`),
    payload
  )
  return response.data
}

export async function updateSystemUserIdentity(
  identityId: number,
  payload: SystemUserIdentityUpdateRequest
): Promise<SystemUserIdentity> {
  const response = await http.put<SystemUserIdentity>(
    buildSystemPath(`/system-users/identities/${identityId}`),
    payload
  )
  return response.data
}

export async function deleteSystemUserIdentity(identityId: number): Promise<void> {
  await http.delete<void>(buildSystemPath(`/system-users/identities/${identityId}`))
}
