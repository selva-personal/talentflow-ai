import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { apiPost, getApiError } from '@/lib/api'
import { toast } from '@/lib/toast'
import { Card } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { FormField } from '@/components/ui/form-field'
import { Skeleton } from '@/components/ui/skeleton'
import { EmptyState } from '@/components/ui/empty-state'
import { Map } from 'lucide-react'

interface RoadmapData {
  skills?: { name: string; priority: string }[]
  learningPath?: { phase: string; topics: string[]; durationWeeks: number }[]
  projects?: { title: string; description: string }[]
  milestones?: { month: number; goal: string }[]
}

export function RoadmapPage() {
  const [roadmap, setRoadmap] = useState<{ roadmapData?: RoadmapData } | null>(null)
  const [form, setForm] = useState({
    currentRole: 'Junior Developer',
    targetRole: 'Senior Software Engineer',
    timelineMonths: 12,
  })

  const generate = useMutation({
    mutationFn: () => apiPost<{ roadmapData: RoadmapData }>('/career-roadmaps', form),
    onSuccess: (data) => {
      setRoadmap(data)
      toast.success('Career roadmap generated')
    },
    onError: (err) => toast.error('Failed to generate roadmap', getApiError(err)),
  })

  const data = roadmap?.roadmapData

  return (
    <div className="max-w-4xl">
      <h1 className="text-3xl font-bold">Career Roadmap</h1>
      <p className="mt-2 text-muted">AI-generated learning path from your current role to your target role.</p>

      <Card className="mt-6">
        <div className="grid gap-4 md:grid-cols-3">
          <FormField label="Current role" htmlFor="currentRole" required>
            <Input
              id="currentRole"
              placeholder="e.g. Junior Developer"
              value={form.currentRole}
              onChange={(e) => setForm({ ...form, currentRole: e.target.value })}
            />
          </FormField>
          <FormField label="Target role" htmlFor="targetRole" required>
            <Input
              id="targetRole"
              placeholder="e.g. Senior Engineer"
              value={form.targetRole}
              onChange={(e) => setForm({ ...form, targetRole: e.target.value })}
            />
          </FormField>
          <FormField label="Timeline (months)" htmlFor="timelineMonths" required>
            <Input
              id="timelineMonths"
              type="number"
              min={1}
              value={form.timelineMonths}
              onChange={(e) => setForm({ ...form, timelineMonths: +e.target.value })}
            />
          </FormField>
        </div>
        <Button
          type="button"
          className="mt-4"
          onClick={() => generate.mutate()}
          loading={generate.isPending}
        >
          Generate Roadmap
        </Button>
      </Card>

      {generate.isPending && <Skeleton className="mt-8 h-64 w-full rounded-2xl" />}

      {!generate.isPending && !data && (
        <div className="mt-8">
          <EmptyState
            icon={Map}
            title="No roadmap yet"
            description="Enter your current and target roles, then generate a personalized career plan."
          />
        </div>
      )}

      {data && !generate.isPending && (
        <div className="mt-8 space-y-6">
          {data.milestones && data.milestones.length > 0 && (
            <Card>
              <h2 className="text-lg font-semibold">Milestones</h2>
              <ul className="mt-4 space-y-3">
                {data.milestones.map((m, i) => (
                  <li key={i} className="flex gap-4 rounded-xl border border-border p-3 text-sm">
                    <span className="font-bold text-primary">Month {m.month}</span>
                    <span>{m.goal}</span>
                  </li>
                ))}
              </ul>
            </Card>
          )}

          {data.skills && data.skills.length > 0 && (
            <Card>
              <h2 className="text-lg font-semibold">Skills to develop</h2>
              <div className="mt-4 flex flex-wrap gap-2">
                {data.skills.map((s, i) => (
                  <span
                    key={i}
                    className="rounded-full border border-border px-3 py-1 text-sm capitalize"
                  >
                    {s.name} · {s.priority}
                  </span>
                ))}
              </div>
            </Card>
          )}

          {data.learningPath && data.learningPath.length > 0 && (
            <Card>
              <h2 className="text-lg font-semibold">Learning path</h2>
              <div className="mt-4 space-y-4">
                {data.learningPath.map((phase, i) => (
                  <div key={i} className="rounded-xl border border-border p-4">
                    <p className="font-medium">{phase.phase}</p>
                    <p className="text-xs text-muted">{phase.durationWeeks} weeks</p>
                    <ul className="mt-2 list-inside list-disc text-sm text-muted">
                      {phase.topics?.map((t) => (
                        <li key={t}>{t}</li>
                      ))}
                    </ul>
                  </div>
                ))}
              </div>
            </Card>
          )}

          {data.projects && data.projects.length > 0 && (
            <Card>
              <h2 className="text-lg font-semibold">Recommended projects</h2>
              <ul className="mt-4 space-y-3">
                {data.projects.map((p, i) => (
                  <li key={i} className="rounded-xl border border-border p-3 text-sm">
                    <p className="font-medium">{p.title}</p>
                    <p className="mt-1 text-muted">{p.description}</p>
                  </li>
                ))}
              </ul>
            </Card>
          )}
        </div>
      )}
    </div>
  )
}
