import { Navigate, Outlet } from 'react-router-dom'
import { useAuthStore } from '@/stores/authStore'

export function ProtectedRoute() {
  if (!useAuthStore.getState().isAuthenticated()) {
    return <Navigate to="/login" replace />
  }
  return <Outlet />
}
