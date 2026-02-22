import { http } from '@/lib/http'
import { buildAiPath } from './client'
import type {
  AiProvider,
  AiProviderCreateRequest,
  AiProviderListQuery,
  AiProviderUpdateRequest,
  PageData,
} from './types'

export async function listProviders(query?: AiProviderListQuery): Promise<PageData<AiProvider>> {
  const response = await http.get<PageData<AiProvider>>(buildAiPath('/providers'), {
    params: query,
  })
  return response.data ?? { data: [] }
}

export async function listAllProviders(status?: number): Promise<AiProvider[]> {
  const response = await http.get<AiProvider[]>(buildAiPath('/providers/all'), {
    params: status != null ? { status } : undefined,
  })
  return response.data ?? []
}

export async function getProvider(providerId: number): Promise<AiProvider> {
  const response = await http.get<AiProvider>(buildAiPath(`/providers/${providerId}`))
  return response.data
}

export async function createProvider(payload: AiProviderCreateRequest): Promise<AiProvider> {
  const response = await http.post<AiProvider>(buildAiPath('/providers'), payload)
  return response.data
}

export async function updateProvider(
  providerId: number,
  payload: AiProviderUpdateRequest
): Promise<AiProvider> {
  const response = await http.put<AiProvider>(
    buildAiPath(`/providers/${providerId}`),
    payload
  )
  return response.data
}

export async function deleteProvider(providerId: number): Promise<void> {
  await http.delete<void>(buildAiPath(`/providers/${providerId}`))
}
