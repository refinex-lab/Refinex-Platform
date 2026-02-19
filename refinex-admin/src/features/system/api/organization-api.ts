import { http } from '@/lib/http'
import { buildSystemPath } from './client'
import type {
  Estab,
  EstabAddress,
  EstabAddressCreateRequest,
  EstabAddressListQuery,
  EstabAddressUpdateRequest,
  EstabAuthPolicy,
  EstabAuthPolicyUpdateRequest,
  EstabCreateRequest,
  EstabListQuery,
  EstabUpdateRequest,
  EstabUser,
  EstabUserCreateRequest,
  EstabUserListQuery,
  EstabUserUpdateRequest,
  Team,
  TeamCreateRequest,
  TeamListQuery,
  TeamUpdateRequest,
  TeamUser,
  TeamUserCreateRequest,
  TeamUserListQuery,
  TeamUserUpdateRequest,
} from './types'

export async function listEstabs(query?: EstabListQuery): Promise<Estab[]> {
  const response = await http.get<Estab[]>(buildSystemPath('/estabs'), {
    params: query,
  })
  return response.data ?? []
}

export async function getEstab(estabId: number): Promise<Estab> {
  const response = await http.get<Estab>(buildSystemPath(`/estabs/${estabId}`))
  return response.data
}

export async function createEstab(payload: EstabCreateRequest): Promise<Estab> {
  const response = await http.post<Estab>(buildSystemPath('/estabs'), payload)
  return response.data
}

export async function updateEstab(estabId: number, payload: EstabUpdateRequest): Promise<Estab> {
  const response = await http.put<Estab>(buildSystemPath(`/estabs/${estabId}`), payload)
  return response.data
}

export async function deleteEstab(estabId: number): Promise<void> {
  await http.delete<void>(buildSystemPath(`/estabs/${estabId}`))
}

export async function listEstabAddresses(
  estabId: number,
  query?: EstabAddressListQuery
): Promise<EstabAddress[]> {
  const response = await http.get<EstabAddress[]>(
    buildSystemPath(`/estabs/${estabId}/addresses`),
    { params: query }
  )
  return response.data ?? []
}

export async function createEstabAddress(
  estabId: number,
  payload: EstabAddressCreateRequest
): Promise<EstabAddress> {
  const response = await http.post<EstabAddress>(
    buildSystemPath(`/estabs/${estabId}/addresses`),
    payload
  )
  return response.data
}

export async function updateEstabAddress(
  addressId: number,
  payload: EstabAddressUpdateRequest
): Promise<EstabAddress> {
  const response = await http.put<EstabAddress>(
    buildSystemPath(`/estab-addresses/${addressId}`),
    payload
  )
  return response.data
}

export async function deleteEstabAddress(addressId: number): Promise<void> {
  await http.delete<void>(buildSystemPath(`/estab-addresses/${addressId}`))
}

export async function getEstabAuthPolicy(estabId: number): Promise<EstabAuthPolicy> {
  const response = await http.get<EstabAuthPolicy>(
    buildSystemPath(`/estabs/${estabId}/auth-policy`)
  )
  return response.data
}

export async function updateEstabAuthPolicy(
  estabId: number,
  payload: EstabAuthPolicyUpdateRequest
): Promise<EstabAuthPolicy> {
  const response = await http.put<EstabAuthPolicy>(
    buildSystemPath(`/estabs/${estabId}/auth-policy`),
    payload
  )
  return response.data
}

export async function listEstabUsers(
  estabId: number,
  query?: EstabUserListQuery
): Promise<EstabUser[]> {
  const response = await http.get<EstabUser[]>(buildSystemPath(`/estabs/${estabId}/users`), {
    params: query,
  })
  return response.data ?? []
}

export async function createEstabUser(
  estabId: number,
  payload: EstabUserCreateRequest
): Promise<EstabUser> {
  const response = await http.post<EstabUser>(
    buildSystemPath(`/estabs/${estabId}/users`),
    payload
  )
  return response.data
}

export async function updateEstabUser(
  estabUserId: number,
  payload: EstabUserUpdateRequest
): Promise<EstabUser> {
  const response = await http.put<EstabUser>(
    buildSystemPath(`/estab-users/${estabUserId}`),
    payload
  )
  return response.data
}

export async function deleteEstabUser(estabUserId: number): Promise<void> {
  await http.delete<void>(buildSystemPath(`/estab-users/${estabUserId}`))
}

export async function listTeams(query?: TeamListQuery): Promise<Team[]> {
  const response = await http.get<Team[]>(buildSystemPath('/teams'), {
    params: query,
  })
  return response.data ?? []
}

export async function getTeam(teamId: number): Promise<Team> {
  const response = await http.get<Team>(buildSystemPath(`/teams/${teamId}`))
  return response.data
}

export async function createTeam(payload: TeamCreateRequest): Promise<Team> {
  const response = await http.post<Team>(buildSystemPath('/teams'), payload)
  return response.data
}

export async function updateTeam(teamId: number, payload: TeamUpdateRequest): Promise<Team> {
  const response = await http.put<Team>(buildSystemPath(`/teams/${teamId}`), payload)
  return response.data
}

export async function deleteTeam(teamId: number): Promise<void> {
  await http.delete<void>(buildSystemPath(`/teams/${teamId}`))
}

export async function listTeamUsers(
  teamId: number,
  query?: TeamUserListQuery
): Promise<TeamUser[]> {
  const response = await http.get<TeamUser[]>(buildSystemPath(`/teams/${teamId}/users`), {
    params: query,
  })
  return response.data ?? []
}

export async function createTeamUser(
  teamId: number,
  payload: TeamUserCreateRequest
): Promise<TeamUser> {
  const response = await http.post<TeamUser>(
    buildSystemPath(`/teams/${teamId}/users`),
    payload
  )
  return response.data
}

export async function updateTeamUser(
  teamUserId: number,
  payload: TeamUserUpdateRequest
): Promise<TeamUser> {
  const response = await http.put<TeamUser>(
    buildSystemPath(`/team-users/${teamUserId}`),
    payload
  )
  return response.data
}

export async function deleteTeamUser(teamUserId: number): Promise<void> {
  await http.delete<void>(buildSystemPath(`/team-users/${teamUserId}`))
}
