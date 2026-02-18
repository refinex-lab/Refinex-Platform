import { useEffect } from 'react'
import { getCurrentUserEstabs, getCurrentUserInfo } from '@/features/user/api/user-api'
import { handleServerError } from '@/lib/handle-server-error'
import { useAuthStore } from '@/stores/auth-store'
import { useUserStore } from '@/stores/user-store'

export function useBootstrapCurrentUser() {
  const accessToken = useAuthStore((state) => state.auth.accessToken)
  const setAuthUser = useAuthStore((state) => state.auth.setUser)
  const setProfile = useUserStore((state) => state.setProfile)
  const setEstabs = useUserStore((state) => state.setEstabs)
  const setLoading = useUserStore((state) => state.setLoading)
  const resetUserStore = useUserStore((state) => state.reset)

  useEffect(() => {
    if (!accessToken) {
      resetUserStore()
      return
    }

    let canceled = false

    const loadCurrentUser = async () => {
      setLoading(true)
      try {
        const [profile, estabs] = await Promise.all([
          getCurrentUserInfo(),
          getCurrentUserEstabs(),
        ])

        if (canceled) return

        setProfile(profile)
        setEstabs(estabs)
        const currentAuthUser = useAuthStore.getState().auth.user
        setAuthUser({
          ...currentAuthUser,
          userId: profile.userId ?? currentAuthUser?.userId,
          userCode: profile.userCode ?? currentAuthUser?.userCode,
          username: profile.username ?? currentAuthUser?.username,
          displayName: profile.displayName ?? currentAuthUser?.displayName,
          nickname: profile.nickname ?? currentAuthUser?.nickname,
          avatarUrl: profile.avatarUrl ?? currentAuthUser?.avatarUrl,
          estabId: currentAuthUser?.estabId ?? profile.primaryEstabId,
          primaryEstabId:
            profile.primaryEstabId ?? currentAuthUser?.primaryEstabId,
          teamId: profile.primaryTeamId ?? currentAuthUser?.teamId,
          estabAdmin:
            profile.estabAdmin == null
              ? currentAuthUser?.estabAdmin
              : profile.estabAdmin,
        })
      } catch (error) {
        if (!canceled) {
          handleServerError(error)
        }
      } finally {
        if (!canceled) {
          setLoading(false)
        }
      }
    }

    loadCurrentUser()

    return () => {
      canceled = true
    }
  }, [
    accessToken,
    resetUserStore,
    setAuthUser,
    setEstabs,
    setLoading,
    setProfile,
  ])
}
