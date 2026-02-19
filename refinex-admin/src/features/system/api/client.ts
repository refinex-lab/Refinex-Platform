import { buildModulePath } from '@/config/app-config'

export function buildSystemPath(path: string): string {
  return buildModulePath('system', path)
}
