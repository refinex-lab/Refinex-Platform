import {Link} from '@tanstack/react-router'
import {ArrowLeft} from 'lucide-react'
import {Logo} from '@/assets/logo'
import {appConfig} from '@/config/app-config'

const EFFECTIVE_DATE = '2026-02-18'

export function PrivacyPolicyPage() {
    return (
        <div className='mx-auto min-h-svh max-w-5xl px-5 py-8 sm:px-8 sm:py-10'>
            <header className='mb-6 flex flex-wrap items-center justify-between gap-3'>
                <div className='flex items-center gap-2'>
                    <Logo className='size-7'/>
                    <span className='text-lg font-semibold tracking-tight'>{appConfig.app.name}</span>
                </div>
                <Link
                    to='/sign-in'
                    className='inline-flex items-center gap-1 text-sm text-muted-foreground underline underline-offset-4 hover:text-primary'
                >
                    <ArrowLeft className='size-4'/>
                    返回登录
                </Link>
            </header>

            <article className='rounded-2xl border bg-card p-6 shadow-sm sm:p-8'>
                <div className='mb-7 space-y-2'>
                    <h1 className='text-3xl font-semibold tracking-tight'>隐私政策</h1>
                    <p className='text-sm text-muted-foreground'>
                        生效日期：{EFFECTIVE_DATE}。本政策用于说明平台如何收集、使用、存储与保护你的个人信息。
                    </p>
                </div>

                <div className='space-y-7'>
                    <section className='space-y-2'>
                        <h2 className='text-xl font-semibold'>1. 我们收集的信息</h2>
                        <ul className='list-disc space-y-1.5 pl-5 text-sm leading-7 text-muted-foreground'>
                            <li>账号信息：手机号、邮箱、用户名、加密后的认证凭据。</li>
                            <li>安全信息：登录日志、设备标识、IP、验证码发送记录。</li>
                            <li>业务信息：你在平台中提交、配置、上传或同步的数据。</li>
                        </ul>
                    </section>

                    <section className='space-y-2'>
                        <h2 className='text-xl font-semibold'>2. 信息使用目的</h2>
                        <ul className='list-disc space-y-1.5 pl-5 text-sm leading-7 text-muted-foreground'>
                            <li>提供账号注册、登录鉴权、权限控制与业务功能。</li>
                            <li>用于风控、反欺诈、故障排查和审计追踪。</li>
                            <li>用于服务通知、工单反馈和版本更新提醒。</li>
                        </ul>
                    </section>

                    <section className='space-y-2'>
                        <h2 className='text-xl font-semibold'>3. 信息共享与披露</h2>
                        <p className='text-sm leading-7 text-muted-foreground'>
                            未经你同意，我们不会向第三方出售个人信息。仅在法律法规要求、监管执法、维护公共安全或履行合同必要场景下，依法进行最小化披露。
                        </p>
                    </section>

                    <section className='space-y-2'>
                        <h2 className='text-xl font-semibold'>4. 信息存储与保护</h2>
                        <ul className='list-disc space-y-1.5 pl-5 text-sm leading-7 text-muted-foreground'>
                            <li>采用访问控制、日志审计、传输加密等安全措施保护数据。</li>
                            <li>根据业务与法律要求设置存储期限，超期后将删除或匿名化处理。</li>
                            <li>发生安全事件时，将按法律要求通知受影响用户并及时处置。</li>
                        </ul>
                    </section>

                    <section className='space-y-2'>
                        <h2 className='text-xl font-semibold'>5. 你的权利</h2>
                        <ul className='list-disc space-y-1.5 pl-5 text-sm leading-7 text-muted-foreground'>
                            <li>你有权查询、更正、删除或导出你的个人信息（法律另有规定除外）。</li>
                            <li>你可通过账号设置或联系我们申请撤回授权、注销账号。</li>
                            <li>你可对本政策相关处理行为提出异议或投诉。</li>
                        </ul>
                    </section>

                    <section className='space-y-2'>
                        <h2 className='text-xl font-semibold'>6. Cookie 与本地存储</h2>
                        <p className='text-sm leading-7 text-muted-foreground'>
                            为保障登录态、偏好设置与会话安全，我们会使用 Cookie 或浏览器本地存储。你可通过浏览器设置管理，但可能影响部分功能可用性。
                        </p>
                    </section>

                    <section className='space-y-2'>
                        <h2 className='text-xl font-semibold'>7. 联系方式</h2>
                        <p className='text-sm leading-7 text-muted-foreground'>
                            如你对隐私政策有疑问或申请行使权利，可发送邮件至
                            <a href='mailto:refinex@163.com'
                               className='mx-1 underline underline-offset-4 hover:text-primary'>
                                refinex@163.com
                            </a>
                            。
                        </p>
                    </section>
                </div>
            </article>
        </div>
    )
}
