import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios'
import { useAuthStore } from '@/stores/authStore'
import { notifyAiMeta, applyAiMeta } from '@/lib/aiMeta'

function resolveApiBase(): string {
  const configured = import.meta.env.VITE_API_BASE_URL
  if (configured && configured.length > 0) {
    return configured.replace(/\/$/, '')
  }
  if (import.meta.env.DEV) {
    return 'http://localhost:8080/api/v1'
  }
  throw new Error(
    'VITE_API_BASE_URL is not set. Configure it in Netlify environment variables before deploying.'
  )
}

const API_BASE = resolveApiBase()

export const api = axios.create({
  baseURL: API_BASE,
  headers: { 'Content-Type': 'application/json' },
  timeout: 120_000,
})

api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = useAuthStore.getState().accessToken
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (res) => res,
  async (error: AxiosError<ApiResponse<unknown>>) => {
    const original = error.config
    if (error.response?.status === 401 && original && !(original as { _retry?: boolean })._retry) {
      const refresh = useAuthStore.getState().refreshToken
      if (refresh) {
        try {
          ;(original as { _retry?: boolean })._retry = true
          const { data } = await axios.post<ApiResponse<AuthRefreshPayload>>(
            `${API_BASE}/auth/refresh`,
            { refreshToken: refresh }
          )
          const payload = data.data
          useAuthStore.getState().setTokens(payload.accessToken, payload.refreshToken, payload.user)
          original.headers.Authorization = `Bearer ${payload.accessToken}`
          return api(original)
        } catch {
          useAuthStore.getState().logout()
        }
      }
    }
    return Promise.reject(error)
  }
)

export interface ApiResponse<T> {
  success: boolean
  message: string
  errorCode?: string
  data: T
  timestamp: string
}

interface AuthRefreshPayload {
  accessToken: string
  refreshToken: string
  user: { id: number; email: string; firstName: string; lastName: string; role: string }
}

export interface SpringPage<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export function getApiError(error: unknown, fallback = 'Something went wrong'): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as ApiResponse<unknown> | { message?: string; errorCode?: string } | undefined
    if (data && typeof data === 'object') {
      if ('errorCode' in data && data.errorCode === 'AI_QUOTA_EXCEEDED') {
        return 'AI service is temporarily unavailable. Please try again later.'
      }
      if ('message' in data && data.message) {
        return String(data.message)
      }
    }
    if (error.response?.status === 401) return 'Session expired. Please sign in again.'
    if (error.response?.status === 403) return 'You do not have permission for this action.'
    if (!error.response) return 'Network error. Check your connection and API URL.'
    return error.message || fallback
  }
  if (error instanceof Error) return error.message
  return fallback
}

export async function apiGet<T>(url: string): Promise<T> {
  const { data } = await api.get<ApiResponse<T>>(url)
  if (!data.success) throw new Error(data.message || 'Request failed')
  return applyAiMeta(data.data, false)
}

export async function apiPost<T>(url: string, body?: unknown): Promise<T> {
  const { data } = await api.post<ApiResponse<T>>(url, body)
  if (!data.success) throw new Error(data.message || 'Request failed')
  return notifyAiMeta(data.data)
}

export async function apiPatch<T>(url: string, body?: unknown): Promise<T> {
  const { data } = await api.patch<ApiResponse<T>>(url, body)
  if (!data.success) throw new Error(data.message || 'Request failed')
  return notifyAiMeta(data.data)
}

export async function apiUpload<T>(url: string, file: File): Promise<T> {
  const form = new FormData()
  form.append('file', file)
  const { data } = await api.post<ApiResponse<T>>(url, form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  if (!data.success) throw new Error(data.message || 'Upload failed')
  return notifyAiMeta(data.data)
}
