import { type UIEvent, useEffect, useMemo, useState } from 'react'
import { zodResolver } from '@hookform/resolvers/zod'
import {
  ChevronDown,
  ChevronRight,
  Eye,
  Loader2,
  MoreHorizontal,
  Pencil,
  Plus,
  RefreshCw,
  Trash2,
} from 'lucide-react'
import { useForm } from 'react-hook-form'
import { toast } from 'sonner'
import { z } from 'zod'
import { ConfirmDialog } from '@/components/confirm-dialog'
import { ConfigDrawer } from '@/components/config-drawer'
import { Header } from '@/components/layout/header'
import { Main } from '@/components/layout/main'
import { ProfileDropdown } from '@/components/profile-dropdown'
import { Search } from '@/components/search'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import {
  Form,
  FormControl,
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
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { Textarea } from '@/components/ui/textarea'
import { ThemeSwitch } from '@/components/theme-switch'
import {
  createTeam,
  createTeamUser,
  deleteTeam,
  deleteTeamUser,
  listSystemUsers,
  listTeams,
  listTeamUserCandidates,
  listTeamUsers,
  type Team,
  type TeamCreateRequest,
  type TeamListQuery,
  type TeamUpdateRequest,
  type TeamUser,
  type TeamUserCandidate,
  type TeamUserCreateRequest,
  type TeamUserListQuery,
  type TeamUserUpdateRequest,
  type SystemUser,
  updateTeam,
  updateTeamUser,
} from '@/features/system/api'
import {
  formatDateTime,
  normalizeDateTimeLocal,
  toDateTimeLocalValue,
  toOptionalNumber,
  toOptionalString,
} from '@/features/system/common'
import { PageToolbar } from '@/features/system/components/page-toolbar'
import { handleServerError } from '@/lib/handle-server-error'
import { useAuthStore } from '@/stores/auth-store'

const TEAM_TREE_PAGE_SIZE = 100
const USER_CANDIDATE_LIMIT = 10

const teamFormSchema = z.object({
  teamName: z.string().trim().min(1, '团队名称不能为空').max(128, '团队名称最长 128 位'),
  leaderUserId: z.string().trim().max(20, '负责人标识格式非法').optional(),
  status: z.enum(['1', '2']),
  sort: z.string().trim().max(6, '排序值过大').optional(),
  remark: z.string().trim().max(255, '备注最长 255 位').optional(),
})

const teamUserFormSchema = z.object({
  userId: z.string().trim().min(1, '请选择团队成员').max(20, '成员标识格式非法'),
  roleInTeam: z.enum(['1', '2', '3']),
  status: z.enum(['1', '2']),
  joinTime: z.string().trim().optional(),
})

type TeamFormValues = z.infer<typeof teamFormSchema>
type TeamUserFormValues = z.infer<typeof teamUserFormSchema>
type TeamTreeNode = Team & { children: TeamTreeNode[] }
type TeamDialogMode = 'create' | 'edit' | 'view'

const DEFAULT_TEAM_FORM: TeamFormValues = {
  teamName: '',
  leaderUserId: '',
  status: '1',
  sort: '0',
  remark: '',
}

const DEFAULT_TEAM_USER_FORM: TeamUserFormValues = {
  userId: '',
  roleInTeam: '1',
  status: '1',
  joinTime: '',
}

function toStatusLabel(status?: number): string {
  if (status === 1) return '启用'
  if (status === 2) return '停用'
  return '-'
}

function toRoleInTeamLabel(role?: number): string {
  if (role === 1) return '负责人'
  if (role === 2) return '成员'
  if (role === 3) return '协作'
  return '-'
}

function parsePositiveInteger(value?: string): number | undefined {
  const parsed = toOptionalNumber(value)
  if (parsed == null || parsed <= 0) return undefined
  return parsed
}

function buildTeamTree(teams: Team[]): TeamTreeNode[] {
  const nodeMap = new Map<number, TeamTreeNode>()
  const roots: TeamTreeNode[] = []

  teams.forEach((team) => {
    if (!team.id) return
    nodeMap.set(team.id, { ...team, children: [] })
  })

  nodeMap.forEach((node) => {
    if (node.parentId && nodeMap.has(node.parentId)) {
      nodeMap.get(node.parentId)?.children.push(node)
      return
    }
    roots.push(node)
  })

  const sortNodes = (nodes: TeamTreeNode[]) => {
    nodes.sort((a, b) => (a.sort ?? 0) - (b.sort ?? 0))
    nodes.forEach((item) => sortNodes(item.children))
  }
  sortNodes(roots)

  return roots
}

function buildCandidateLabel(candidate?: TeamUserCandidate | null): string {
  if (!candidate) return ''
  const name = candidate.username || candidate.displayName || ''
  const code = candidate.userCode ? `（${candidate.userCode}）` : ''
  return `${name}${code}`
}

function mapSystemUserToCandidate(user: SystemUser): TeamUserCandidate {
  return {
    userId: user.userId,
    username: user.username,
    userCode: user.userCode,
    displayName: user.displayName,
  }
}

function mapTeamLeaderToCandidate(team: Team): TeamUserCandidate | null {
  if (team.leaderUserId == null) {
    return null
  }
  return {
    userId: team.leaderUserId,
    username: team.leaderUsername,
    userCode: team.leaderUserCode,
    displayName: team.leaderDisplayName,
  }
}

export function TeamsPage() {
  const currentLoginUser = useAuthStore((state) => state.auth.user)
  const currentEstabId = currentLoginUser?.estabId ?? currentLoginUser?.primaryEstabId

  const [teams, setTeams] = useState<Team[]>([])
  const [loading, setLoading] = useState(false)
  const [loadingMoreTeams, setLoadingMoreTeams] = useState(false)
  const [teamKeywordInput, setTeamKeywordInput] = useState('')
  const [teamPage, setTeamPage] = useState(1)
  const [hasMoreTeams, setHasMoreTeams] = useState(false)
  const [query, setQuery] = useState<Omit<TeamListQuery, 'currentPage'>>({
    estabId: currentEstabId,
    keyword: undefined,
    pageSize: TEAM_TREE_PAGE_SIZE,
  })

  const [selectedTeam, setSelectedTeam] = useState<Team | null>(null)
  const selectedTeamId = selectedTeam?.id
  const [expandedNodeIds, setExpandedNodeIds] = useState<Set<number>>(new Set())

  const [members, setMembers] = useState<TeamUser[]>([])
  const [memberTotal, setMemberTotal] = useState(0)
  const [memberLoading, setMemberLoading] = useState(false)
  const [memberQuery, setMemberQuery] = useState<TeamUserListQuery>({ currentPage: 1, pageSize: 10 })

  const [teamDialogOpen, setTeamDialogOpen] = useState(false)
  const [teamDialogMode, setTeamDialogMode] = useState<TeamDialogMode>('create')
  const [editingTeam, setEditingTeam] = useState<Team | null>(null)
  const [teamDialogParentId, setTeamDialogParentId] = useState<number | undefined>(undefined)
  const [savingTeam, setSavingTeam] = useState(false)
  const [deletingTeam, setDeletingTeam] = useState<Team | null>(null)
  const [deletingTeamLoading, setDeletingTeamLoading] = useState(false)
  const [teamLeaderKeyword, setTeamLeaderKeyword] = useState('')
  const [teamLeaderCandidates, setTeamLeaderCandidates] = useState<TeamUserCandidate[]>([])
  const [teamLeaderLoading, setTeamLeaderLoading] = useState(false)
  const [selectedTeamLeader, setSelectedTeamLeader] = useState<TeamUserCandidate | null>(null)

  const [memberDialogOpen, setMemberDialogOpen] = useState(false)
  const [editingMember, setEditingMember] = useState<TeamUser | null>(null)
  const [savingMember, setSavingMember] = useState(false)
  const [deletingMember, setDeletingMember] = useState<TeamUser | null>(null)
  const [deletingMemberLoading, setDeletingMemberLoading] = useState(false)

  const [memberKeyword, setMemberKeyword] = useState('')
  const [memberCandidates, setMemberCandidates] = useState<TeamUserCandidate[]>([])
  const [memberCandidateLoading, setMemberCandidateLoading] = useState(false)
  const [selectedCandidate, setSelectedCandidate] = useState<TeamUserCandidate | null>(null)

  const teamForm = useForm<TeamFormValues>({
    resolver: zodResolver(teamFormSchema),
    defaultValues: DEFAULT_TEAM_FORM,
    mode: 'onChange',
  })
  const memberForm = useForm<TeamUserFormValues>({
    resolver: zodResolver(teamUserFormSchema),
    defaultValues: DEFAULT_TEAM_USER_FORM,
    mode: 'onChange',
  })

  const teamTree = useMemo(() => buildTeamTree(teams), [teams])
  const isTeamViewMode = teamDialogMode === 'view'
  const rootTeamIds = useMemo(
    () => teamTree.map((item) => item.id).filter((item): item is number => Boolean(item)),
    [teamTree]
  )

  useEffect(() => {
    if (rootTeamIds.length === 0) {
      setExpandedNodeIds(new Set())
      return
    }
    setExpandedNodeIds((prev) => {
      if (prev.size > 0) return prev
      return new Set(rootTeamIds)
    })
  }, [rootTeamIds])

  async function loadTeams(activeQuery: TeamListQuery, append: boolean) {
    if (!append) setLoading(true)
    try {
      const pageData = await listTeams(activeQuery)
      const rows = pageData.data ?? []
      const total = pageData.total ?? 0
      const currentPage = activeQuery.currentPage ?? 1
      const pageSize = activeQuery.pageSize ?? TEAM_TREE_PAGE_SIZE

      setTeamPage(currentPage)
      setHasMoreTeams(currentPage * pageSize < total)
      setTeams((prev) => (append ? [...prev, ...rows] : rows))
      if (!append) {
        setSelectedTeam((prev) => {
          if (rows.length === 0) return null
          if (!prev?.id) return rows[0]
          const matched = rows.find((item) => item.id === prev.id)
          return matched ?? rows[0]
        })
      } else {
        setSelectedTeam((prev) => prev ?? rows[0] ?? null)
      }
    } catch (error) {
      handleServerError(error)
    } finally {
      if (!append) setLoading(false)
    }
  }

  async function loadTeamMembers(teamId: number, activeQuery: TeamUserListQuery = memberQuery) {
    setMemberLoading(true)
    try {
      const pageData = await listTeamUsers(teamId, activeQuery)
      setMembers(pageData.data ?? [])
      setMemberTotal(pageData.total ?? 0)
    } catch (error) {
      handleServerError(error)
    } finally {
      setMemberLoading(false)
    }
  }

  useEffect(() => {
    setQuery((prev) => ({
      ...prev,
      estabId: currentEstabId,
      pageSize: TEAM_TREE_PAGE_SIZE,
    }))
  }, [currentEstabId])

  useEffect(() => {
    if (!query.estabId) {
      setTeams([])
      setTeamPage(1)
      setHasMoreTeams(false)
      setSelectedTeam(null)
      return
    }
    void loadTeams({ ...query, currentPage: 1 }, false)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [query])

  useEffect(() => {
    if (!selectedTeamId) {
      setMembers([])
      setMemberTotal(0)
      return
    }
    void loadTeamMembers(selectedTeamId, memberQuery)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedTeamId, memberQuery])

  useEffect(() => {
    if (!teamDialogOpen || isTeamViewMode) {
      return
    }
    if (selectedTeamLeader?.userId) {
      return
    }
    const keyword = teamLeaderKeyword.trim()
    if (!keyword || !currentEstabId) {
      setTeamLeaderCandidates([])
      return
    }

    const timer = window.setTimeout(async () => {
      setTeamLeaderLoading(true)
      try {
        const pageData = await listSystemUsers({
          primaryEstabId: currentEstabId,
          status: 1,
          keyword,
          currentPage: 1,
          pageSize: USER_CANDIDATE_LIMIT,
        })
        const candidates = (pageData.data ?? [])
          .filter((item) => item.userId != null)
          .map(mapSystemUserToCandidate)
        setTeamLeaderCandidates(candidates)
      } catch (error) {
        handleServerError(error)
      } finally {
        setTeamLeaderLoading(false)
      }
    }, 300)

    return () => window.clearTimeout(timer)
  }, [teamDialogOpen, teamLeaderKeyword, currentEstabId, selectedTeamLeader, isTeamViewMode])

  useEffect(() => {
    if (!memberDialogOpen || Boolean(editingMember) || !selectedTeamId) {
      return
    }
    if (selectedCandidate?.userId) {
      return
    }
    const keyword = memberKeyword.trim()
    if (!keyword) {
      setMemberCandidates([])
      return
    }

    const timer = window.setTimeout(async () => {
      setMemberCandidateLoading(true)
      try {
        const users = await listTeamUserCandidates(selectedTeamId, {
          keyword,
          limit: USER_CANDIDATE_LIMIT,
        })
        setMemberCandidates(users)
      } catch (error) {
        handleServerError(error)
      } finally {
        setMemberCandidateLoading(false)
      }
    }, 300)

    return () => window.clearTimeout(timer)
  }, [memberDialogOpen, editingMember, memberKeyword, selectedTeamId, selectedCandidate])

  useEffect(() => {
    const timer = setTimeout(() => {
      if (!currentEstabId) return
      setQuery((prev) => {
        const nextKeyword = toOptionalString(teamKeywordInput)
        if (prev.keyword === nextKeyword) return prev
        return {
          ...prev,
          keyword: nextKeyword,
          pageSize: TEAM_TREE_PAGE_SIZE,
        }
      })
    }, 300)
    return () => clearTimeout(timer)
  }, [teamKeywordInput, currentEstabId])

  function resetTeamKeywordFilter() {
    if (!currentEstabId) {
      toast.error('未获取到当前登录企业，请重新登录或切换企业。')
      return
    }
    setTeamKeywordInput('')
  }

  async function loadMoreTeams() {
    if (!query.estabId || loading || loadingMoreTeams || !hasMoreTeams) return
    const nextPage = teamPage + 1
    setLoadingMoreTeams(true)
    try {
      await loadTeams({ ...query, currentPage: nextPage }, true)
    } finally {
      setLoadingMoreTeams(false)
    }
  }

  function handleTeamTreeScroll(event: UIEvent<HTMLDivElement>) {
    const target = event.currentTarget
    if (target.scrollTop + target.clientHeight >= target.scrollHeight - 120) {
      void loadMoreTeams()
    }
  }

  function toggleExpandNode(teamId: number) {
    setExpandedNodeIds((prev) => {
      const next = new Set(prev)
      if (next.has(teamId)) {
        next.delete(teamId)
      } else {
        next.add(teamId)
      }
      return next
    })
  }

  function resetTeamLeaderCandidateState() {
    setTeamLeaderKeyword('')
    setTeamLeaderCandidates([])
    setSelectedTeamLeader(null)
  }

  function selectTeamLeaderCandidate(
    candidate: TeamUserCandidate,
    onChange: (value: string) => void
  ) {
    if (!candidate.userId) return
    onChange(String(candidate.userId))
    setSelectedTeamLeader(candidate)
    setTeamLeaderKeyword(buildCandidateLabel(candidate))
    setTeamLeaderCandidates([])
  }

  function openCreateTeamDialog() {
    if (!currentEstabId) {
      toast.error('未获取到当前登录企业，请重新登录或切换企业。')
      return
    }
    setTeamDialogMode('create')
    setEditingTeam(null)
    setTeamDialogParentId(undefined)
    teamForm.reset({
      ...DEFAULT_TEAM_FORM,
    })
    resetTeamLeaderCandidateState()
    setTeamDialogOpen(true)
  }

  function openCreateChildTeamDialog(parentTeam: Team) {
    if (!parentTeam.id || !parentTeam.estabId) {
      toast.error('父团队信息不完整，无法新增子部门。')
      return
    }
    setSelectedTeam(parentTeam)
    setTeamDialogMode('create')
    setEditingTeam(null)
    setTeamDialogParentId(parentTeam.id)
    teamForm.reset({
      ...DEFAULT_TEAM_FORM,
    })
    resetTeamLeaderCandidateState()
    setTeamDialogOpen(true)
  }

  function openEditTeamDialog(team: Team) {
    setTeamDialogMode('edit')
    setEditingTeam(team)
    setTeamDialogParentId(team.parentId == null || team.parentId <= 0 ? undefined : team.parentId)
    teamForm.reset({
      teamName: team.teamName ?? '',
      leaderUserId: team.leaderUserId == null ? '' : String(team.leaderUserId),
      status: String(team.status ?? 1) as '1' | '2',
      sort: String(team.sort ?? 0),
      remark: team.remark ?? '',
    })
    const leaderCandidate = mapTeamLeaderToCandidate(team)
    setSelectedTeamLeader(leaderCandidate)
    setTeamLeaderKeyword(buildCandidateLabel(leaderCandidate))
    setTeamLeaderCandidates([])
    setTeamDialogOpen(true)
  }

  function openViewTeamDialog(team: Team) {
    setTeamDialogMode('view')
    setEditingTeam(team)
    setTeamDialogParentId(team.parentId == null || team.parentId <= 0 ? undefined : team.parentId)
    teamForm.reset({
      teamName: team.teamName ?? '',
      leaderUserId: team.leaderUserId == null ? '' : String(team.leaderUserId),
      status: String(team.status ?? 1) as '1' | '2',
      sort: String(team.sort ?? 0),
      remark: team.remark ?? '',
    })
    const leaderCandidate = mapTeamLeaderToCandidate(team)
    setSelectedTeamLeader(leaderCandidate)
    setTeamLeaderKeyword(buildCandidateLabel(leaderCandidate))
    setTeamLeaderCandidates([])
    setTeamDialogOpen(true)
  }

  function closeTeamDialog() {
    setTeamDialogOpen(false)
    setTeamDialogMode('create')
    setEditingTeam(null)
    setTeamDialogParentId(undefined)
    teamForm.reset(DEFAULT_TEAM_FORM)
    resetTeamLeaderCandidateState()
  }

  async function submitTeam(values: TeamFormValues) {
    if (isTeamViewMode) {
      return
    }
    setSavingTeam(true)
    try {
      const payload: TeamUpdateRequest = {
        teamName: values.teamName.trim(),
        parentId: teamDialogParentId,
        leaderUserId: parsePositiveInteger(values.leaderUserId),
        status: Number(values.status),
        sort: toOptionalNumber(values.sort),
        remark: toOptionalString(values.remark),
      }

      if (editingTeam?.id) {
        await updateTeam(editingTeam.id, payload)
        toast.success('团队已更新。')
      } else {
        await createTeam(payload as TeamCreateRequest)
        toast.success('团队已创建。')
      }

      closeTeamDialog()
      await loadTeams({ ...query, currentPage: 1 }, false)
    } catch (error) {
      handleServerError(error)
    } finally {
      setSavingTeam(false)
    }
  }

  async function confirmDeleteTeam() {
    if (!deletingTeam?.id) return
    setDeletingTeamLoading(true)
    try {
      await deleteTeam(deletingTeam.id)
      toast.success('团队已删除。')
      setDeletingTeam(null)
      await loadTeams({ ...query, currentPage: 1 }, false)
    } catch (error) {
      handleServerError(error)
    } finally {
      setDeletingTeamLoading(false)
    }
  }

  function resetMemberCandidateState() {
    setMemberKeyword('')
    setMemberCandidates([])
    setSelectedCandidate(null)
  }

  function openCreateMemberDialog() {
    if (!selectedTeamId) {
      toast.error('请先选择团队。')
      return
    }
    setEditingMember(null)
    memberForm.reset(DEFAULT_TEAM_USER_FORM)
    resetMemberCandidateState()
    setMemberDialogOpen(true)
  }

  function openEditMemberDialog(member: TeamUser) {
    setEditingMember(member)
    memberForm.reset({
      userId: member.userId == null ? '' : String(member.userId),
      roleInTeam: String(member.roleInTeam ?? 1) as '1' | '2' | '3',
      status: String(member.status ?? 1) as '1' | '2',
      joinTime: toDateTimeLocalValue(member.joinTime),
    })
    const presetCandidate: TeamUserCandidate = {
      userId: member.userId,
      username: member.username,
      userCode: member.userCode,
      displayName: member.displayName,
    }
    setSelectedCandidate(presetCandidate)
    setMemberKeyword(presetCandidate.username || presetCandidate.displayName || '')
    setMemberCandidates([])
    setMemberDialogOpen(true)
  }

  function closeMemberDialog() {
    setMemberDialogOpen(false)
    setEditingMember(null)
    memberForm.reset(DEFAULT_TEAM_USER_FORM)
    resetMemberCandidateState()
  }

  function selectMemberCandidate(
    candidate: TeamUserCandidate,
    onChange: (value: string) => void
  ) {
    if (!candidate.userId) return
    onChange(String(candidate.userId))
    setSelectedCandidate(candidate)
    setMemberKeyword(buildCandidateLabel(candidate))
    setMemberCandidates([])
  }

  async function submitMember(values: TeamUserFormValues) {
    if (!selectedTeamId) {
      toast.error('请先选择团队。')
      return
    }
    const userId = parsePositiveInteger(values.userId)
    if (!userId) {
      toast.error('请选择有效的成员用户。')
      return
    }

    setSavingMember(true)
    try {
      const payload: TeamUserUpdateRequest = {
        roleInTeam: Number(values.roleInTeam),
        status: Number(values.status),
        joinTime: normalizeDateTimeLocal(values.joinTime),
      }

      if (editingMember?.id) {
        await updateTeamUser(editingMember.id, payload)
        toast.success('团队成员已更新。')
      } else {
        await createTeamUser(selectedTeamId, {
          ...(payload as TeamUserCreateRequest),
          userId,
        })
        toast.success('团队成员已创建。')
      }

      closeMemberDialog()
      await loadTeamMembers(selectedTeamId, memberQuery)
    } catch (error) {
      handleServerError(error)
    } finally {
      setSavingMember(false)
    }
  }

  async function confirmDeleteMember() {
    if (!selectedTeamId || !deletingMember?.id) return
    setDeletingMemberLoading(true)
    try {
      await deleteTeamUser(deletingMember.id)
      toast.success('团队成员已删除。')
      setDeletingMember(null)
      await loadTeamMembers(selectedTeamId, memberQuery)
    } catch (error) {
      handleServerError(error)
    } finally {
      setDeletingMemberLoading(false)
    }
  }

  function renderTreeNodes(nodes: TeamTreeNode[], depth = 0) {
    return nodes.map((node) => {
      if (!node.id) {
        return null
      }

      const nodeId = node.id
      const hasChildren = node.children.length > 0
      const isExpanded = expandedNodeIds.has(nodeId)
      const isSelected = selectedTeamId === nodeId
      const leaderLabel = node.leaderDisplayName || node.leaderUsername || '-'

      return (
        <div key={nodeId} className='space-y-1'>
          <div
            className={`group flex items-center gap-2 rounded-md border px-2 py-1.5 ${
              isSelected ? 'border-primary' : 'border-border hover:bg-muted/40'
            }`}
            style={{ marginLeft: `${depth * 14}px` }}
          >
            {hasChildren ? (
              <button
                type='button'
                className='inline-flex h-5 w-5 items-center justify-center rounded hover:bg-muted'
                onClick={(event) => {
                  event.stopPropagation()
                  toggleExpandNode(nodeId)
                }}
              >
                {isExpanded ? (
                  <ChevronDown className='h-3.5 w-3.5 text-muted-foreground' />
                ) : (
                  <ChevronRight className='h-3.5 w-3.5 text-muted-foreground' />
                )}
              </button>
            ) : (
              <span className='inline-flex h-5 w-5 items-center justify-center text-muted-foreground'>•</span>
            )}

            <button
              type='button'
              className='flex min-w-0 flex-1 flex-col items-start text-left'
              onClick={() => setSelectedTeam(node)}
            >
              <span className='truncate text-sm font-medium'>{node.teamName || '-'}</span>
              <span className='truncate text-xs text-muted-foreground'>负责人：{leaderLabel}</span>
            </button>

            <DropdownMenu modal={false}>
              <DropdownMenuTrigger asChild>
                <Button
                  type='button'
                  variant='ghost'
                  size='icon'
                  className='h-7 w-7'
                  onClick={(event) => event.stopPropagation()}
                >
                  <MoreHorizontal className='h-3.5 w-3.5' />
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align='end' onClick={(event) => event.stopPropagation()}>
                <DropdownMenuItem onClick={() => openViewTeamDialog(node)} className='gap-2'>
                  <Eye className='h-3.5 w-3.5' />
                  查看
                </DropdownMenuItem>
                <DropdownMenuItem onClick={() => openEditTeamDialog(node)} className='gap-2'>
                  <Pencil className='h-3.5 w-3.5' />
                  编辑
                </DropdownMenuItem>
                <DropdownMenuItem onClick={() => openCreateChildTeamDialog(node)} className='gap-2'>
                  <Plus className='h-3.5 w-3.5' />
                  新增子部门
                </DropdownMenuItem>
                <DropdownMenuSeparator />
                <DropdownMenuItem
                  variant='destructive'
                  onClick={() => setDeletingTeam(node)}
                  className='gap-2'
                >
                  <Trash2 className='h-3.5 w-3.5' />
                  删除
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
          {hasChildren && isExpanded ? renderTreeNodes(node.children, depth + 1) : null}
        </div>
      )
    })
  }

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
        <div className='grid gap-4 xl:grid-cols-[360px_minmax(0,1fr)]'>
          <Card className='overflow-hidden'>
            <CardHeader className='pb-3'>
              <div className='flex items-center justify-between gap-2'>
                <CardTitle className='text-base'>组织结构</CardTitle>
              </div>
              <div className='mt-2 flex items-center gap-2'>
                <Input
                  value={teamKeywordInput}
                  placeholder='按团队名称搜索'
                  onChange={(event) => setTeamKeywordInput(event.target.value)}
                />
                <Button type='button' variant='outline' onClick={resetTeamKeywordFilter} className='gap-2'>
                  <RefreshCw className='h-4 w-4' />
                  重置
                </Button>
              </div>
            </CardHeader>
            <CardContent>
              <div
                className='max-h-[calc(100vh-300px)] min-h-[360px] overflow-auto pr-1'
                onScroll={handleTeamTreeScroll}
              >
                {loading ? (
                  <div className='flex items-center justify-center gap-2 py-8 text-muted-foreground'>
                    <Loader2 className='h-4 w-4 animate-spin' />
                    正在加载组织树...
                  </div>
                ) : teamTree.length === 0 ? (
                  <div className='flex flex-col items-center justify-center gap-4 py-12 text-center text-sm text-muted-foreground'>
                    {currentEstabId ? (
                      <>
                        <p>暂无团队数据，您可以创建一个新的根部门。</p>
                        <Button type='button' onClick={openCreateTeamDialog} className='gap-2'>
                          <Plus className='h-4 w-4' />
                          新增根部门
                        </Button>
                      </>
                    ) : (
                      '未获取到当前企业，请先登录或切换企业'
                    )}
                  </div>
                ) : (
                  <div className='space-y-1'>{renderTreeNodes(teamTree)}</div>
                )}
                {loadingMoreTeams ? (
                  <div className='flex items-center justify-center gap-2 py-3 text-xs text-muted-foreground'>
                    <Loader2 className='h-3.5 w-3.5 animate-spin' />
                    正在加载更多...
                  </div>
                ) : null}
              </div>
            </CardContent>
          </Card>

          <Card className='overflow-hidden'>
            <CardHeader className='pb-3'>
              <div className='flex flex-wrap items-center justify-between gap-2'>
                <div className='space-y-1'>
                  <CardTitle className='text-base'>团队成员</CardTitle>
                </div>
                <Button type='button' onClick={openCreateMemberDialog} className='gap-2'>
                  <Plus className='h-4 w-4' />
                  新增成员
                </Button>
              </div>
            </CardHeader>
            <CardContent>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>用户名</TableHead>
                    <TableHead>团队角色</TableHead>
                    <TableHead className='w-[88px] text-center'>状态</TableHead>
                    <TableHead>加入时间</TableHead>
                    <TableHead className='w-[132px] text-center'>操作</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {memberLoading ? (
                    <TableRow>
                      <TableCell colSpan={5}>
                        <div className='flex items-center justify-center gap-2 py-8 text-muted-foreground'>
                          <Loader2 className='h-4 w-4 animate-spin' />
                          正在加载团队成员...
                        </div>
                      </TableCell>
                    </TableRow>
                  ) : members.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={5} className='py-8 text-center text-muted-foreground'>
                        {selectedTeam ? '暂无团队成员' : '请先在左侧选择团队'}
                      </TableCell>
                    </TableRow>
                  ) : (
                    members.map((item) => {
                      const title = item.displayName || item.username || '-'
                      const subtitle =
                        item.displayName && item.username
                          ? `${item.username}${item.userCode ? `（${item.userCode}）` : ''}`
                          : item.userCode
                            ? `编码：${item.userCode}`
                            : ''
                      return (
                        <TableRow key={item.id}>
                          <TableCell>
                            <div className='flex flex-col'>
                              <span>{title}</span>
                              {subtitle ? <span className='text-xs text-muted-foreground'>{subtitle}</span> : null}
                            </div>
                          </TableCell>
                          <TableCell>{toRoleInTeamLabel(item.roleInTeam)}</TableCell>
                          <TableCell className='text-center'>
                            <Badge variant={item.status === 1 ? 'default' : 'secondary'}>
                              {toStatusLabel(item.status)}
                            </Badge>
                          </TableCell>
                          <TableCell>{formatDateTime(item.joinTime)}</TableCell>
                          <TableCell>
                            <div className='flex items-center justify-center gap-1'>
                              <Button
                                type='button'
                                variant='ghost'
                                size='icon'
                                className='h-8 w-8'
                                onClick={() => openEditMemberDialog(item)}
                              >
                                <Pencil className='h-4 w-4' />
                              </Button>
                              <Button
                                type='button'
                                variant='ghost'
                                size='icon'
                                className='h-8 w-8 text-destructive'
                                onClick={() => setDeletingMember(item)}
                              >
                                <Trash2 className='h-4 w-4' />
                              </Button>
                            </div>
                          </TableCell>
                        </TableRow>
                      )
                    })
                  )}
                </TableBody>
              </Table>

              <PageToolbar
                page={memberQuery.currentPage ?? 1}
                size={memberQuery.pageSize ?? 10}
                total={memberTotal}
                loading={memberLoading}
                onPageChange={(page) => setMemberQuery((prev) => ({ ...prev, currentPage: page }))}
                onPageSizeChange={(size) =>
                  setMemberQuery((prev) => ({ ...prev, pageSize: size, currentPage: 1 }))
                }
              />
            </CardContent>
          </Card>
        </div>
      </Main>

      <Dialog
        open={teamDialogOpen}
        onOpenChange={(open) => {
          if (!open) {
            closeTeamDialog()
            return
          }
          setTeamDialogOpen(true)
        }}
      >
        <DialogContent className='max-h-[92vh] overflow-y-auto sm:max-w-2xl'>
          <DialogHeader>
            <DialogTitle>
              {isTeamViewMode ? '查看团队' : editingTeam ? '编辑团队' : '新建团队'}
            </DialogTitle>
          </DialogHeader>
          <Form {...teamForm}>
            <form className='grid gap-4 md:grid-cols-2' onSubmit={teamForm.handleSubmit(submitTeam)}>
              <FormField
                control={teamForm.control}
                name='teamName'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>团队名称</FormLabel>
                    <FormControl>
                      <Input {...field} disabled={isTeamViewMode} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={teamForm.control}
                name='leaderUserId'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>负责人用户</FormLabel>
                    {isTeamViewMode ? (
                      <FormControl>
                        <Input
                          value={buildCandidateLabel(selectedTeamLeader) || '-'}
                          disabled
                        />
                      </FormControl>
                    ) : (
                      <div className='space-y-2'>
                        <FormControl>
                          <Input
                            value={teamLeaderKeyword}
                            placeholder='输入用户名进行联想，例如：refinex'
                            onChange={(event) => {
                              setTeamLeaderKeyword(event.target.value)
                              setSelectedTeamLeader(null)
                              setTeamLeaderCandidates([])
                              field.onChange('')
                            }}
                          />
                        </FormControl>

                        {teamLeaderLoading ? (
                          <div className='flex items-center gap-2 rounded-md border px-3 py-2 text-sm text-muted-foreground'>
                            <Loader2 className='h-3.5 w-3.5 animate-spin' />
                            正在检索用户...
                          </div>
                        ) : null}

                        {!teamLeaderLoading &&
                        !selectedTeamLeader &&
                        !field.value &&
                        teamLeaderKeyword.trim().length > 0 &&
                        teamLeaderCandidates.length === 0 ? (
                          <div className='rounded-md border px-3 py-2 text-sm text-muted-foreground'>
                            未找到可选负责人
                          </div>
                        ) : null}

                        {!teamLeaderLoading && teamLeaderCandidates.length > 0 ? (
                          <div className='max-h-48 overflow-auto rounded-md border'>
                            {teamLeaderCandidates.map((candidate) => (
                              <button
                                key={candidate.userId}
                                type='button'
                                className='flex w-full items-center justify-between border-b px-3 py-2 text-left text-sm last:border-b-0 hover:bg-muted/40'
                                onClick={() => selectTeamLeaderCandidate(candidate, field.onChange)}
                              >
                                <span className='truncate'>
                                  {candidate.username || candidate.displayName || '-'}
                                </span>
                                <span className='ml-2 text-xs text-muted-foreground'>
                                  {candidate.userCode || '-'}
                                </span>
                              </button>
                            ))}
                          </div>
                        ) : null}
                      </div>
                    )}
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={teamForm.control}
                name='status'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>状态</FormLabel>
                    <Select onValueChange={field.onChange} value={field.value} disabled={isTeamViewMode}>
                      <FormControl>
                        <SelectTrigger className='w-full'>
                          <SelectValue />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        <SelectItem value='1'>启用</SelectItem>
                        <SelectItem value='2'>停用</SelectItem>
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={teamForm.control}
                name='sort'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>排序</FormLabel>
                    <FormControl>
                      <Input {...field} disabled={isTeamViewMode} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={teamForm.control}
                name='remark'
                render={({ field }) => (
                  <FormItem className='md:col-span-2'>
                    <FormLabel>备注</FormLabel>
                    <FormControl>
                      <Textarea
                        rows={3}
                        {...field}
                        disabled={isTeamViewMode}
                        placeholder='请输入备注信息'
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <DialogFooter className='md:col-span-2'>
                <Button type='button' variant='outline' onClick={closeTeamDialog}>
                  {isTeamViewMode ? '关闭' : '取消'}
                </Button>
                {!isTeamViewMode ? (
                  <Button type='submit' disabled={savingTeam}>
                    {savingTeam ? (
                      <>
                        <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                        保存中...
                      </>
                    ) : (
                      '保存团队'
                    )}
                  </Button>
                ) : null}
              </DialogFooter>
            </form>
          </Form>
        </DialogContent>
      </Dialog>

      <Dialog open={memberDialogOpen} onOpenChange={setMemberDialogOpen}>
        <DialogContent className='max-h-[92vh] overflow-y-auto sm:max-w-xl'>
          <DialogHeader>
            <DialogTitle>{editingMember ? '编辑团队成员' : '新增团队成员'}</DialogTitle>
            <DialogDescription>维护团队成员关系与角色分工。</DialogDescription>
          </DialogHeader>
          <Form {...memberForm}>
            <form className='grid gap-4' onSubmit={memberForm.handleSubmit(submitMember)}>
              <FormField
                control={memberForm.control}
                name='userId'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>成员用户</FormLabel>
                    {editingMember ? (
                      <FormControl>
                        <Input
                          value={buildCandidateLabel(selectedCandidate) || '-'}
                          disabled
                        />
                      </FormControl>
                    ) : (
                      <div className='space-y-2'>
                        <FormControl>
                          <Input
                            value={memberKeyword}
                            placeholder='输入用户名进行联想，例如：refinex'
                            onChange={(event) => {
                              setMemberKeyword(event.target.value)
                              setSelectedCandidate(null)
                              setMemberCandidates([])
                              field.onChange('')
                            }}
                          />
                        </FormControl>

                        {memberCandidateLoading ? (
                          <div className='flex items-center gap-2 rounded-md border px-3 py-2 text-sm text-muted-foreground'>
                            <Loader2 className='h-3.5 w-3.5 animate-spin' />
                            正在检索用户...
                          </div>
                        ) : null}

                        {!memberCandidateLoading &&
                        !selectedCandidate &&
                        !field.value &&
                        memberKeyword.trim().length > 0 &&
                        memberCandidates.length === 0 ? (
                          <div className='rounded-md border px-3 py-2 text-sm text-muted-foreground'>
                            未找到可加入当前团队的用户
                          </div>
                        ) : null}

                        {!memberCandidateLoading && memberCandidates.length > 0 ? (
                          <div className='max-h-48 overflow-auto rounded-md border'>
                            {memberCandidates.map((candidate) => (
                              <button
                                key={candidate.userId}
                                type='button'
                                className='flex w-full items-center justify-between border-b px-3 py-2 text-left text-sm last:border-b-0 hover:bg-muted/40'
                                onClick={() => selectMemberCandidate(candidate, field.onChange)}
                              >
                                <span className='truncate'>
                                  {candidate.username || candidate.displayName || '-'}
                                </span>
                                <span className='ml-2 text-xs text-muted-foreground'>
                                  {candidate.userCode || '-'}
                                </span>
                              </button>
                            ))}
                          </div>
                        ) : null}
                      </div>
                    )}
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={memberForm.control}
                name='roleInTeam'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>团队角色</FormLabel>
                    <Select onValueChange={field.onChange} value={field.value}>
                      <FormControl>
                        <SelectTrigger className='w-full'>
                          <SelectValue />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        <SelectItem value='1'>负责人</SelectItem>
                        <SelectItem value='2'>成员</SelectItem>
                        <SelectItem value='3'>协作</SelectItem>
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={memberForm.control}
                name='status'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>状态</FormLabel>
                    <Select onValueChange={field.onChange} value={field.value}>
                      <FormControl>
                        <SelectTrigger className='w-full'>
                          <SelectValue />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        <SelectItem value='1'>启用</SelectItem>
                        <SelectItem value='2'>停用</SelectItem>
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={memberForm.control}
                name='joinTime'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>加入时间</FormLabel>
                    <FormControl>
                      <Input {...field} type='datetime-local' />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <DialogFooter>
                <Button type='button' variant='outline' onClick={closeMemberDialog}>
                  取消
                </Button>
                <Button type='submit' disabled={savingMember}>
                  {savingMember ? (
                    <>
                      <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                      保存中...
                    </>
                  ) : (
                    '保存成员'
                  )}
                </Button>
              </DialogFooter>
            </form>
          </Form>
        </DialogContent>
      </Dialog>

      <ConfirmDialog
        open={Boolean(deletingTeam)}
        onOpenChange={(open) => !open && setDeletingTeam(null)}
        title='删除团队'
        desc={`将删除团队「${deletingTeam?.teamName ?? '-'}」，该操作不可撤销。`}
        confirmText={deletingTeamLoading ? '删除中...' : '确认删除'}
        destructive
        isLoading={deletingTeamLoading}
        handleConfirm={() => void confirmDeleteTeam()}
      />

      <ConfirmDialog
        open={Boolean(deletingMember)}
        onOpenChange={(open) => !open && setDeletingMember(null)}
        title='删除团队成员'
        desc='将解除该用户与团队的成员关系，删除后不可恢复。'
        confirmText={deletingMemberLoading ? '删除中...' : '确认删除'}
        destructive
        isLoading={deletingMemberLoading}
        handleConfirm={() => void confirmDeleteMember()}
      />
    </>
  )
}
