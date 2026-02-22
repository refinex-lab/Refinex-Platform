import { http } from '@/lib/http'
import { buildAiPath } from './client'
import type {
  AiModelProvision,
  AiModelProvisionCreateRequest,
  AiModelProvisionListQuery,
  AiModelProvisionUpdateRequest,
  PageData,
} from './types'

export async function listModelProvisions(
  query?: AiModelProvisionListQuery
): Promise<PageData<AiModelProvision>> {
  const response = await http.get<PageData<AiModelProvision>>(
    buildAiPath('/model-provisions'),
    { params: query }
  )
  return response.data ?? { data: [] }
}

export async function getModelProvision(provisionId: number): Promise<AiModelProvision> {
  const response = await http.get<AiModelProvision>(
    buildAiPath(`/model-provisions/${provisionId}`)
  )
  return response.data
}

export async function createModelProvision(
  payload: AiModelProvisionCreateRequest
): Promise<AiModelProvision> {
  const response = await http.post<AiModelProvision>(
    buildAiPath('/model-provisions'),
    payload
  )
  return response.data
}

export async function updateModelProvision(
  provisionId: number,
  payload: AiModelProvisionUpdateRequest
): Promise<AiModelProvision> {
  const response = await http.put<AiModelProvision>(
    buildAiPath(`/model-provisions/${provisionId}`),
    payload
  )
  return response.data
}

export async function deleteModelProvision(provisionId: number): Promise<void> {
  await http.delete<void>(buildAiPath(`/model-provisions/${provisionId}`))
}
