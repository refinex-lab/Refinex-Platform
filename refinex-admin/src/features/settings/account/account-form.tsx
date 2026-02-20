import { useEffect, useMemo, useState } from 'react'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { Loader2 } from 'lucide-react'
import { toast } from 'sonner'
import {
  changeCurrentUserPassword,
  getCurrentUserAccountInfo,
  type UserAccountInfo,
} from '@/features/user/api/user-api'
import { handleServerError } from '@/lib/handle-server-error'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import {
  Form,
  FormControl,
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form'
import { Input } from '@/components/ui/input'

const accountPasswordSchema = z
  .object({
    oldPassword: z
      .string()
      .trim()
      .min(6, '旧密码至少需要 6 个字符。')
      .max(128, '旧密码长度不能超过 128 个字符。'),
    newPassword: z
      .string()
      .trim()
      .min(6, '新密码至少需要 6 个字符。')
      .max(128, '新密码长度不能超过 128 个字符。'),
    confirmPassword: z
      .string()
      .trim()
      .min(6, '确认密码至少需要 6 个字符。')
      .max(128, '确认密码长度不能超过 128 个字符。'),
  })
  .refine((values) => values.newPassword === values.confirmPassword, {
    message: '两次输入的新密码不一致。',
    path: ['confirmPassword'],
  })
  .refine((values) => values.oldPassword !== values.newPassword, {
    message: '新密码不能与旧密码相同。',
    path: ['newPassword'],
  })

type AccountPasswordFormValues = z.infer<typeof accountPasswordSchema>

const defaultValues: AccountPasswordFormValues = {
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
}

const STATUS_LABEL: Record<string, string> = {
  ENABLED: '启用',
  DISABLED: '停用',
  LOCKED: '锁定',
}

const USER_TYPE_LABEL: Record<string, string> = {
  PLATFORM: '平台用户',
  TENANT: '租户用户',
  PARTNER: '合作方用户',
}

function formatDateTime(value?: string): string {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '-'
  return date.toLocaleString('zh-CN')
}

function formatStatus(value?: string): string {
  if (!value) return '-'
  return STATUS_LABEL[value] ?? value
}

function formatUserType(value?: string): string {
  if (!value) return '-'
  return USER_TYPE_LABEL[value] ?? value
}

function boolLabel(value?: boolean): string {
  return value ? '已验证' : '未验证'
}

export function AccountForm() {
  const [account, setAccount] = useState<UserAccountInfo | null>(null)
  const [fetching, setFetching] = useState(false)
  const [saving, setSaving] = useState(false)

  const form = useForm<AccountPasswordFormValues>({
    resolver: zodResolver(accountPasswordSchema),
    defaultValues,
    mode: 'onChange',
  })

  useEffect(() => {
    let canceled = false
    setFetching(true)
    ;(async () => {
      try {
        const data = await getCurrentUserAccountInfo()
        if (!canceled) {
          setAccount(data)
        }
      } catch (error) {
        if (!canceled) {
          handleServerError(error)
        }
      } finally {
        if (!canceled) {
          setFetching(false)
        }
      }
    })()

    return () => {
      canceled = true
    }
  }, [])

  const accountMeta = useMemo(
    () => ({
      userCode: account?.userCode || '-',
      username: account?.username || '-',
      primaryPhone: account?.primaryPhone || '-',
      primaryEmail: account?.primaryEmail || '-',
      status: formatStatus(account?.status),
      userType: formatUserType(account?.userType),
      registerTime: formatDateTime(account?.registerTime),
      lastLoginTime: formatDateTime(account?.lastLoginTime),
      lastLoginIp: account?.lastLoginIp || '-',
    }),
    [account]
  )

  const canChangePassword = Boolean(
    account?.usernamePasswordEnabled || account?.emailPasswordEnabled
  )
  const formDisabled = fetching || saving || !account || !canChangePassword

  if (fetching && !account) {
    return (
      <div className='flex items-center gap-2 text-sm text-muted-foreground'>
        <Loader2 className='h-4 w-4 animate-spin' />
        正在加载账号信息...
      </div>
    )
  }

  async function onSubmit(values: AccountPasswordFormValues) {
    setSaving(true)
    try {
      await changeCurrentUserPassword({
        oldPassword: values.oldPassword.trim(),
        newPassword: values.newPassword.trim(),
      })
      form.reset(defaultValues)
      toast.success('密码修改成功，请使用新密码重新登录。')
    } catch (error) {
      handleServerError(error)
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className='space-y-6'>
      <Card>
        {/* <CardHeader>
          <CardTitle>账号基础信息</CardTitle>
          <CardDescription>以下字段由系统维护，部分字段需通过验证流程更新。</CardDescription>
        </CardHeader> */}
        <CardContent className='space-y-4 px-0'>
          <div className='grid gap-4 md:grid-cols-2'>
            <div className='space-y-2'>
              <p className='text-sm font-medium'>用户编码</p>
              <Input value={accountMeta.userCode} disabled />
            </div>
            <div className='space-y-2'>
              <p className='text-sm font-medium'>用户名</p>
              <Input value={accountMeta.username} disabled />
            </div>
          </div>

          <div className='grid gap-4 md:grid-cols-2'>
            <div className='space-y-2'>
              <p className='text-sm font-medium'>主手机号</p>
              <div className='relative'>
                <Input value={accountMeta.primaryPhone} disabled className='pr-24' />
                <Badge
                  variant={account?.phoneVerified ? 'default' : 'outline'}
                  className='pointer-events-none absolute right-2 top-1/2 -translate-y-1/2'
                >
                  {boolLabel(account?.phoneVerified)}
                </Badge>
              </div>
            </div>
            <div className='space-y-2'>
              <p className='text-sm font-medium'>主邮箱</p>
              <div className='relative'>
                <Input value={accountMeta.primaryEmail} disabled className='pr-24' />
                <Badge
                  variant={account?.emailVerified ? 'default' : 'outline'}
                  className='pointer-events-none absolute right-2 top-1/2 -translate-y-1/2'
                >
                  {boolLabel(account?.emailVerified)}
                </Badge>
              </div>
            </div>
          </div>

          <div className='grid gap-4 md:grid-cols-2'>
            <div className='space-y-2'>
              <p className='text-sm font-medium'>账号状态</p>
              <Input value={accountMeta.status} disabled />
            </div>
            <div className='space-y-2'>
              <p className='text-sm font-medium'>账号类型</p>
              <Input value={accountMeta.userType} disabled />
            </div>
          </div>

          <div className='grid gap-4 text-sm text-muted-foreground md:grid-cols-3'>
            <div>
              <p className='font-medium text-foreground'>注册时间</p>
              <p>{accountMeta.registerTime}</p>
            </div>
            <div>
              <p className='font-medium text-foreground'>最近登录时间</p>
              <p>{accountMeta.lastLoginTime}</p>
            </div>
            <div>
              <p className='font-medium text-foreground'>最近登录 IP</p>
              <p>{accountMeta.lastLoginIp}</p>
            </div>
          </div>

          <div className='grid gap-3 md:grid-cols-2'>
            <div className='flex items-center justify-between rounded-md border px-3 py-2 text-sm'>
              <span>用户名密码</span>
              <Badge variant={account?.usernamePasswordEnabled ? 'default' : 'outline'}>
                {account?.usernamePasswordEnabled ? '已设置' : '未设置'}
              </Badge>
            </div>
            <div className='flex items-center justify-between rounded-md border px-3 py-2 text-sm'>
              <span>邮箱密码</span>
              <Badge variant={account?.emailPasswordEnabled ? 'default' : 'outline'}>
                {account?.emailPasswordEnabled ? '已设置' : '未设置'}
              </Badge>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader className='px-0'>
          <CardTitle>修改密码</CardTitle>
          <CardDescription>修改成功后，建议重新登录以刷新会话状态。</CardDescription>
        </CardHeader>
        <CardContent className='px-0'>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className='space-y-4'>
              {account && !canChangePassword && (
                <p className='rounded-md border border-dashed px-3 py-2 text-sm text-muted-foreground'>
                  当前账号尚未设置密码，请先通过忘记密码流程设置初始密码。
                </p>
              )}
              <FormField
                control={form.control}
                name='oldPassword'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>旧密码</FormLabel>
                    <FormControl>
                      <Input type='password' placeholder='请输入旧密码' disabled={formDisabled} {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name='newPassword'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>新密码</FormLabel>
                    <FormControl>
                      <Input type='password' placeholder='请输入新密码' disabled={formDisabled} {...field} />
                    </FormControl>
                    <FormDescription>建议至少包含字母、数字与特殊字符组合。</FormDescription>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name='confirmPassword'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>确认新密码</FormLabel>
                    <FormControl>
                      <Input
                        type='password'
                        placeholder='请再次输入新密码'
                        disabled={formDisabled}
                        {...field}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <Button type='submit' disabled={formDisabled}>
                {(fetching || saving) && <Loader2 className='mr-2 h-4 w-4 animate-spin' />}
                保存新密码
              </Button>
            </form>
          </Form>
        </CardContent>
      </Card>
    </div>
  )
}
