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

export function formatDate(value?: string): string {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleDateString('zh-CN')
}

export function toDateTimeLocalValue(value?: string): string {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  const offset = date.getTimezoneOffset()
  const local = new Date(date.getTime() - offset * 60_000)
  return local.toISOString().slice(0, 16)
}

export function normalizeDateTimeLocal(value?: string): string | undefined {
  const normalized = value?.trim()
  if (!normalized) return undefined
  return normalized.length === 16 ? `${normalized}:00` : normalized
}
