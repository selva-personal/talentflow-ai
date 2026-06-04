import { useQuery } from '@tanstack/react-query'
import { apiGet } from '@/lib/api'
import { Card } from '@/components/ui/card'
import { Link } from 'react-router-dom'
import { motion } from 'framer-motion'
import { Brain, Code2, Map, Upload } from 'lucide-react'
import { useAuthStore } from '@/stores/authStore'

export function DashboardPage() {
  const user = useAuthStore((s) => s.user)
  const { data } = useQuery({
    queryKey: ['analytics-dashboard'],
    queryFn: () => apiGet<Record<string, unknown>>('/analytics/dashboard'),
  })

  const quick = [
    { to: '/app/resume', icon: Upload, label: 'Analyze Resume', color: 'text-primary' },
    { to: '/app/mock', icon: Brain, label: 'Mock Interview', color: 'text-secondary' },
    { to: '/app/coding', icon: Code2, label: 'Coding Test', color: 'text-success' },
    { to: '/app/roadmap', icon: Map, label: 'Career Roadmap', color: 'text-warning' },
  ]

  return (
    <div>
      <motion.h1 initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="text-3xl font-bold">
        Welcome back, {user?.firstName}
      </motion.h1>
      <p className="mt-2 text-muted">Your AI interview command center</p>

      <div className="mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <Card><p className="text-sm text-muted">Avg ATS Score</p><p className="text-3xl font-bold gradient-text">{String(data?.avgAtsScore ?? 0)}</p></Card>
        <Card><p className="text-sm text-muted">Interview Score</p><p className="text-3xl font-bold">{String(data?.avgInterviewScore ?? 0)}</p></Card>
        <Card><p className="text-sm text-muted">Notifications</p><p className="text-3xl font-bold">{String(data?.unreadNotifications ?? 0)}</p></Card>
        <Card><p className="text-sm text-muted">Status</p><p className="text-lg font-semibold text-success">Active</p></Card>
      </div>

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
    </div>
  )
}
