import { create } from 'zustand'

export interface AiMeta {
  provider: string
  fallbackActive: boolean
  message?: string
}

export interface AiStatus {
  geminiStatus: string
  quotaStatus: string
  fallbackActive: boolean
  activeProvider: string
}

interface AiState {
  fallbackActive: boolean
  provider: string
  geminiStatus: string
  quotaStatus: string
  setFromMeta: (meta: AiMeta) => void
  setStatus: (status: AiStatus) => void
  clear: () => void
}

export const useAiStore = create<AiState>((set) => ({
  fallbackActive: false,
  provider: 'gemini',
  geminiStatus: 'UNKNOWN',
  quotaStatus: 'UNKNOWN',
  setFromMeta: (meta) =>
    set({
      fallbackActive: meta.fallbackActive,
      provider: meta.provider,
    }),
  setStatus: (status) =>
    set({
      fallbackActive: status.fallbackActive,
      provider: status.activeProvider,
      geminiStatus: status.geminiStatus,
      quotaStatus: status.quotaStatus,
    }),
  clear: () =>
    set({
      fallbackActive: false,
      provider: 'gemini',
      geminiStatus: 'UNKNOWN',
      quotaStatus: 'UNKNOWN',
    }),
}))
