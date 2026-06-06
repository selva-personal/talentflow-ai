import { useQuery } from '@tanstack/react-query'
import { apiGet, type SpringPage } from '@/lib/api'
import { Card } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { EmptyState } from '@/components/ui/empty-state'
import { Shield, Users, FileText, Brain, Activity } from 'lucide-react'
import { Link } from 'react-router-dom'

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

export function AdminPage() {
  const { data, isLoading } = useQuery({
    queryKey: ['admin-dashboard'],
    queryFn: () => apiGet<AdminDashboard>('/admin/dashboard'),
  })

  const { data: users, isLoading: usersLoading } = useQuery({
    queryKey: ['admin-users'],
    queryFn: () => apiGet<SpringPage<UserRow>>('/admin/users?size=10'),
  })

  const { data: aiStatus, isLoading: aiStatusLoading } = useQuery({
    queryKey: ['admin-ai-status'],
    queryFn: () =>
      apiGet<{
        geminiStatus: string
        quotaStatus: string
        fallbackMode: string
        activeProvider: string
        configuredMode: string
        apiKeyConfigured: boolean
        totalRetryAttempts: number
        quotaFailureCount: number
      }>('/admin/ai-status'),
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

      <h2 className="mt-10 text-xl font-semibold">AI Status</h2>
      <Card className="mt-4">
        {aiStatusLoading ? (
          <Skeleton className="h-24 w-full" />
        ) : (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <div>
              <p className="text-sm text-muted">Gemini Status</p>
              <p className="text-lg font-semibold">{aiStatus?.geminiStatus ?? '—'}</p>
            </div>
            <div>
              <p className="text-sm text-muted">Quota Status</p>
              <p className="text-lg font-semibold">{aiStatus?.quotaStatus ?? '—'}</p>
            </div>
            <div>
              <p className="text-sm text-muted">Fallback Mode</p>
              <p className="text-lg font-semibold">{aiStatus?.fallbackMode ?? '—'}</p>
            </div>
            <div>
              <p className="text-sm text-muted">Active Provider</p>
              <p className="text-lg font-semibold capitalize">{aiStatus?.activeProvider ?? '—'}</p>
            </div>
            <div>
              <p className="text-sm text-muted">Configured Mode</p>
              <p className="text-sm">{aiStatus?.configuredMode ?? '—'}</p>
            </div>
            <div>
              <p className="text-sm text-muted">API Key</p>
              <p className="text-sm">{aiStatus?.apiKeyConfigured ? 'Configured' : 'Missing'}</p>
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
        {!usersLoading && (
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
              {(users?.content ?? []).map((u) => (
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
