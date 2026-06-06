import { toast } from '@/lib/toast'
import { useAiStore, type AiMeta } from '@/stores/aiStore'

export function extractAiMeta<T>(data: T): AiMeta | undefined {
  if (data && typeof data === 'object' && 'aiMeta' in data) {
    return (data as T & { aiMeta: AiMeta }).aiMeta
  }
  return undefined
}

export function applyAiMeta<T>(data: T, notify = false): T {
  const meta = extractAiMeta(data)
  if (meta) {
    useAiStore.getState().setFromMeta(meta)
    if (notify && meta.fallbackActive) {
      toast.warning(
        'Offline AI Mode',
        meta.message ?? 'AI service temporarily unavailable. Using offline analysis.'
      )
    }
  }
  return data
}

/** @deprecated use applyAiMeta */
export function notifyAiMeta<T>(data: T): T {
  return applyAiMeta(data, true)
}
