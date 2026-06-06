import { Navigate, Outlet } from 'react-router-dom'
import { useAuthStore } from '@/stores/authStore'

export function AdminRoute() {
  const accessToken = useAuthStore((s) => s.accessToken)
  const role = useAuthStore((s) => s.user?.role)

  if (!accessToken) {
    return <Navigate to="/login" replace />
  }

  if (role !== 'ADMIN') {
    return <Navigate to="/app/dashboard" replace />
  }

  return <Outlet />
}
