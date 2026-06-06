import { create } from 'zustand'
import { persist } from 'zustand/middleware'

export interface AuthUser {
  id: number
  email: string
  firstName: string
  lastName: string
  role: string
}

interface AuthState {
  accessToken: string | null
  refreshToken: string | null
  user: AuthUser | null
  setTokens: (access: string, refresh: string, user: AuthUser) => void
  logout: () => void
  isAuthenticated: () => boolean
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      accessToken: null,
      refreshToken: null,
      user: null,
      setTokens: (access, refresh, user) =>
        set({ accessToken: access, refreshToken: refresh, user }),
      logout: () => {
        set({ accessToken: null, refreshToken: null, user: null })
      },
      isAuthenticated: () => !!get().accessToken,
    }),
    { name: 'talentflow-auth' }
  )
)
