import { useEffect, useRef, useState, type ChangeEvent } from 'react'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { ImageUp, Loader2 } from 'lucide-react'
import { toast } from 'sonner'
import {
  getCurrentUserInfo,
  uploadCurrentUserAvatar,
  updateCurrentUserProfile,
} from '@/features/user/api/user-api'
import { handleServerError } from '@/lib/handle-server-error'
import { useAuthStore } from '@/stores/auth-store'
import { useUserStore } from '@/stores/user-store'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
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
const MAX_AVATAR_SIZE_BYTES = 5 * 1024 * 1024
const ACCEPTED_AVATAR_TYPES = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'] as const

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
  gender: z.enum(GENDER_OPTIONS),
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
  const setProfile = useUserStore((state) => state.setProfile)
  const loading = useUserStore((state) => state.loading)
  const [saving, setSaving] = useState(false)
  const [fetching, setFetching] = useState(false)
  const [uploadingAvatar, setUploadingAvatar] = useState(false)
  const fileInputRef = useRef<HTMLInputElement | null>(null)

  const form = useForm<ProfileFormValues>({
    resolver: zodResolver(profileFormSchema),
    defaultValues,
    mode: 'onChange',
  })

  useEffect(() => {
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
          const cachedProfile = useUserStore.getState().profile
          if (cachedProfile) {
            form.reset(toFormValues(cachedProfile))
          }
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
  }, [form, setProfile])

  const avatarUrl = form.watch('avatarUrl')
  const displayName = form.watch('displayName')
  const avatarName =
    displayName ||
    auth.user?.displayName ||
    auth.user?.nickname ||
    auth.user?.username ||
    '用户'
  const avatarFallback = avatarName.slice(0, 1).toUpperCase()
  const disabled = loading || fetching || saving || uploadingAvatar

  async function handleAvatarFileChange(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0]
    event.target.value = ''
    if (!file) {
      return
    }

    if (!ACCEPTED_AVATAR_TYPES.includes(file.type as (typeof ACCEPTED_AVATAR_TYPES)[number])) {
      toast.error('头像仅支持 JPG/PNG/WEBP/GIF 格式')
      return
    }
    if (file.size > MAX_AVATAR_SIZE_BYTES) {
      toast.error('头像文件大小不能超过 5MB')
      return
    }

    setUploadingAvatar(true)
    try {
      const updated = await uploadCurrentUserAvatar(file)
      form.setValue('avatarUrl', updated.avatarUrl ?? '', { shouldDirty: true })
      setProfile(updated)
      auth.setUser({
        ...auth.user,
        avatarUrl: updated.avatarUrl ?? auth.user?.avatarUrl,
      })
      toast.success('头像上传成功')
    } catch (error) {
      handleServerError(error)
    } finally {
      setUploadingAvatar(false)
    }
  }

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
        {/* <div className='rounded-md border border-dashed px-3 py-2 text-sm text-muted-foreground'>
          这里维护个人展示信息。账号安全、登录方式与密码请在“账号”页面管理。
        </div> */}

        <FormField
          control={form.control}
          name='avatarUrl'
          render={() => (
            <FormItem>
              <FormLabel>头像</FormLabel>
              <div className='flex items-center gap-4 rounded-lg border p-4'>
                <Avatar className='h-16 w-16 border'>
                  <AvatarImage src={avatarUrl || undefined} alt={avatarName} />
                  <AvatarFallback className='text-lg'>{avatarFallback}</AvatarFallback>
                </Avatar>
                <div className='space-y-2'>
                  <Button
                    type='button'
                    variant='outline'
                    disabled={disabled}
                    onClick={() => fileInputRef.current?.click()}
                  >
                    {uploadingAvatar ? (
                      <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                    ) : (
                      <ImageUp className='mr-2 h-4 w-4' />
                    )}
                    上传头像
                  </Button>
                  <FormDescription>
                    支持 JPG/PNG/WEBP/GIF，最大 5MB。
                  </FormDescription>
                  <input
                    ref={fileInputRef}
                    type='file'
                    accept={ACCEPTED_AVATAR_TYPES.join(',')}
                    className='hidden'
                    onChange={handleAvatarFileChange}
                  />
                </div>
              </div>
              <FormMessage />
            </FormItem>
          )}
        />

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
                    <SelectTrigger className='w-full'>
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

        <Button type='submit' disabled={disabled}>
          {(saving || fetching) && <Loader2 className='mr-2 h-4 w-4 animate-spin' />}
          保存资料
        </Button>
      </form>
    </Form>
  )
}
