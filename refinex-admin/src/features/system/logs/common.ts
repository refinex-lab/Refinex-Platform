export function toOptionalString(value?: string): string | undefined {
  const normalized = value?.trim()
  return normalized ? normalized : undefined
}

export function toOptionalNumber(value?: string): number | undefined {
  const normalized = value?.trim()
  if (!normalized) return undefined
  const parsed = Number(normalized)
  if (!Number.isFinite(parsed)) return undefined
  return Math.floor(parsed)
}

export function formatDateTime(value?: string): string {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString('zh-CN')
}
