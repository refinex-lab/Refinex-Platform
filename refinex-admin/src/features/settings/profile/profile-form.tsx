import { useEffect, useMemo, useState } from 'react'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { Loader2 } from 'lucide-react'
import { toast } from 'sonner'
import {
  getCurrentUserInfo,
  updateCurrentUserProfile,
} from '@/features/user/api/user-api'
import { handleServerError } from '@/lib/handle-server-error'
import { useAuthStore } from '@/stores/auth-store'
import { useUserStore } from '@/stores/user-store'
import { Button } from '@/components/ui/button'
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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'

const GENDER_OPTIONS = ['0', '1', '2', '3'] as const

const profileFormSchema = z.object({
  displayName: z
    .string()
    .trim()
    .min(2, '显示名称至少需要 2 个字符。')
    .max(64, '显示名称长度不能超过 64 个字符。'),
  nickname: z
    .string()
    .trim()
    .max(64, '昵称长度不能超过 64 个字符。')
    .optional()
    .or(z.literal('')),
  avatarUrl: z
    .string()
    .trim()
    .max(255, '头像地址长度不能超过 255 个字符。')
    .optional()
    .or(z.literal('')),
  gender: z.preprocess(
    (value) => {
      if (value == null || value === '') return '0'
      const normalized = String(value)
      return (GENDER_OPTIONS as readonly string[]).includes(normalized)
        ? normalized
        : '0'
    },
    z.enum(GENDER_OPTIONS)
  ),
  birthday: z.string().optional(),
})

type ProfileFormValues = z.infer<typeof profileFormSchema>

const defaultValues: ProfileFormValues = {
  displayName: '',
  nickname: '',
  avatarUrl: '',
  gender: '0',
  birthday: '',
}

function formatDateTime(value?: string): string {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '-'
  return date.toLocaleString('zh-CN')
}

function toFormValues(profile: ReturnType<typeof useUserStore.getState>['profile']): ProfileFormValues {
  const rawGender = profile?.gender
  const normalizedGender = String(rawGender ?? '0')
  const gender = (GENDER_OPTIONS as readonly string[]).includes(normalizedGender)
    ? (normalizedGender as ProfileFormValues['gender'])
    : '0'

  return {
    displayName: profile?.displayName ?? '',
    nickname: profile?.nickname ?? '',
    avatarUrl: profile?.avatarUrl ?? '',
    gender,
    birthday: profile?.birthday ?? '',
  }
}

