import {
  Construction,
  LayoutDashboard,
  Monitor,
  Bug,
  ListTodo,
  FileX,
  HelpCircle,
  Lock,
  Bell,
  Package,
  Palette,
  ServerOff,
  Settings,
  Wrench,
  UserCog,
  UserX,
  Users,
  MessagesSquare,
  ShieldCheck,
  AudioWaveform,
  Command,
  GalleryVerticalEnd,
  Database,
  Building2,
  UserRound,
} from 'lucide-react'
import { ClerkLogo } from '@/assets/clerk-logo'
import { appConfig } from '@/config/app-config'
import { type SidebarData } from '../types'

export const sidebarData: SidebarData = {
  user: {
    name: appConfig.app.name,
    email: appConfig.app.subtitle,
    avatar: '/avatars/avatar.jpg',
  },
  teams: [
    {
      name: 'Refinex 科技有限公司',
      logo: Command,
      plan: '系统初始化默认企业',
    },
    {
      name: '星河科技',
      logo: GalleryVerticalEnd,
      plan: '企业版',
    },
    {
      name: '远景创新',
      logo: AudioWaveform,
      plan: '初创版',
    },
  ],
  navGroups: [
    {
      title: '通用',
      items: [
        {
          title: '仪表盘',
          url: '/',
          icon: LayoutDashboard,
        },
        {
          title: '任务',
          url: '/tasks',
          icon: ListTodo,
        },
        {
          title: '应用',
          url: '/apps',
          icon: Package,
        },
        {
          title: '聊天',
          url: '/chats',
          badge: '3',
          icon: MessagesSquare,
        },
        {
          title: '用户',
          url: '/users',
          icon: Users,
        },
        {
          title: 'Clerk 认证',
          icon: ClerkLogo,
          items: [
            {
              title: '登录',
              url: '/clerk/sign-in',
            },
            {
              title: '注册',
              url: '/clerk/sign-up',
            },
            {
              title: '用户管理',
              url: '/clerk/user-management',
            },
          ],
        },
      ],
    },
    {
      title: '页面',
      items: [
        {
          title: '认证',
          icon: ShieldCheck,
          items: [
            {
              title: '登录',
              url: '/sign-in',
            },
            {
              title: '登录（双栏）',
              url: '/sign-in-2',
            },
            {
              title: '注册',
              url: '/sign-up',
            },
            {
              title: '忘记密码',
              url: '/forgot-password',
            },
            {
              title: '验证码',
              url: '/otp',
            },
          ],
        },
        {
          title: '错误页',
          icon: Bug,
          items: [
            {
              title: '未授权',
              url: '/errors/unauthorized',
              icon: Lock,
            },
            {
              title: '禁止访问',
              url: '/errors/forbidden',
              icon: UserX,
            },
            {
              title: '未找到',
              url: '/errors/not-found',
              icon: FileX,
            },
            {
              title: '服务器内部错误',
              url: '/errors/internal-server-error',
              icon: ServerOff,
            },
            {
              title: '维护中',
              url: '/errors/maintenance-error',
              icon: Construction,
            },
          ],
        },
      ],
    },
    {
      title: '其他',
      items: [
        {
          title: '个人空间',
          icon: UserRound,
          items: [
            {
              title: '个人资料',
              url: '/settings',
              icon: UserCog,
            },
            {
              title: '账号',
              url: '/settings/account',
              icon: Wrench,
            },
            {
              title: '外观',
              url: '/settings/appearance',
              icon: Palette,
            },
            {
              title: '通知',
              url: '/settings/notifications',
              icon: Bell,
            },
            {
              title: '显示',
              url: '/settings/display',
              icon: Monitor,
            },
          ],
        },
        {
          title: '系统管理',
          icon: Settings,
          items: [
            {
              title: '系统定义',
              url: '/system-management/systems',
              icon: Settings,
            },
            {
              title: '组织管理',
              icon: Building2,
              items: [
                {
                  title: '企业管理',
                  url: '/system-management/organizations/estabs',
                },
                {
                  title: '团队管理',
                  url: '/system-management/organizations/teams',
                },
              ],
            },
            {
              title: '用户管理',
              url: '/system-management/system-users',
              icon: Users,
            },
            {
              title: '集合定义',
              url: '/system-management/value-sets',
              icon: Database,
            },
            {
              title: '日志管理',
              icon: Bug,
              items: [
                {
                  title: '登录日志',
                  url: '/system-management/logs/login',
                },
                {
                  title: '操作日志',
                  url: '/system-management/logs/operate',
                },
                {
                  title: '错误日志',
                  url: '/system-management/logs/error',
                },
                {
                  title: '通知日志',
                  url: '/system-management/logs/notify',
                },
              ],
            },
          ],
        },
        {
          title: '帮助中心',
          url: '/help-center',
          icon: HelpCircle,
        },
      ],
    },
  ],
}
