import { useEffect, useMemo, useState } from 'react'
import { z } from 'zod'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useNavigate } from '@tanstack/react-router'
import { Loader2 } from 'lucide-react'
import { toast } from 'sonner'
import {
  register,
  RegisterTypeCode,
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
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs'

const formSchema = z.object({
  identifier: z.string().trim().min(1, '请输入注册账号'),
  code: z.string().trim().min(4, '请输入验证码'),
  displayName: z.string().trim().max(64, '昵称不能超过 64 个字符').optional(),
})

type RegisterMode = 'phone' | 'email'

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const PHONE_REGEX = /^1\d{10}$/

export function SignUpForm({
  className,
  ...props
}: React.HTMLAttributes<HTMLFormElement>) {
  const [isLoading, setIsLoading] = useState(false)
  const [sendCodeLoading, setSendCodeLoading] = useState(false)
  const [codeCooldown, setCodeCooldown] = useState(0)
  const [registerMode, setRegisterMode] = useState<RegisterMode>('phone')
  const navigate = useNavigate()

  const form = useForm<z.infer<typeof formSchema>>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      identifier: '',
      code: '',
      displayName: '',
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
    if (registerMode === 'phone') {
      return {
        label: '手机号',
        placeholder: '请输入 11 位手机号',
      }
    }
    return {
      label: '邮箱',
      placeholder: 'name@example.com',
    }
  }, [registerMode])

  async function handleSendCode() {
    const identifier = form.getValues('identifier').trim()

    if (!identifier) {
      form.setError('identifier', { message: `请输入${identifierMeta.label}` })
      return
    }

    if (registerMode === 'phone' && !PHONE_REGEX.test(identifier)) {
      form.setError('identifier', { message: '手机号格式不正确' })
      return
    }

    if (registerMode === 'email' && !EMAIL_REGEX.test(identifier)) {
      form.setError('identifier', { message: '邮箱格式不正确' })
      return
    }

    setSendCodeLoading(true)
    try {
      if (registerMode === 'phone') {
        await sendSmsCode({ phone: identifier, scene: 'register' })
      } else {
        await sendEmailCode({ email: identifier, scene: 'register' })
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

    if (registerMode === 'phone' && !PHONE_REGEX.test(identifier)) {
      form.setError('identifier', { message: '手机号格式不正确' })
      return
    }

    if (registerMode === 'email' && !EMAIL_REGEX.test(identifier)) {
      form.setError('identifier', { message: '邮箱格式不正确' })
      return
    }

    setIsLoading(true)
    try {
      await register({
        registerType:
          registerMode === 'phone' ? RegisterTypeCode.PHONE : RegisterTypeCode.EMAIL,
        identifier,
        code: data.code.trim(),
        displayName: data.displayName?.trim() || undefined,
      })

      toast.success('注册成功，请登录')
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
          value={registerMode}
          onValueChange={(value) => {
            setRegisterMode(value as RegisterMode)
            form.clearErrors()
          }}
        >
          <TabsList className='grid h-auto grid-cols-2 gap-1.5'>
            <TabsTrigger value='phone'>手机号注册</TabsTrigger>
            <TabsTrigger value='email'>邮箱注册</TabsTrigger>
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
          name='displayName'
          render={({ field }) => (
            <FormItem>
              <FormLabel>昵称（可选）</FormLabel>
              <FormControl>
                <Input placeholder='例如：张三' {...field} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <Button className='mt-2' disabled={isLoading}>
          {isLoading ? <Loader2 className='animate-spin' /> : null}
          创建账号
        </Button>
      </form>
    </Form>
  )
}