export function ProfileForm() {
  const { auth } = useAuthStore()
  const profile = useUserStore((state) => state.profile)
  const setProfile = useUserStore((state) => state.setProfile)
  const loading = useUserStore((state) => state.loading)
  const [saving, setSaving] = useState(false)
  const [fetching, setFetching] = useState(false)

  const form = useForm<ProfileFormValues>({
    resolver: zodResolver(profileFormSchema),
    defaultValues,
    mode: 'onChange',
  })

  useEffect(() => {
    if (profile) {
      form.reset(toFormValues(profile))
      return
    }

    let canceled = false
    setFetching(true)
    ;(async () => {
      try {
        const info = await getCurrentUserInfo()
        if (canceled) return
        setProfile(info)
        form.reset(toFormValues(info))
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
  }, [form, profile, setProfile])

  const disabled = loading || fetching || saving
  const profileMeta = useMemo(
    () => ({
      userCode: profile?.userCode || '-',
      username: profile?.username || '-',
      primaryPhone: profile?.primaryPhone || '-',
      primaryEmail: profile?.primaryEmail || '-',
      registerTime: formatDateTime(profile?.registerTime),
      lastLoginTime: formatDateTime(profile?.lastLoginTime),
      lastLoginIp: profile?.lastLoginIp || '-',
    }),
    [profile]
  )

  async function onSubmit(values: ProfileFormValues) {
    setSaving(true)
    try {
      const payload = {
        displayName: values.displayName.trim(),
        nickname: values.nickname?.trim() || undefined,
        avatarUrl: values.avatarUrl?.trim() || undefined,
        gender: Number(values.gender),
        birthday: values.birthday || undefined,
      }
      const updated = await updateCurrentUserProfile(payload)
      setProfile(updated)
      auth.setUser({
        ...auth.user,
        displayName: updated.displayName ?? auth.user?.displayName,
        nickname: updated.nickname ?? auth.user?.nickname,
        avatarUrl: updated.avatarUrl ?? auth.user?.avatarUrl,
      })
      form.reset(toFormValues(updated))
      toast.success('个人资料已更新')
    } catch (error) {
      handleServerError(error)
    } finally {
      setSaving(false)
    }
  }

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(onSubmit)} className='space-y-8'>
        <div className='grid gap-4 md:grid-cols-2'>
          <FormItem>
            <FormLabel>用户编码</FormLabel>
            <FormControl>
              <Input value={profileMeta.userCode} disabled />
            </FormControl>
          </FormItem>
          <FormItem>
            <FormLabel>用户名</FormLabel>
            <FormControl>
              <Input value={profileMeta.username} disabled />
            </FormControl>
          </FormItem>
        </div>

        <div className='grid gap-4 md:grid-cols-2'>
          <FormField
            control={form.control}
            name='displayName'
            render={({ field }) => (
              <FormItem>
                <FormLabel>显示名称</FormLabel>
                <FormControl>
                  <Input placeholder='请输入显示名称' disabled={disabled} {...field} />
                </FormControl>
                <FormDescription>显示在导航栏与用户菜单中的名称。</FormDescription>
                <FormMessage />
              </FormItem>
            )}
          />
          <FormField
            control={form.control}
            name='nickname'
            render={({ field }) => (
              <FormItem>
                <FormLabel>昵称</FormLabel>
                <FormControl>
                  <Input placeholder='请输入昵称（可选）' disabled={disabled} {...field} />
                </FormControl>
                <FormDescription>用于个人资料与互动场景展示（可选）。</FormDescription>
                <FormMessage />
              </FormItem>
            )}
          />
        </div>

        <div className='grid gap-4 md:grid-cols-2'>
          <FormField
            control={form.control}
            name='gender'
            render={({ field }) => (
              <FormItem>
                <FormLabel>性别</FormLabel>
                <Select
                  onValueChange={(value) => field.onChange(value || '0')}
                  value={field.value || '0'}
                  disabled={disabled}
                >
                  <FormControl>
                    <SelectTrigger>
                      <SelectValue placeholder='请选择性别' />
                    </SelectTrigger>
                  </FormControl>
                  <SelectContent>
                    <SelectItem value='0'>未知</SelectItem>
                    <SelectItem value='1'>男</SelectItem>
                    <SelectItem value='2'>女</SelectItem>
                    <SelectItem value='3'>其他</SelectItem>
                  </SelectContent>
                </Select>
                <FormDescription>系统将按该字段展示用户标签。</FormDescription>
                <FormMessage />
              </FormItem>
            )}
          />
          <FormField
            control={form.control}
            name='birthday'
            render={({ field }) => (
              <FormItem>
                <FormLabel>生日</FormLabel>
                <FormControl>
                  <Input type='date' disabled={disabled} {...field} />
                </FormControl>
                <FormDescription>用于个性化服务与统计分析（可选）。</FormDescription>
                <FormMessage />
              </FormItem>
            )}
          />
        </div>

        <FormField
          control={form.control}
          name='avatarUrl'
          render={({ field }) => (
            <FormItem>
              <FormLabel>头像地址</FormLabel>
              <FormControl>
                <Input
                  placeholder='https://example.com/avatar.png'
                  disabled={disabled}
                  {...field}
                />
              </FormControl>
              <FormDescription>支持填写公网可访问的头像 URL。</FormDescription>
              <FormMessage />
            </FormItem>
          )}
        />

        <div className='grid gap-4 md:grid-cols-2'>
          <FormItem>
            <FormLabel>主手机号</FormLabel>
            <FormControl>
              <Input value={profileMeta.primaryPhone} disabled />
            </FormControl>
          </FormItem>
          <FormItem>
            <FormLabel>主邮箱</FormLabel>
            <FormControl>
              <Input value={profileMeta.primaryEmail} disabled />
            </FormControl>
          </FormItem>
        </div>

        <div className='grid gap-4 text-sm text-muted-foreground md:grid-cols-3'>
          <div>
            <p className='font-medium text-foreground'>注册时间</p>
            <p>{profileMeta.registerTime}</p>
          </div>
          <div>
            <p className='font-medium text-foreground'>最近登录时间</p>
            <p>{profileMeta.lastLoginTime}</p>
          </div>
          <div>
            <p className='font-medium text-foreground'>最近登录 IP</p>
            <p>{profileMeta.lastLoginIp}</p>
          </div>
        </div>

        <Button type='submit' disabled={disabled}>
          {(saving || fetching) && <Loader2 className='mr-2 h-4 w-4 animate-spin' />}
          保存资料
        </Button>
      </form>
    </Form>
  )
}
