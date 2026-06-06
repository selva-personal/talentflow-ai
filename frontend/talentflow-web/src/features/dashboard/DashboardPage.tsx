import { useQuery } from '@tanstack/react-query'
import { apiGet } from '@/lib/api'
import { Card } from '@/components/ui/card'
import { Link } from 'react-router-dom'
import { motion } from 'framer-motion'
import { Brain, Code2, Map, Upload, Activity, FileText } from 'lucide-react'
import { useAuthStore } from '@/stores/authStore'
import { Skeleton } from '@/components/ui/skeleton'
import { EmptyState } from '@/components/ui/empty-state'

interface DashboardData {
  avgAtsScore: number
  avgInterviewScore: number
  resumeCount: number
  interviewCount: number
  accountStatus: string
  unreadNotifications: number
  recentActivity: { action: string; entityType?: string; createdAt: string }[]
}

export function DashboardPage() {
  const user = useAuthStore((s) => s.user)
  const { data, isLoading, isError } = useQuery({
    queryKey: ['analytics-dashboard'],
    queryFn: () => apiGet<DashboardData>('/analytics/dashboard'),
  })

  const quick = [
    { to: '/app/resume', icon: Upload, label: 'Analyze Resume', color: 'text-primary' },
    { to: '/app/interviews', icon: Brain, label: 'AI Interview', color: 'text-secondary' },
    { to: '/app/coding', icon: Code2, label: 'Coding Test', color: 'text-success' },
    { to: '/app/roadmap', icon: Map, label: 'Career Roadmap', color: 'text-warning' },
  ]

  const stats = [
    { label: 'Avg ATS Score', value: data?.avgAtsScore ?? 0, accent: true },
    { label: 'Interview Score', value: data?.avgInterviewScore ?? 0, accent: false },
    { label: 'Resumes', value: data?.resumeCount ?? 0, accent: false },
    { label: 'Interviews', value: data?.interviewCount ?? 0, accent: false },
  ]

  return (
    <div>
      <motion.h1 initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="text-3xl font-bold">
        Welcome back, {user?.firstName}
      </motion.h1>
      <p className="mt-2 text-muted">
        Your AI interview command center · Status: {data?.accountStatus ?? 'ACTIVE'}
      </p>

      <div className="mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {isLoading
          ? [1, 2, 3, 4].map((i) => (
              <Card key={i}>
                <Skeleton className="h-4 w-24" />
                <Skeleton className="mt-3 h-8 w-16" />
              </Card>
            ))
          : stats.map((s) => (
              <Card key={s.label}>
                <p className="text-sm text-muted">{s.label}</p>
                <p className={s.accent ? 'text-3xl font-bold gradient-text' : 'text-3xl font-bold'}>
                  {String(s.value)}
                </p>
              </Card>
            ))}
      </div>

      {!isLoading && (data?.unreadNotifications ?? 0) > 0 && (
        <Card className="mt-4 border-primary/30 bg-primary/5">
          <p className="text-sm">
            You have <strong>{data!.unreadNotifications}</strong> unread notification
            {data!.unreadNotifications === 1 ? '' : 's'}. Open the bell icon in the header to review them.
          </p>
        </Card>
      )}

      <h2 className="mt-10 text-xl font-semibold">Quick actions</h2>
      <div className="mt-4 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {quick.map((q) => (
          <Link key={q.to} to={q.to}>
            <Card className="flex items-center gap-4 transition hover:scale-[1.02]">
              <q.icon className={`h-8 w-8 ${q.color}`} />
              <span className="font-medium">{q.label}</span>
            </Card>
          </Link>
        ))}
      </div>

      <h2 className="mt-10 text-xl font-semibold">Recent activity</h2>
      <div className="mt-4">
        {isLoading && (
          <Card>
            <Skeleton className="h-4 w-full" />
            <Skeleton className="mt-2 h-4 w-3/4" />
          </Card>
        )}
        {!isLoading && isError && (
          <EmptyState
            icon={Activity}
            title="Could not load activity"
            description="Check your connection and refresh the page."
          />
        )}
        {!isLoading && !isError && (data?.recentActivity?.length ?? 0) === 0 && (
          <EmptyState
            icon={FileText}
            title="No activity yet"
            description="Upload a resume or generate an interview to see your progress here."
          />
        )}
        {!isLoading && (data?.recentActivity?.length ?? 0) > 0 && (
          <Card>
            <ul className="divide-y divide-border">
              {data!.recentActivity.map((a, i) => (
                <li key={i} className="flex justify-between py-3 text-sm first:pt-0 last:pb-0">
                  <span>{a.action.replace(/_/g, ' ')}</span>
                  <span className="text-muted">{new Date(a.createdAt).toLocaleString()}</span>
                </li>
              ))}
            </ul>
          </Card>
        )}
      </div>
    </div>
  )
}
