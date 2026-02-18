import { useEffect, useMemo, useState } from 'react'
import { z } from 'zod'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useNavigate } from '@tanstack/react-router'
import { ArrowRight, Loader2 } from 'lucide-react'
import { toast } from 'sonner'
import {
  resetPassword,
  ResetTypeCode,
  sendEmailCode,
  sendSmsCode,
} from '@/features/auth/api/auth-api'
import { handleServerError } from '@/lib/handle-server-error'
import { cn } from '@/lib/utils'
import { Button } from '@/components/ui/button'
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { PasswordInput } from '@/components/password-input'
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs'

const formSchema = z
  .object({
    identifier: z.string().trim().min(1, '请输入账号'),
    code: z.string().trim().min(4, '请输入验证码'),
    newPassword: z.string().min(7, '密码长度至少为 7 位'),
    confirmPassword: z.string().min(1, '请再次输入新密码'),
  })
  .refine((data) => data.newPassword === data.confirmPassword, {
    message: '两次输入的密码不一致',
    path: ['confirmPassword'],
  })

type ResetMode = 'phone' | 'email'

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const PHONE_REGEX = /^1\d{10}$/

export function ForgotPasswordForm({
  className,
  ...props
}: React.HTMLAttributes<HTMLFormElement>) {
  const navigate = useNavigate()
  const [isLoading, setIsLoading] = useState(false)
  const [sendCodeLoading, setSendCodeLoading] = useState(false)
  const [codeCooldown, setCodeCooldown] = useState(0)
  const [resetMode, setResetMode] = useState<ResetMode>('email')

  const form = useForm<z.infer<typeof formSchema>>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      identifier: '',
      code: '',
      newPassword: '',
      confirmPassword: '',
    },
  })

  useEffect(() => {
    if (codeCooldown <= 0) return
    const timer = window.setInterval(() => {
      setCodeCooldown((prev) => (prev > 0 ? prev - 1 : 0))
    }, 1000)
    return () => window.clearInterval(timer)
  }, [codeCooldown])

  const identifierMeta = useMemo(() => {
    if (resetMode === 'phone') {
      return {
        label: '手机号',
        placeholder: '请输入 11 位手机号',
      }
    }
    return {
      label: '邮箱',
      placeholder: 'name@example.com',
    }
  }, [resetMode])

  function validateIdentifier(identifier: string): boolean {
    if (resetMode === 'phone') {
      if (!PHONE_REGEX.test(identifier)) {
        form.setError('identifier', { message: '手机号格式不正确' })
        return false
      }
      return true
    }

    if (!EMAIL_REGEX.test(identifier)) {
      form.setError('identifier', { message: '邮箱格式不正确' })
      return false
    }
    return true
  }

  async function handleSendCode() {
    const identifier = form.getValues('identifier').trim()
    if (!identifier) {
      form.setError('identifier', { message: `请输入${identifierMeta.label}` })
      return
    }

    if (!validateIdentifier(identifier)) {
      return
    }

    setSendCodeLoading(true)
    try {
      if (resetMode === 'phone') {
        await sendSmsCode({ phone: identifier, scene: 'reset' })
      } else {
        await sendEmailCode({ email: identifier, scene: 'reset' })
      }
      setCodeCooldown(60)
      toast.success('验证码已发送，请注意查收')
    } catch (error) {
      handleServerError(error)
    } finally {
      setSendCodeLoading(false)
    }
  }

  async function onSubmit(data: z.infer<typeof formSchema>) {
    const identifier = data.identifier.trim()
    if (!validateIdentifier(identifier)) {
      return
    }

    setIsLoading(true)
    try {
      await resetPassword({
        resetType: resetMode === 'phone' ? ResetTypeCode.PHONE : ResetTypeCode.EMAIL,
        identifier,
        code: data.code.trim(),
        newPassword: data.newPassword,
      })
      toast.success('密码重置成功，请重新登录')
      navigate({ to: '/sign-in', replace: true })
    } catch (error) {
      handleServerError(error)
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <Form {...form}>
      <form
        onSubmit={form.handleSubmit(onSubmit)}
        className={cn('grid gap-4', className)}
        {...props}
      >
        <Tabs
          value={resetMode}
          onValueChange={(value) => {
            setResetMode(value as ResetMode)
            form.clearErrors()
          }}
        >
          <TabsList className='grid h-auto grid-cols-2 gap-1.5'>
            <TabsTrigger value='email'>邮箱验证码</TabsTrigger>
            <TabsTrigger value='phone'>短信验证码</TabsTrigger>
          </TabsList>
        </Tabs>

        <FormField
          control={form.control}
          name='identifier'
          render={({ field }) => (
            <FormItem>
              <FormLabel>{identifierMeta.label}</FormLabel>
              <FormControl>
                <Input placeholder={identifierMeta.placeholder} {...field} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <FormField
          control={form.control}
          name='code'
          render={({ field }) => (
            <FormItem>
              <FormLabel>验证码</FormLabel>
              <FormControl>
                <div className='flex gap-2'>
                  <Input placeholder='请输入验证码' {...field} />
                  <Button
                    type='button'
                    variant='outline'
                    onClick={handleSendCode}
                    disabled={sendCodeLoading || codeCooldown > 0}
                  >
                    {sendCodeLoading ? (
                      <Loader2 className='animate-spin' />
                    ) : codeCooldown > 0 ? (
                      `${codeCooldown}s`
                    ) : (
                      '发送验证码'
                    )}
                  </Button>
                </div>
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
                <PasswordInput placeholder='请输入新密码' {...field} />
              </FormControl>
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
                <PasswordInput placeholder='请再次输入新密码' {...field} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <Button className='mt-2' disabled={isLoading}>
          立即重置
          {isLoading ? <Loader2 className='animate-spin' /> : <ArrowRight />}
        </Button>
      </form>
    </Form>
  )
}
