import { useEffect, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { apiGet, apiPost, getApiError, type SpringPage } from '@/lib/api'
import { toast } from '@/lib/toast'
import { Card } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Select } from '@/components/ui/select'
import { Skeleton } from '@/components/ui/skeleton'
import { EmptyState } from '@/components/ui/empty-state'
import { Shield, Users, FileText, Brain, Activity, Zap } from 'lucide-react'
import { Link } from 'react-router-dom'
import type { AdminAiStatus } from '@/stores/aiStore'
import { cn } from '@/lib/utils'

interface AdminDashboard {
  totalUsers: number
  totalResumes: number
  totalInterviews: number
  totalMockSessions: number
  totalNotifications: number
  recentActivity: { id: number; userId: number; action: string; createdAt: string }[]
}

interface UserRow {
  id: number
  email: string
  firstName: string
  lastName: string
  role: string
  enabled: boolean
  createdAt: string
}

function statusColor(value: string) {
  const v = value.toUpperCase()
  if (v === 'ONLINE' || v === 'AVAILABLE' || v === 'GEMINI') return 'text-success'
  if (v === 'EXHAUSTED' || v === 'OFFLINE' || v === 'FALLBACK' || v === 'ENABLED') return 'text-warning'
  return 'text-muted'
}

function formatTime(iso?: string | null) {
  if (!iso) return '—'
  return new Date(iso).toLocaleString()
}

