import { AxiosError } from 'axios'
import { toast } from 'sonner'
import { ApiBusinessError } from '@/lib/http'

export function handleServerError(error: unknown) {
  // eslint-disable-next-line no-console
  console.log(error)

  let errMsg = '发生错误！'

  if (error instanceof ApiBusinessError) {
    errMsg = error.message
  }

  if (
    error &&
    typeof error === 'object' &&
    'status' in error &&
    Number(error.status) === 204
  ) {
    errMsg = '未找到内容。'
  }

  if (error instanceof AxiosError) {
    const responseData = error.response?.data
    if (responseData && typeof responseData === 'object') {
      const payload = responseData as Record<string, unknown>
      errMsg =
        (typeof payload.msg === 'string' && payload.msg) ||
        (typeof payload.responseMessage === 'string' && payload.responseMessage) ||
        (typeof payload.title === 'string' && payload.title) ||
        error.message
    } else {
      errMsg = error.message
    }
  }

  toast.error(errMsg)
}
