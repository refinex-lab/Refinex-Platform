import { http } from '@/lib/http'
import { buildSystemPath } from './client'
import type {
  PageData,
  ValueCreateRequest,
  ValueItem,
  ValueListQuery,
  ValueSet,
  ValueSetCreateRequest,
  ValueSetListQuery,
  ValueSetUpdateRequest,
  ValueUpdateRequest,
} from './types'

export async function listValueSets(query?: ValueSetListQuery): Promise<PageData<ValueSet>> {
  const response = await http.get<PageData<ValueSet>>(buildSystemPath('/valuesets'), {
    params: query,
  })
  return response.data ?? { data: [] }
}

export async function getValueSet(valueSetId: number): Promise<ValueSet> {
  const response = await http.get<ValueSet>(buildSystemPath(`/valuesets/${valueSetId}`))
  return response.data
}

export async function createValueSet(payload: ValueSetCreateRequest): Promise<ValueSet> {
  const response = await http.post<ValueSet>(buildSystemPath('/valuesets'), payload)
  return response.data
}

export async function updateValueSet(
  valueSetId: number,
  payload: ValueSetUpdateRequest
): Promise<ValueSet> {
  const response = await http.put<ValueSet>(
    buildSystemPath(`/valuesets/${valueSetId}`),
    payload
  )
  return response.data
}

export async function deleteValueSet(valueSetId: number): Promise<void> {
  await http.delete<void>(buildSystemPath(`/valuesets/${valueSetId}`))
}

export async function listValues(query?: ValueListQuery): Promise<PageData<ValueItem>> {
  const response = await http.get<PageData<ValueItem>>(buildSystemPath('/valuesets/values'), {
    params: query,
  })
  return response.data ?? { data: [] }
}

export async function getValue(valueId: number): Promise<ValueItem> {
  const response = await http.get<ValueItem>(buildSystemPath(`/valuesets/values/${valueId}`))
  return response.data
}

export async function createValue(
  setCode: string,
  payload: ValueCreateRequest
): Promise<ValueItem> {
  const response = await http.post<ValueItem>(
    buildSystemPath(`/valuesets/${setCode}/values`),
    payload
  )
  return response.data
}

export async function updateValue(valueId: number, payload: ValueUpdateRequest): Promise<ValueItem> {
  const response = await http.put<ValueItem>(
    buildSystemPath(`/valuesets/values/${valueId}`),
    payload
  )
  return response.data
}

export async function deleteValue(valueId: number): Promise<void> {
  await http.delete<void>(buildSystemPath(`/valuesets/values/${valueId}`))
}
