import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { apiPost } from '@/lib/api'
import { Card } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'

interface Question {
  id: number
  type: string
  text: string
}

export function InterviewPage() {
  const [interviewId, setInterviewId] = useState<number | null>(null)
  const [questions, setQuestions] = useState<Question[]>([])
  const [answers, setAnswers] = useState<Record<number, string>>({})
  const [result, setResult] = useState<Record<string, unknown> | null>(null)
  const [form, setForm] = useState({ roleTarget: 'Software Engineer', experienceLevel: 'Mid', skillLevel: 'Intermediate' })

  const generate = useMutation({
    mutationFn: () => apiPost<{ interviewId: number; questions: Question[] }>('/interviews/generate', form),
    onSuccess: (data) => {
      setInterviewId(data.interviewId)
      setQuestions(data.questions)
      setResult(null)
    },
  })

  const submitAnswer = async (questionId: number) => {
    const text = answers[questionId]
    if (!text) return
    await apiPost(`/interviews/questions/${questionId}/answer`, { answerText: text })
  }

  const complete = useMutation({
    mutationFn: () => apiPost<Record<string, unknown>>(`/interviews/${interviewId}/complete`),
    onSuccess: setResult,
  })

  return (
    <div>
      <h1 className="text-3xl font-bold">AI Interview Generator</h1>
      <Card className="mt-6 grid gap-4 md:grid-cols-3">
        <Input placeholder="Target role" value={form.roleTarget} onChange={(e) => setForm({ ...form, roleTarget: e.target.value })} />
        <Input placeholder="Experience" value={form.experienceLevel} onChange={(e) => setForm({ ...form, experienceLevel: e.target.value })} />
        <Input placeholder="Skill level" value={form.skillLevel} onChange={(e) => setForm({ ...form, skillLevel: e.target.value })} />
      </Card>
      <Button className="mt-4" onClick={() => generate.mutate()} loading={generate.isPending}>Generate Questions</Button>

      <div className="mt-8 space-y-4">
        {questions.map((q) => (
          <Card key={q.id}>
            <span className="text-xs font-semibold text-primary">{q.type}</span>
            <p className="mt-2">{q.text}</p>
            <textarea
              className="mt-3 w-full rounded-xl border border-white/10 bg-surface p-3 text-sm"
              rows={3}
              value={answers[q.id] ?? ''}
              onChange={(e) => setAnswers({ ...answers, [q.id]: e.target.value })}
            />
            <Button variant="secondary" className="mt-2" onClick={() => submitAnswer(q.id)}>Submit Answer</Button>
          </Card>
        ))}
      </div>

      {interviewId && questions.length > 0 && (
        <Button className="mt-6" variant="primary" onClick={() => complete.mutate()} loading={complete.isPending}>
          Complete Interview
        </Button>
      )}

      {result && (
        <Card className="mt-6">
          <p className="text-2xl font-bold">Score: {String(result.overallScore)}</p>
          <p className="mt-2 text-muted">{String(result.feedback)}</p>
        </Card>
      )}
    </div>
  )
}
