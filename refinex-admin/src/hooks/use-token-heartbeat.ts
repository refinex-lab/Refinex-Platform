import { useEffect, useRef } from 'react'
import { getTokenInfo } from '@/features/auth/api/auth-api'
import { useAuthStore } from '@/stores/auth-store'

const HEARTBEAT_INTERVAL_MS = 10 * 60 * 1000 // 10 minutes

export function useTokenHeartbeat() {
  const timerRef = useRef<ReturnType<typeof setInterval> | null>(null)

  useEffect(() => {
    function sendHeartbeat() {
      const { auth } = useAuthStore.getState()
      if (!auth.isAuthenticated()) return
      if (document.visibilityState !== 'visible') return
      getTokenInfo().catch(() => {})
    }

    function handleVisibilityChange() {
      if (document.visibilityState === 'visible') {
        sendHeartbeat()
      }
    }

    timerRef.current = setInterval(sendHeartbeat, HEARTBEAT_INTERVAL_MS)
    document.addEventListener('visibilitychange', handleVisibilityChange)

    return () => {
      if (timerRef.current) clearInterval(timerRef.current)
      document.removeEventListener('visibilitychange', handleVisibilityChange)
    }
  }, [])
}
