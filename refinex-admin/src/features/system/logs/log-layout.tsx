import { ConfigDrawer } from '@/components/config-drawer'
import { Header } from '@/components/layout/header'
import { Main } from '@/components/layout/main'
import { ProfileDropdown } from '@/components/profile-dropdown'
import { Search } from '@/components/search'
import { ThemeSwitch } from '@/components/theme-switch'

type LogLayoutProps = {
  title: string
  desc: string
  children: React.ReactNode
}

export function LogLayout({ title, desc, children }: LogLayoutProps) {
  return (
    <>
      <Header>
        <Search />
        <div className='ms-auto flex items-center gap-4'>
          <ThemeSwitch />
          <ConfigDrawer />
          <ProfileDropdown />
        </div>
      </Header>

      <Main fixed fluid>
        <div className='mb-4'>
          <h1 className='text-2xl font-bold tracking-tight'>{title}</h1>
          <p className='text-sm text-muted-foreground'>{desc}</p>
        </div>
        {children}
      </Main>
    </>
  )
}
