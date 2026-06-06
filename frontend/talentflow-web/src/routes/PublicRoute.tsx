import { Navigate, Outlet } from 'react-router-dom'
import { useAuthStore } from '@/stores/authStore'

export function PublicRoute() {
  const accessToken = useAuthStore((s) => s.accessToken)

  if (accessToken) {
    return <Navigate to="/app/dashboard" replace />
  }

  return <Outlet />
}
