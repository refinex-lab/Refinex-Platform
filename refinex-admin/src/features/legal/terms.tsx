import {Link} from '@tanstack/react-router'
import {ArrowLeft} from 'lucide-react'
import {Logo} from '@/assets/logo'
import {appConfig} from '@/config/app-config'

const EFFECTIVE_DATE = '2026-02-18'

export function TermsOfServicePage() {
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
                    <h1 className='text-3xl font-semibold tracking-tight'>服务条款</h1>
                    <p className='text-sm text-muted-foreground'>
                        生效日期：{EFFECTIVE_DATE}。请在使用本平台服务前完整阅读本条款。
                    </p>
                </div>

                <div className='space-y-7'>
                    <section className='space-y-2'>
                        <h2 className='text-xl font-semibold'>1. 适用范围</h2>
                        <p className='text-sm leading-7 text-muted-foreground'>
                            本条款适用于你访问、注册、登录和使用 {appConfig.app.name}
                            及其相关网站、应用、小程序与 API 服务的全部行为。你继续使用本平台即视为同意受本条款约束。
                        </p>
                    </section>

                    <section className='space-y-2'>
                        <h2 className='text-xl font-semibold'>2. 账号与安全</h2>
                        <ul className='list-disc space-y-1.5 pl-5 text-sm leading-7 text-muted-foreground'>
                            <li>你应保证注册信息真实、准确、完整并保持更新。</li>
                            <li>你应妥善保管账号、密码、验证码与登录设备，不得出借或转让。</li>
                            <li>你发现账号异常、泄露或被未授权使用时，应立即通知平台并修改凭证。</li>
                        </ul>
                    </section>

                    <section className='space-y-2'>
                        <h2 className='text-xl font-semibold'>3. 使用规范</h2>
                        <ul className='list-disc space-y-1.5 pl-5 text-sm leading-7 text-muted-foreground'>
                            <li>不得实施违法违规行为，不得发布违法、侵权、侮辱、诈骗或恶意内容。</li>
                            <li>不得以任何方式破坏系统安全、稳定性与可用性。</li>
                            <li>不得绕过平台限制进行恶意抓取、压力攻击、逆向工程或未授权调用。</li>
                        </ul>
                    </section>

                    <section className='space-y-2'>
                        <h2 className='text-xl font-semibold'>4. 知识产权</h2>
                        <p className='text-sm leading-7 text-muted-foreground'>
                            平台界面、代码、文档、商标与其它内容的知识产权归平台或权利人所有。未经授权，你不得复制、传播、改编或用于商业用途。
                        </p>
                    </section>

                    <section className='space-y-2'>
                        <h2 className='text-xl font-semibold'>5. 服务中断与变更</h2>
                        <p className='text-sm leading-7 text-muted-foreground'>
                            基于系统维护、升级、故障、网络或监管要求，平台可对服务进行变更、中断或终止，并将尽合理努力提前公告。
                        </p>
                    </section>

                    <section className='space-y-2'>
                        <h2 className='text-xl font-semibold'>6. 责任限制</h2>
                        <p className='text-sm leading-7 text-muted-foreground'>
                            在法律允许范围内，平台不对间接损失、预期收益损失、数据间接损失承担责任。你应自行对业务数据进行备份与风控。
                        </p>
                    </section>

                    <section className='space-y-2'>
                        <h2 className='text-xl font-semibold'>7. 违约处理</h2>
                        <p className='text-sm leading-7 text-muted-foreground'>
                            若你违反本条款，平台有权采取限制功能、暂停服务、封禁账号、保留证据并追究法律责任等措施。
                        </p>
                    </section>

                    <section className='space-y-2'>
                        <h2 className='text-xl font-semibold'>8. 联系方式</h2>
                        <p className='text-sm leading-7 text-muted-foreground'>
                            如你对本条款有疑问，可发送邮件至
                            <a href='mailto:refinex@163.com'
                               className='mx-1 underline underline-offset-4 hover:text-primary'>
                                refinex@163.com
                            </a>
                            联系平台。
                        </p>
                    </section>
                </div>
            </article>
        </div>
    )
}
