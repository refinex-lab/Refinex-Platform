import { http } from '@/lib/http'
import { buildSystemPath } from './client'
import type {
  Menu,
  MenuCreateRequest,
  MenuOpCreateRequest,
  MenuOpManage,
  MenuOpUpdateRequest,
  MenuReorderItem,
  OpDefinition,
  PageData,
  PaginationQuery,
  MenuTreeNode,
  MenuTreeQuery,
  MenuUpdateRequest,
} from './types'

export async function getMenuTree(query: MenuTreeQuery): Promise<MenuTreeNode[]> {
  const response = await http.get<MenuTreeNode[]>(buildSystemPath('/menus/tree'), {
    params: query,
  })
  return response.data ?? []
}

export async function getMenu(menuId: number): Promise<Menu> {
  const response = await http.get<Menu>(buildSystemPath(`/menus/${menuId}`))
  return response.data
}

export async function createMenu(payload: MenuCreateRequest): Promise<Menu> {
  const response = await http.post<Menu>(buildSystemPath('/menus'), payload)
  return response.data
}

export async function updateMenu(menuId: number, payload: MenuUpdateRequest): Promise<Menu> {
  const response = await http.put<Menu>(buildSystemPath(`/menus/${menuId}`), payload)
  return response.data
}

export async function deleteMenu(menuId: number): Promise<void> {
  await http.delete<void>(buildSystemPath(`/menus/${menuId}`))
}

export async function listMenuOps(
  menuId: number,
  query?: PaginationQuery
): Promise<PageData<MenuOpManage>> {
  const response = await http.get<PageData<MenuOpManage>>(buildSystemPath(`/menus/${menuId}/ops`), {
    params: query,
  })
  return response.data ?? { data: [] }
}

export async function getMenuOp(menuOpId: number): Promise<MenuOpManage> {
  const response = await http.get<MenuOpManage>(buildSystemPath(`/menus/ops/${menuOpId}`))
  return response.data
}

export async function createMenuOp(
  menuId: number,
  payload: MenuOpCreateRequest
): Promise<MenuOpManage> {
  const response = await http.post<MenuOpManage>(
    buildSystemPath(`/menus/${menuId}/ops`),
    payload
  )
  return response.data
}

export async function updateMenuOp(
  menuOpId: number,
  payload: MenuOpUpdateRequest
): Promise<MenuOpManage> {
  const response = await http.put<MenuOpManage>(
    buildSystemPath(`/menus/ops/${menuOpId}`),
    payload
  )
  return response.data
}

export async function deleteMenuOp(menuOpId: number): Promise<void> {
  await http.delete<void>(buildSystemPath(`/menus/ops/${menuOpId}`))
}

export async function listOpDefinitions(): Promise<OpDefinition[]> {
  const response = await http.get<OpDefinition[]>(buildSystemPath('/menus/op-definitions'))
  return response.data ?? []
}

export async function reorderMenus(items: MenuReorderItem[]): Promise<void> {
  await http.put<void>(buildSystemPath('/menus/reorder'), items)
}
