import { useEffect } from 'react'
import { useQuery } from '@tanstack/react-query'
import { apiGet, getApiError } from '@/lib/api'
import { toast } from '@/lib/toast'
import { Card } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { EmptyState } from '@/components/ui/empty-state'
import { BarChart3 } from 'lucide-react'
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  RadarChart,
  PolarGrid,
  PolarAngleAxis,
  Radar,
} from 'recharts'

export function AnalyticsPage() {
  const { data, isLoading, isError } = useQuery({
    queryKey: ['analytics-dashboard'],
    queryFn: () =>
      apiGet<{
        avgAtsScore: number
        avgInterviewScore: number
        atsHistory: { date: string; score: number }[]
        skillRadar: { skill: string; value: number }[]
        aiRecommendations: string
        recentActivity: { action: string; createdAt: string }[]
      }>('/analytics/dashboard'),
  })

  useEffect(() => {
    if (isError) toast.error('Failed to load analytics', getApiError(new Error('load failed')))
  }, [isError])

  let recommendations: string[] = []
  try {
    recommendations = JSON.parse(data?.aiRecommendations ?? '[]')
  } catch {
    recommendations = data?.aiRecommendations ? [String(data.aiRecommendations)] : []
  }

  if (isLoading) {
    return (
      <div>
        <h1 className="text-3xl font-bold">Analytics Dashboard</h1>
        <div className="mt-8 grid gap-4 lg:grid-cols-2">
          <Skeleton className="h-64 rounded-2xl" />
          <Skeleton className="h-64 rounded-2xl" />
        </div>
      </div>
    )
  }

  if (isError || !data) {
    return (
      <div>
        <h1 className="text-3xl font-bold">Analytics Dashboard</h1>
        <div className="mt-8">
          <EmptyState
            icon={BarChart3}
            title="Analytics unavailable"
            description="Could not load your analytics. Please try again later."
          />
        </div>
      </div>
    )
  }

  return (
    <div>
      <h1 className="text-3xl font-bold">Analytics Dashboard</h1>
      <div className="mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <Card>
          <p className="text-sm text-muted">Avg ATS</p>
          <p className="text-2xl font-bold gradient-text">{data.avgAtsScore}</p>
        </Card>
        <Card>
          <p className="text-sm text-muted">Interview score</p>
          <p className="text-2xl font-bold">{data.avgInterviewScore}</p>
        </Card>
      </div>
      <div className="mt-8 grid gap-4 lg:grid-cols-2">
        <Card>
          <h3 className="font-semibold">ATS Score History</h3>
          {(data.atsHistory?.length ?? 0) === 0 ? (
            <p className="mt-4 text-sm text-muted">No ATS data yet.</p>
          ) : (
            <div className="mt-4 h-64">
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={data.atsHistory}>
                  <XAxis dataKey="date" tick={{ fill: 'var(--color-muted)', fontSize: 10 }} />
                  <YAxis domain={[0, 100]} tick={{ fill: 'var(--color-muted)' }} />
                  <Tooltip contentStyle={{ background: 'var(--color-surface)', border: '1px solid var(--color-border)' }} />
                  <Line type="monotone" dataKey="score" stroke="#7c3aed" strokeWidth={2} dot={false} />
                </LineChart>
              </ResponsiveContainer>
            </div>
          )}
        </Card>
        <Card>
          <h3 className="font-semibold">Skill Radar</h3>
          {(data.skillRadar?.length ?? 0) === 0 ? (
            <p className="mt-4 text-sm text-muted">Upload a resume to see skill gaps.</p>
          ) : (
            <div className="mt-4 h-64">
              <ResponsiveContainer width="100%" height="100%">
                <RadarChart data={data.skillRadar}>
                  <PolarGrid stroke="var(--color-border)" />
                  <PolarAngleAxis dataKey="skill" tick={{ fill: 'var(--color-muted)', fontSize: 10 }} />
                  <Radar dataKey="value" stroke="#06b6d4" fill="#06b6d4" fillOpacity={0.4} />
                </RadarChart>
              </ResponsiveContainer>
            </div>
          )}
        </Card>
      </div>

      <Card className="mt-6">
        <h3 className="font-semibold">AI Recommendations</h3>
        <ul className="mt-3 list-disc pl-5 text-sm text-muted">
          {recommendations.map((r, i) => (
            <li key={i}>{r}</li>
          ))}
        </ul>
      </Card>

      <Card className="mt-6">
        <h3 className="font-semibold">Recent Activity</h3>
        <ul className="mt-3 space-y-2 text-sm text-muted">
          {(data.recentActivity ?? []).map((a, i) => (
            <li key={i}>{a.action} — {new Date(a.createdAt).toLocaleString()}</li>
          ))}
        </ul>
      </Card>
    </div>
  )
}
