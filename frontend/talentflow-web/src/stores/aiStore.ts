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
  lastSuccessfulRequest?: string | null
  lastFailureReason?: string | null
}

export interface AdminAiStatus extends AiStatus {
  fallbackMode: string
  configuredMode: string
  configuredProvider: string
  apiKeyConfigured: boolean
  geminiModel: string
  totalRetryAttempts: number
  quotaFailureCount: number
  fallbackUsageCount: number
  lastRequestAt?: string | null
  lastFailureAt?: string | null
}

interface AiState {
  fallbackActive: boolean
  provider: string
  geminiStatus: string
  quotaStatus: string
  lastSuccessfulRequest: string | null
  lastFailureReason: string | null
  setFromMeta: (meta: AiMeta) => void
  setStatus: (status: AiStatus) => void
  clear: () => void
}

export const useAiStore = create<AiState>((set) => ({
  fallbackActive: false,
  provider: 'gemini',
  geminiStatus: 'UNKNOWN',
  quotaStatus: 'UNKNOWN',
  lastSuccessfulRequest: null,
  lastFailureReason: null,
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
      lastSuccessfulRequest: status.lastSuccessfulRequest ?? null,
      lastFailureReason: status.lastFailureReason ?? null,
    }),
  clear: () =>
    set({
      fallbackActive: false,
      provider: 'gemini',
      geminiStatus: 'UNKNOWN',
      quotaStatus: 'UNKNOWN',
      lastSuccessfulRequest: null,
      lastFailureReason: null,
    }),
}))
