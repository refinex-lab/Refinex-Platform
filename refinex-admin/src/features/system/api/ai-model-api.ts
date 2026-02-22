import { http } from '@/lib/http'
import { buildAiPath } from './client'
import type {
  AiModel,
  AiModelCreateRequest,
  AiModelListQuery,
  AiModelUpdateRequest,
  PageData,
} from './types'

export async function listModels(query?: AiModelListQuery): Promise<PageData<AiModel>> {
  const response = await http.get<PageData<AiModel>>(buildAiPath('/models'), {
    params: query,
  })
  return response.data ?? { data: [] }
}

export async function listModelsByProviderId(providerId: number): Promise<AiModel[]> {
  const response = await http.get<AiModel[]>(
    buildAiPath(`/models/by-provider/${providerId}`)
  )
  return response.data ?? []
}

export async function getModel(modelId: number): Promise<AiModel> {
  const response = await http.get<AiModel>(buildAiPath(`/models/${modelId}`))
  return response.data
}

export async function createModel(payload: AiModelCreateRequest): Promise<AiModel> {
  const response = await http.post<AiModel>(buildAiPath('/models'), payload)
  return response.data
}

export async function updateModel(
  modelId: number,
  payload: AiModelUpdateRequest
): Promise<AiModel> {
  const response = await http.put<AiModel>(buildAiPath(`/models/${modelId}`), payload)
  return response.data
}

export async function deleteModel(modelId: number): Promise<void> {
  await http.delete<void>(buildAiPath(`/models/${modelId}`))
}
