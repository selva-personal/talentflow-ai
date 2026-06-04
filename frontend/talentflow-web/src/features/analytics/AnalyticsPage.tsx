import { useQuery } from '@tanstack/react-query'
import { apiGet } from '@/lib/api'
import { Card } from '@/components/ui/card'
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
  const { data, isLoading } = useQuery({
    queryKey: ['analytics-dashboard'],
    queryFn: () => apiGet<{
      avgAtsScore: number
      avgInterviewScore: number
      atsHistory: { date: string; score: number }[]
      skillRadar: { skill: string; value: number }[]
      aiRecommendations: string
      recentActivity: { action: string; createdAt: string }[]
    }>('/analytics/dashboard'),
  })

  if (isLoading) return <div className="text-muted">Loading analytics...</div>

  let recommendations: string[] = []
  try {
    recommendations = JSON.parse(data?.aiRecommendations ?? '[]')
  } catch {
    recommendations = [String(data?.aiRecommendations ?? '')]
  }

  return (
    <div>
      <h1 className="text-3xl font-bold">Analytics Dashboard</h1>
      <div className="mt-8 grid gap-4 lg:grid-cols-2">
        <Card>
          <h3 className="font-semibold">ATS Score History</h3>
          <div className="mt-4 h-64">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={data?.atsHistory ?? []}>
                <XAxis dataKey="date" tick={{ fill: '#9ca3af', fontSize: 10 }} />
                <YAxis domain={[0, 100]} tick={{ fill: '#9ca3af' }} />
                <Tooltip contentStyle={{ background: '#111827', border: '1px solid #374151' }} />
                <Line type="monotone" dataKey="score" stroke="#7c3aed" strokeWidth={2} dot={false} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </Card>
        <Card>
          <h3 className="font-semibold">Skill Radar</h3>
          <div className="mt-4 h-64">
            <ResponsiveContainer width="100%" height="100%">
              <RadarChart data={data?.skillRadar ?? []}>
                <PolarGrid stroke="#374151" />
                <PolarAngleAxis dataKey="skill" tick={{ fill: '#9ca3af', fontSize: 10 }} />
                <Radar dataKey="value" stroke="#06b6d4" fill="#06b6d4" fillOpacity={0.4} />
              </RadarChart>
            </ResponsiveContainer>
          </div>
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
          {(data?.recentActivity ?? []).map((a, i) => (
            <li key={i}>{a.action} — {new Date(a.createdAt).toLocaleString()}</li>
          ))}
        </ul>
      </Card>
    </div>
  )
}