export function AdminPage() {
  const queryClient = useQueryClient()
  const [testPrompt, setTestPrompt] = useState('Tell me a joke')
  const [testResult, setTestResult] = useState<{ provider: string; response: string } | null>(null)
  const [selectedProvider, setSelectedProvider] = useState('gemini')

  const { data, isLoading, isError: dashboardError } = useQuery({
    queryKey: ['admin-dashboard'],
    queryFn: () => apiGet<AdminDashboard>('/admin/dashboard'),
  })

  const { data: users, isLoading: usersLoading, isError: usersError } = useQuery({
    queryKey: ['admin-users'],
    queryFn: () => apiGet<SpringPage<UserRow>>('/admin/users?size=10'),
  })

  const { data: aiStatus, isLoading: aiStatusLoading, isError: aiStatusError } = useQuery({
    queryKey: ['admin-ai-status'],
    queryFn: () => apiGet<AdminAiStatus>('/admin/ai-status'),
    refetchInterval: 30_000,
  })

  useEffect(() => {
    if (aiStatus?.configuredProvider) {
      setSelectedProvider(aiStatus.configuredProvider)
    }
  }, [aiStatus?.configuredProvider])

  const switchProvider = useMutation({
    mutationFn: (provider: string) => apiPost('/admin/ai-provider', { provider }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-ai-status'] })
      queryClient.invalidateQueries({ queryKey: ['ai-diagnostics'] })
      queryClient.invalidateQueries({ queryKey: ['ai-status'] })
      toast.success('Provider updated', `Preferred provider: ${selectedProvider}`)
    },
    onError: (err) => toast.error('Switch failed', getApiError(err)),
  })

  const testAi = useMutation({
    mutationFn: () =>
      apiPost<{ provider: string; response: string; fallbackActive: boolean }>('/ai/test', {
        prompt: testPrompt,
      }),
    onSuccess: (result) => {
      setTestResult({ provider: result.provider, response: result.response })
      queryClient.invalidateQueries({ queryKey: ['admin-ai-status'] })
      queryClient.invalidateQueries({ queryKey: ['ai-status'] })
      if (result.provider !== 'fallback') {
        toast.success(`${result.provider} online`, 'Real AI response received')
      } else {
        toast.warning('Fallback mode', 'All providers unavailable — offline response used')
      }
    },
    onError: (err) => toast.error('AI test failed', getApiError(err)),
  })

  const stats = [
    { label: 'Users', value: data?.totalUsers ?? 0, icon: Users },
    { label: 'Resumes', value: data?.totalResumes ?? 0, icon: FileText },
    { label: 'Interviews', value: data?.totalInterviews ?? 0, icon: Brain },
    { label: 'Mock Sessions', value: data?.totalMockSessions ?? 0, icon: Activity },
  ]

  return (
    <div>
      <div className="flex items-center gap-3">
        <Shield className="h-8 w-8 text-primary" />
        <div>
          <h1 className="text-3xl font-bold">Admin Console</h1>
          <p className="text-muted">System overview and user management</p>
        </div>
      </div>

      <div className="mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {isLoading
          ? [1, 2, 3, 4].map((i) => (
              <Card key={i}>
                <Skeleton className="h-4 w-24" />
                <Skeleton className="mt-3 h-8 w-16" />
              </Card>
            ))
          : dashboardError
            ? (
                <Card className="sm:col-span-2 lg:col-span-4">
                  <EmptyState icon={Activity} title="Dashboard unavailable" description="Could not load admin metrics." />
                </Card>
              )
            : stats.map((s) => (
                <Card key={s.label} className="flex items-center gap-4">
                  <s.icon className="h-8 w-8 text-primary" />
                  <div>
                    <p className="text-sm text-muted">{s.label}</p>
                    <p className="text-2xl font-bold">{s.value}</p>
                  </div>
                </Card>
              ))}
      </div>

      <div className="mt-10 flex items-center justify-between gap-4">
        <h2 className="text-xl font-semibold">AI Monitor</h2>
        <Link to="/app/admin/ai-diagnostics" className="text-sm font-medium text-primary hover:underline">
          Full AI Diagnostics →
        </Link>
      </div>
      <Card className="mt-4">
        {aiStatusLoading ? (
          <Skeleton className="h-40 w-full" />
        ) : aiStatusError ? (
          <EmptyState icon={Zap} title="AI status unavailable" description="Could not load AI monitor data." />
        ) : (
          <div className="space-y-6">
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
              <div>
                <p className="text-sm text-muted">AI Provider</p>
                <p className={cn('text-lg font-semibold capitalize', statusColor(aiStatus?.activeProvider ?? ''))}>
                  {aiStatus?.activeProvider ?? '—'}
                </p>
              </div>
              <div>
                <p className="text-sm text-muted">Gemini Status</p>
                <p className={cn('text-lg font-semibold', statusColor(aiStatus?.geminiStatus ?? ''))}>
                  {aiStatus?.geminiStatus ?? '—'}
                </p>
              </div>
              <div>
                <p className="text-sm text-muted">Quota Status</p>
                <p className={cn('text-lg font-semibold', statusColor(aiStatus?.quotaStatus ?? ''))}>
                  {aiStatus?.quotaStatus ?? '—'}
                </p>
              </div>
              <div>
                <p className="text-sm text-muted">Configured Mode</p>
                <p className="text-lg font-semibold">{aiStatus?.configuredMode ?? '—'}</p>
              </div>
              <div>
                <p className="text-sm text-muted">Last Request</p>
                <p className="text-sm">{formatTime(aiStatus?.lastRequestAt)}</p>
              </div>
              <div>
                <p className="text-sm text-muted">Last Success</p>
                <p className="text-sm">{formatTime(aiStatus?.lastSuccessfulRequest)}</p>
              </div>
              <div>
                <p className="text-sm text-muted">Last Failure</p>
                <p className="text-sm">{formatTime(aiStatus?.lastFailureAt)}</p>
              </div>
              <div>
                <p className="text-sm text-muted">Fallback Usage Count</p>
                <p className="text-lg font-semibold">{aiStatus?.fallbackUsageCount ?? 0}</p>
              </div>
              <div className="sm:col-span-2">
                <p className="text-sm text-muted">Last Failure Reason</p>
                <p className="text-sm text-warning">{aiStatus?.lastFailureReason ?? '—'}</p>
              </div>
              <div>
                <p className="text-sm text-muted">API Key</p>
                <p className="text-sm">{aiStatus?.apiKeyConfigured ? 'Configured' : 'Missing'}</p>
              </div>
              <div>
                <p className="text-sm text-muted">Model</p>
                <p className="text-sm">{aiStatus?.geminiModel ?? '—'}</p>
              </div>
              <div>
                <p className="text-sm text-muted">Quota Failures</p>
                <p className="text-sm">{aiStatus?.quotaFailureCount ?? 0}</p>
              </div>
              <div>
                <p className="text-sm text-muted">Retry Attempts</p>
                <p className="text-sm">{aiStatus?.totalRetryAttempts ?? 0}</p>
              </div>
            </div>

            <div className="border-t border-border pt-4">
              <p className="mb-2 text-sm font-medium">Switch preferred provider</p>
              <div className="flex flex-col gap-3 sm:flex-row">
                <Select
                  value={selectedProvider}
                  onChange={(e) => setSelectedProvider(e.target.value)}
                  className="flex-1"
                >
                  <option value="gemini">Gemini</option>
                  <option value="openai">OpenAI</option>
                  <option value="anthropic">Anthropic</option>
                  <option value="ollama">Ollama</option>
                  <option value="fallback">Fallback (offline)</option>
                </Select>
                <Button
                  type="button"
                  variant="secondary"
                  onClick={() => switchProvider.mutate(selectedProvider)}
                  loading={switchProvider.isPending}
                >
                  Apply Provider
                </Button>
              </div>
            </div>

            <div className="border-t border-border pt-4">
              <p className="mb-2 text-sm font-medium">Test AI failover chain</p>
              <div className="flex flex-col gap-3 sm:flex-row">
                <input
                  type="text"
                  value={testPrompt}
                  onChange={(e) => setTestPrompt(e.target.value)}
                  className="flex-1 rounded-xl border border-border bg-bg px-3 py-2 text-sm"
                  placeholder="Enter test prompt"
                />
                <Button type="button" onClick={() => testAi.mutate()} loading={testAi.isPending}>
                  Run AI Test
                </Button>
              </div>
              {testResult && (
                <div className="mt-3 rounded-xl border border-border bg-bg/50 p-3 text-sm">
                  <p>
                    <span className="text-muted">Provider:</span>{' '}
                    <span className={cn('font-semibold capitalize', statusColor(testResult.provider))}>
                      {testResult.provider}
                    </span>
                  </p>
                  <p className="mt-2 whitespace-pre-wrap">{testResult.response}</p>
                </div>
              )}
            </div>
          </div>
        )}
      </Card>

      <h2 className="mt-10 text-xl font-semibold">Recent activity</h2>
      <Card className="mt-4">
        {isLoading && <Skeleton className="h-20 w-full" />}
        {!isLoading && (data?.recentActivity?.length ?? 0) === 0 && (
          <EmptyState icon={Activity} title="No activity" description="System activity will appear here." />
        )}
        {!isLoading && (data?.recentActivity?.length ?? 0) > 0 && (
          <ul className="divide-y divide-border">
            {data!.recentActivity.map((a) => (
              <li key={a.id} className="flex justify-between py-3 text-sm first:pt-0 last:pb-0">
                <span>
                  User #{a.userId}: {a.action.replace(/_/g, ' ')}
                </span>
                <span className="text-muted">{new Date(a.createdAt).toLocaleString()}</span>
              </li>
            ))}
          </ul>
        )}
      </Card>

      <h2 className="mt-10 text-xl font-semibold">Users</h2>
      <Card className="mt-4 overflow-x-auto">
        {usersLoading && <Skeleton className="h-32 w-full" />}
        {usersError && (
          <EmptyState icon={Users} title="Users unavailable" description="Could not load user list." />
        )}
        {!usersLoading && !usersError && (users?.content?.length ?? 0) === 0 && (
          <EmptyState icon={Users} title="No users" description="Registered users will appear here." />
        )}
        {!usersLoading && !usersError && (users?.content?.length ?? 0) > 0 && (
          <table className="w-full text-left text-sm">
            <thead>
              <tr className="border-b border-border text-muted">
                <th className="pb-3 pr-4">Name</th>
                <th className="pb-3 pr-4">Email</th>
                <th className="pb-3 pr-4">Role</th>
                <th className="pb-3">Joined</th>
              </tr>
            </thead>
            <tbody>
              {users!.content.map((u) => (
                <tr key={u.id} className="border-b border-border/50 last:border-0">
                  <td className="py-3 pr-4">
                    {u.firstName} {u.lastName}
                  </td>
                  <td className="py-3 pr-4">{u.email}</td>
                  <td className="py-3 pr-4">{u.role}</td>
                  <td className="py-3 text-muted">{new Date(u.createdAt).toLocaleDateString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Card>

      <p className="mt-6 text-sm text-muted">
        <Link to="/app/dashboard" className="text-primary hover:underline">
          ← Back to user dashboard
        </Link>
      </p>
    </div>
  )
}
