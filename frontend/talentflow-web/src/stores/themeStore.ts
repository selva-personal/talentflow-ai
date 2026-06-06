import { create } from 'zustand'
import { persist } from 'zustand/middleware'

export type ThemeMode = 'light' | 'dark' | 'system'

interface ThemeState {
  mode: ThemeMode
  setMode: (mode: ThemeMode) => void
  resolved: () => 'light' | 'dark'
}

function systemTheme(): 'light' | 'dark' {
  if (typeof window === 'undefined') return 'dark'
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
}

export function applyTheme(mode: ThemeMode) {
  const resolved = mode === 'system' ? systemTheme() : mode
  document.documentElement.setAttribute('data-theme', resolved)
  document.documentElement.classList.toggle('dark', resolved === 'dark')
}

export const useThemeStore = create<ThemeState>()(
  persist(
    (set, get) => ({
      mode: 'system',
      setMode: (mode) => {
        set({ mode })
        applyTheme(mode)
      },
      resolved: () => {
        const { mode } = get()
        return mode === 'system' ? systemTheme() : mode
      },
    }),
    {
      name: 'talentflow-theme',
      onRehydrateStorage: () => (state) => {
        if (state) applyTheme(state.mode)
      },
    }
  )
)

if (typeof window !== 'undefined') {
  window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', () => {
    const { mode } = useThemeStore.getState()
    if (mode === 'system') applyTheme('system')
  })
}
