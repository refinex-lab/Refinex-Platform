import { http } from '@/lib/http'
import { buildSystemPath } from './client'
import type {
  AssignRolePermissionsRequest,
  AssignRoleUsersRequest,
  PageData,
  Role,
  RoleBinding,
  RoleCreateRequest,
  RoleListQuery,
  RoleUpdateRequest,
} from './types'

export async function listRoles(query?: RoleListQuery): Promise<PageData<Role>> {
  const response = await http.get<PageData<Role>>(buildSystemPath('/roles'), {
    params: query,
  })
  return response.data ?? { data: [] }
}

export async function getRole(roleId: number): Promise<Role> {
  const response = await http.get<Role>(buildSystemPath(`/roles/${roleId}`))
  return response.data
}

export async function createRole(payload: RoleCreateRequest): Promise<Role> {
  const response = await http.post<Role>(buildSystemPath('/roles'), payload)
  return response.data
}

export async function updateRole(roleId: number, payload: RoleUpdateRequest): Promise<Role> {
  const response = await http.put<Role>(buildSystemPath(`/roles/${roleId}`), payload)
  return response.data
}

export async function getRoleBindings(roleId: number): Promise<RoleBinding> {
  const response = await http.get<RoleBinding>(buildSystemPath(`/roles/${roleId}/bindings`))
  return response.data
}

export async function assignRoleUsers(
  roleId: number,
  payload: AssignRoleUsersRequest
): Promise<void> {
  await http.put<void>(buildSystemPath(`/roles/${roleId}/users`), payload)
}

export async function assignRolePermissions(
  roleId: number,
  payload: AssignRolePermissionsRequest
): Promise<void> {
  await http.put<void>(buildSystemPath(`/roles/${roleId}/permissions`), payload)
}
