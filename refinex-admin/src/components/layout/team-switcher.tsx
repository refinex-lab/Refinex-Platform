import * as React from 'react'
import { Building2, Check, ChevronsUpDown, Loader2, Plus } from 'lucide-react'
import { toast } from 'sonner'
import { switchEstab } from '@/features/auth/api/auth-api'
import { getCurrentUserEstabs, getCurrentUserInfo } from '@/features/user/api/user-api'
import { handleServerError } from '@/lib/handle-server-error'
import { useAuthStore } from '@/stores/auth-store'
import { useUserStore } from '@/stores/user-store'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuShortcut,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import {
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  useSidebar,
} from '@/components/ui/sidebar'

export function TeamSwitcher() {
  const { isMobile } = useSidebar()
  const { auth } = useAuthStore()
  const estabs = useUserStore((state) => state.estabs)
  const setEstabs = useUserStore((state) => state.setEstabs)
  const setProfile = useUserStore((state) => state.setProfile)
  const [switchingId, setSwitchingId] = React.useState<number | null>(null)

  const activeEstabId = auth.user?.estabId
  const activeEstab =
    estabs.find((item) => item.estabId === activeEstabId) ||
    estabs.find((item) => item.current) ||
    estabs[0]

  const activeEstabName = activeEstab?.estabName || '未加入企业'
  const activeSubtitle = activeEstab?.admin ? '企业管理员' : '成员'

  async function handleSwitchEstab(estabId: number) {
    if (switchingId || estabId === activeEstabId) {
      return
    }

    setSwitchingId(estabId)
    try {
      const updatedLoginUser = await switchEstab({ estabId })
      auth.setUser(updatedLoginUser)
      const [profile, latestEstabs] = await Promise.all([
        getCurrentUserInfo(),
        getCurrentUserEstabs(),
      ])
      setProfile(profile)
      setEstabs(latestEstabs)
      toast.success('已切换当前企业')
    } catch (error) {
      handleServerError(error)
    } finally {
      setSwitchingId(null)
    }
  }

  return (
    <SidebarMenu>
      <SidebarMenuItem>
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <SidebarMenuButton
              size='lg'
              className='data-[state=open]:bg-sidebar-accent data-[state=open]:text-sidebar-accent-foreground'
            >
              <div className='flex aspect-square size-8 items-center justify-center rounded-lg bg-sidebar-primary text-sidebar-primary-foreground'>
                <Building2 className='size-4' />
              </div>
              <div className='grid flex-1 text-start text-sm leading-tight'>
                <span className='truncate font-semibold'>{activeEstabName}</span>
                <span className='truncate text-xs'>{activeSubtitle}</span>
              </div>
              <ChevronsUpDown className='ms-auto' />
            </SidebarMenuButton>
          </DropdownMenuTrigger>
          <DropdownMenuContent
            className='w-(--radix-dropdown-menu-trigger-width) min-w-56 rounded-lg'
            align='start'
            side={isMobile ? 'bottom' : 'right'}
            sideOffset={4}
          >
            <DropdownMenuLabel className='text-xs text-muted-foreground'>
              切换企业
            </DropdownMenuLabel>
            {estabs.map((estab) => (
              <DropdownMenuItem
                key={estab.estabId}
                onClick={() => void handleSwitchEstab(estab.estabId)}
                className='gap-2 p-2'
              >
                <div className='flex size-6 items-center justify-center rounded-sm border'>
                  <Building2 className='size-4 shrink-0' />
                </div>
                <span className='truncate'>{estab.estabName}</span>
                {switchingId === estab.estabId && (
                  <DropdownMenuShortcut>
                    <Loader2 className='size-4 animate-spin' />
                  </DropdownMenuShortcut>
                )}
                {switchingId !== estab.estabId && estab.estabId === activeEstabId && (
                  <DropdownMenuShortcut>
                    <Check className='size-4' />
                  </DropdownMenuShortcut>
                )}
              </DropdownMenuItem>
            ))}
            <DropdownMenuSeparator />
            <DropdownMenuItem className='gap-2 p-2' disabled>
              <div className='flex size-6 items-center justify-center rounded-md border bg-background'>
                <Plus className='size-4' />
              </div>
              <div className='font-medium text-muted-foreground'>新建企业（敬请期待）</div>
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </SidebarMenuItem>
    </SidebarMenu>
  )
}
