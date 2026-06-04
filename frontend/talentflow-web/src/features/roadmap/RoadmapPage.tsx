import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { apiPost } from '@/lib/api'
import { Card } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'

export function RoadmapPage() {
  const [roadmap, setRoadmap] = useState<Record<string, unknown> | null>(null)
  const [form, setForm] = useState({
    currentRole: 'Junior Developer',
    targetRole: 'Senior Software Engineer',
    timelineMonths: 12,
  })

  const generate = useMutation({
    mutationFn: () => apiPost<Record<string, unknown>>('/career-roadmaps', form),
    onSuccess: setRoadmap,
  })

  return (
    <div>
      <h1 className="text-3xl font-bold">Career Roadmap</h1>
      <Card className="mt-6 grid gap-4 md:grid-cols-3">
        <Input value={form.currentRole} onChange={(e) => setForm({ ...form, currentRole: e.target.value })} placeholder="Current role" />
        <Input value={form.targetRole} onChange={(e) => setForm({ ...form, targetRole: e.target.value })} placeholder="Target role" />
        <Input type="number" value={form.timelineMonths} onChange={(e) => setForm({ ...form, timelineMonths: +e.target.value })} placeholder="Months" />
      </Card>
      <Button className="mt-4" onClick={() => generate.mutate()} loading={generate.isPending}>Generate Roadmap</Button>

      {roadmap?.roadmapData != null && (
        <Card className="mt-8">
          <pre className="overflow-auto text-sm text-muted">
            {JSON.stringify(roadmap.roadmapData as object, null, 2)}
          </pre>
        </Card>
      )}
    </div>
  )
}
