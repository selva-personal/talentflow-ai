import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios'
import { useAuthStore } from '@/stores/authStore'

/**
 * API base URL from Vite env (required in production builds).
 * Local dev: set VITE_API_BASE_URL in frontend/talentflow-web/.env
 * Netlify: Site settings → Environment variables
 */
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
  async (error: AxiosError<{ message?: string }>) => {
    const original = error.config
    if (error.response?.status === 401 && original && !(original as { _retry?: boolean })._retry) {
      const refresh = useAuthStore.getState().refreshToken
      if (refresh) {
        try {
          ;(original as { _retry?: boolean })._retry = true
          const { data } = await axios.post(`${API_BASE}/auth/refresh`, { refreshToken: refresh })
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
  data: T
  timestamp: string
}

export async function apiGet<T>(url: string): Promise<T> {
  const { data } = await api.get<ApiResponse<T>>(url)
  return data.data
}

export async function apiPost<T>(url: string, body?: unknown): Promise<T> {
  const { data } = await api.post<ApiResponse<T>>(url, body)
  return data.data
}

export async function apiUpload<T>(url: string, file: File): Promise<T> {
  const form = new FormData()
  form.append('file', file)
  const { data } = await api.post<ApiResponse<T>>(url, form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return data.data
}
