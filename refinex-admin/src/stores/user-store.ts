import { create } from 'zustand'
import type { UserEstab, UserInfo } from '@/features/user/api/user-api'

interface UserStoreState {
  profile: UserInfo | null
  estabs: UserEstab[]
  loading: boolean
  setProfile: (profile: UserInfo | null) => void
  setEstabs: (estabs: UserEstab[]) => void
  setLoading: (loading: boolean) => void
  reset: () => void
}

export const useUserStore = create<UserStoreState>()((set) => ({
  profile: null,
  estabs: [],
  loading: false,
  setProfile: (profile) => set({ profile }),
  setEstabs: (estabs) => set({ estabs }),
  setLoading: (loading) => set({ loading }),
  reset: () => set({ profile: null, estabs: [], loading: false }),
}))
