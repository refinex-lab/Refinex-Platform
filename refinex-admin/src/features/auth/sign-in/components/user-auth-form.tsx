import { useEffect, useMemo, useState } from 'react'
import { z } from 'zod'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Link, useNavigate } from '@tanstack/react-router'
import { Loader2, LogIn } from 'lucide-react'
import { toast } from 'sonner'
import {
  login,
  LoginTypeCode,
  sendEmailCode,
  sendSmsCode,
} from '@/features/auth/api/auth-api'
import { handleServerError } from '@/lib/handle-server-error'
import { useAuthStore } from '@/stores/auth-store'
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

const formSchema = z.object({
  identifier: z.string().trim().min(1, '请输入账号'),
  password: z.string().optional(),
  code: z.string().optional(),
})

type LoginMode = 'phone_sms' | 'email_password' | 'email_code'

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const PHONE_REGEX = /^1\d{10}$/

interface UserAuthFormProps extends React.HTMLAttributes<HTMLFormElement> {
  redirectTo?: string
}

export function UserAuthForm({
  className,
  redirectTo,
  ...props
}: UserAuthFormProps) {
  const [isLoading, setIsLoading] = useState(false)
  const [sendCodeLoading, setSendCodeLoading] = useState(false)
  const [codeCooldown, setCodeCooldown] = useState(0)
  const [loginMode, setLoginMode] = useState<LoginMode>('phone_sms')
  const navigate = useNavigate()
  const { auth } = useAuthStore()

  const form = useForm<z.infer<typeof formSchema>>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      identifier: '',
      password: '',
      code: '',
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
    if (loginMode === 'phone_sms') {
      return {
        label: '手机号',
        placeholder: '请输入 11 位手机号',
      }
    }
    if (loginMode === 'email_password' || loginMode === 'email_code') {
      return {
        label: '邮箱',
        placeholder: 'name@example.com',
      }
    }
    return {
      label: '手机号',
      placeholder: '请输入 11 位手机号',
    }
  }, [loginMode])

  const requiresPassword = loginMode === 'email_password'
  const requiresCode = loginMode === 'phone_sms' || loginMode === 'email_code'

  async function handleSendCode() {
    const identifier = form.getValues('identifier').trim()

    if (!identifier) {
      form.setError('identifier', { message: `请输入${identifierMeta.label}` })
      return
    }

    if (loginMode === 'phone_sms' && !PHONE_REGEX.test(identifier)) {
      form.setError('identifier', { message: '手机号格式不正确' })
      return
    }

    if (loginMode === 'email_code' && !EMAIL_REGEX.test(identifier)) {
      form.setError('identifier', { message: '邮箱格式不正确' })
      return
    }

    setSendCodeLoading(true)
    try {
      if (loginMode === 'phone_sms') {
        await sendSmsCode({ phone: identifier, scene: 'login' })
      } else {
        await sendEmailCode({ email: identifier, scene: 'login' })
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
    const password = data.password?.trim()
    const code = data.code?.trim()

    if (loginMode === 'phone_sms' && !PHONE_REGEX.test(identifier)) {
      form.setError('identifier', { message: '手机号格式不正确' })
      return
    }

    if (
      (loginMode === 'email_password' || loginMode === 'email_code') &&
      !EMAIL_REGEX.test(identifier)
    ) {
      form.setError('identifier', { message: '邮箱格式不正确' })
      return
    }

    if (requiresPassword && !password) {
      form.setError('password', { message: '请输入密码' })
      return
    }

    if (requiresCode && (!code || code.length < 4)) {
      form.setError('code', { message: '请输入正确的验证码' })
      return
    }

    setIsLoading(true)
    try {
      const loginType =
        loginMode === 'phone_sms'
          ? LoginTypeCode.PHONE_SMS
          : loginMode === 'email_password'
            ? LoginTypeCode.EMAIL_PASSWORD
            : LoginTypeCode.EMAIL_CODE

      const loginResult = await login({
        loginType,
        identifier,
        password,
        code,
        sourceType: 1,
        clientId: 'refinex-admin',
      })

      auth.setSession(loginResult)
      const targetPath = redirectTo || '/'
      navigate({ to: targetPath, replace: true })
      toast.success('登录成功')
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
          value={loginMode}
          onValueChange={(value) => {
            setLoginMode(value as LoginMode)
            form.clearErrors()
          }}
        >
          <TabsList className='grid h-auto w-full grid-cols-3 gap-1.5'>
            <TabsTrigger value='phone_sms' className='px-2 text-xs sm:text-sm'>
              手机号验证码
            </TabsTrigger>
            <TabsTrigger value='email_password' className='px-2 text-xs sm:text-sm'>
              邮箱密码
            </TabsTrigger>
            <TabsTrigger value='email_code' className='px-2 text-xs sm:text-sm'>
              邮箱验证码
            </TabsTrigger>
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

        {requiresPassword && (
          <FormField
            control={form.control}
            name='password'
            render={({ field }) => (
              <FormItem className='relative'>
                <FormLabel>密码</FormLabel>
                <FormControl>
                  <PasswordInput placeholder='请输入密码' {...field} />
                </FormControl>
                <FormMessage />
                <Link
                  to='/forgot-password'
                  className='absolute end-0 -top-0.5 text-sm font-medium text-muted-foreground hover:opacity-75'
                >
                  忘记密码？
                </Link>
              </FormItem>
            )}
          />
        )}

        {requiresCode && (
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
        )}

        <Button className='mt-2' disabled={isLoading}>
          {isLoading ? <Loader2 className='animate-spin' /> : <LogIn />}
          登录
        </Button>

        <div className='space-y-3 text-xs text-muted-foreground'>
          {/* <div className='space-y-1.5'>
            <p>其他登录方式（敬请期待）</p>
            <div className='flex flex-wrap gap-x-3 gap-y-1'>
              <button
                type='button'
                className='underline underline-offset-4'
                onClick={() => toast.info('微信扫码登录敬请期待')}
              >
                微信扫码
              </button>
              <button
                type='button'
                className='underline underline-offset-4'
                onClick={() => toast.info('OAuth/OIDC 登录敬请期待')}
              >
                OAuth/OIDC
              </button>
              <button
                type='button'
                className='underline underline-offset-4'
                onClick={() => toast.info('SAML 登录敬请期待')}
              >
                SAML
              </button>
              <button
                type='button'
                className='underline underline-offset-4'
                onClick={() => toast.info('TOTP 登录敬请期待')}
              >
                TOTP
              </button>
            </div>
          </div> */}
          <div className='pt-0.5'>
            还没有账号？
            <Link to='/sign-up' className='ms-1 underline underline-offset-4'>
              去注册
            </Link>
          </div>
        </div>
      </form>
    </Form>
  )
}
