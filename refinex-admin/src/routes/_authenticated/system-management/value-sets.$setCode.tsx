import z from 'zod'
import { createFileRoute } from '@tanstack/react-router'
import { ValueSetValuesPage } from '@/features/system/value-sets/values-page'

const searchSchema = z.object({
  setName: z.string().optional().catch(''),
})

export const Route = createFileRoute('/_authenticated/system-management/value-sets/$setCode')({
  validateSearch: searchSchema,
  component: RouteComponent,
})

function RouteComponent() {
  const { setCode } = Route.useParams()
  const { setName } = Route.useSearch()

  return <ValueSetValuesPage setCode={setCode} setName={setName} />
}
