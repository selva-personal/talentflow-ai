import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { apiPost, getApiError } from '@/lib/api'
import { toast } from '@/lib/toast'
import { Card } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { Select } from '@/components/ui/select'
import { FormField } from '@/components/ui/form-field'
import { Skeleton } from '@/components/ui/skeleton'
import { EmptyState } from '@/components/ui/empty-state'
import { Brain, CheckCircle2 } from 'lucide-react'

interface Question {
  id: number
  type: string
  text: string
}

interface GenerateResponse {
  interviewId: number
  title: string
  questions: Question[]
}

const INTERVIEW_TYPES = ['MIXED', 'TECHNICAL', 'BEHAVIORAL', 'HR', 'SYSTEM_DESIGN'] as const

export function InterviewPage() {
  const [interviewId, setInterviewId] = useState<number | null>(null)
  const [interviewTitle, setInterviewTitle] = useState('')
  const [questions, setQuestions] = useState<Question[]>([])
  const [answers, setAnswers] = useState<Record<number, string>>({})
  const [submitted, setSubmitted] = useState<Record<number, boolean>>({})
  const [result, setResult] = useState<Record<string, unknown> | null>(null)
  const [form, setForm] = useState({
    roleTarget: 'Software Engineer',
    experienceLevel: 'Mid-Level',
    skillLevel: 'Intermediate',
    interviewType: 'MIXED' as (typeof INTERVIEW_TYPES)[number],
  })

  const generate = useMutation({
    mutationFn: () => apiPost<GenerateResponse>('/interviews/generate', form),
    onMutate: () => {
      setResult(null)
      setQuestions([])
      setInterviewId(null)
      setSubmitted({})
    },
    onSuccess: (data) => {
      const qs = Array.isArray(data.questions) ? data.questions : []
      if (qs.length === 0) {
        toast.warning('No questions returned', 'Try again or adjust your inputs.')
        return
      }
      setInterviewId(data.interviewId)
      setInterviewTitle(data.title ?? `${form.roleTarget} Interview`)
      setQuestions(qs)
      toast.success('Interview questions generated', `${qs.length} questions ready`)
    },
    onError: (err) => {
      toast.error('Failed to generate questions', getApiError(err))
    },
  })

  const submitAnswer = useMutation({
    mutationFn: ({ questionId, answerText }: { questionId: number; answerText: string }) =>
      apiPost<{ score: number; feedback: string }>(
        `/interviews/questions/${questionId}/answer`,
        { answerText }
      ),
    onSuccess: (data, vars) => {
      setSubmitted((s) => ({ ...s, [vars.questionId]: true }))
      toast.success('Answer submitted', `Score: ${data.score}/100`)
    },
    onError: (err) => {
      toast.error('Failed to submit answer', getApiError(err))
    },
  })

  const complete = useMutation({
    mutationFn: () => {
      if (!interviewId) throw new Error('No active interview')
      return apiPost<Record<string, unknown>>(`/interviews/${interviewId}/complete`)
    },
    onSuccess: (data) => {
      setResult(data)
      toast.success('Interview completed', `Overall score: ${data.overallScore ?? '—'}`)
    },
    onError: (err) => {
      toast.error('Failed to complete interview', getApiError(err))
    },
  })

  const handleSubmitAnswer = (questionId: number) => {
    const text = answers[questionId]?.trim()
    if (!text) {
      toast.warning('Answer required', 'Please write your answer before submitting.')
      return
    }
    submitAnswer.mutate({ questionId, answerText: text })
  }

  return (
    <div className="max-w-4xl">
      <h1 className="text-3xl font-bold">AI Interview Generator</h1>
      <p className="mt-2 text-muted">
        Generate tailored interview questions and get AI feedback on your answers.
      </p>

      <Card className="mt-6">
        <div className="grid gap-4 md:grid-cols-2">
          <FormField label="Role" htmlFor="roleTarget" required>
            <Input
              id="roleTarget"
              placeholder="e.g. Frontend Developer"
              value={form.roleTarget}
              onChange={(e) => setForm({ ...form, roleTarget: e.target.value })}
            />
          </FormField>
          <FormField label="Experience" htmlFor="experienceLevel" required>
            <Input
              id="experienceLevel"
              placeholder="e.g. 1 Year, Mid-Level, Senior"
              value={form.experienceLevel}
              onChange={(e) => setForm({ ...form, experienceLevel: e.target.value })}
            />
          </FormField>
          <FormField label="Skill level" htmlFor="skillLevel" required>
            <Input
              id="skillLevel"
              placeholder="e.g. Beginner, Intermediate, Advanced"
              value={form.skillLevel}
              onChange={(e) => setForm({ ...form, skillLevel: e.target.value })}
            />
          </FormField>
          <FormField label="Interview type" htmlFor="interviewType" required>
            <Select
              id="interviewType"
              value={form.interviewType}
              onChange={(e) =>
                setForm({
                  ...form,
                  interviewType: e.target.value as (typeof INTERVIEW_TYPES)[number],
                })
              }
            >
              {INTERVIEW_TYPES.map((t) => (
                <option key={t} value={t}>
                  {t.replace('_', ' ')}
                </option>
              ))}
            </Select>
          </FormField>
        </div>
        <Button
          className="mt-6"
          type="button"
          onClick={() => generate.mutate()}
          loading={generate.isPending}
          disabled={!form.roleTarget.trim() || generate.isPending}
        >
          {generate.isPending ? 'Generating…' : 'Generate Questions'}
        </Button>
      </Card>

      {generate.isPending && (
        <div className="mt-8 space-y-4">
          {[1, 2, 3].map((i) => (
            <Card key={i}>
              <Skeleton className="h-4 w-24" />
              <Skeleton className="mt-3 h-5 w-full" />
              <Skeleton className="mt-4 h-20 w-full" />
            </Card>
          ))}
        </div>
      )}

      {!generate.isPending && questions.length === 0 && !result && (
        <div className="mt-8">
          <EmptyState
            icon={Brain}
            title="No questions yet"
            description="Fill in your role and experience, then click Generate Questions to start your AI interview prep."
          />
        </div>
      )}

      {questions.length > 0 && (
        <>
          <div className="mt-8 flex items-center justify-between">
            <div>
              <h2 className="text-xl font-semibold">{interviewTitle}</h2>
              <p className="text-sm text-muted">{questions.length} questions</p>
            </div>
          </div>
          <div className="mt-4 space-y-4">
            {questions.map((q, idx) => (
              <Card key={q.id}>
                <div className="flex items-center gap-2">
                  <span className="text-xs font-semibold uppercase text-primary">{q.type}</span>
                  <span className="text-xs text-muted">Q{idx + 1}</span>
                  {submitted[q.id] && (
                    <CheckCircle2 className="h-4 w-4 text-success" aria-label="Submitted" />
                  )}
                </div>
                <p className="mt-2 text-text">{q.text}</p>
                <FormField label="Your answer" htmlFor={`answer-${q.id}`} required className="mt-4">
                  <Textarea
                    id={`answer-${q.id}`}
                    rows={4}
                    placeholder="Write your answer here…"
                    value={answers[q.id] ?? ''}
                    onChange={(e) => setAnswers({ ...answers, [q.id]: e.target.value })}
                  />
                </FormField>
                <Button
                  type="button"
                  variant="secondary"
                  className="mt-3"
                  loading={submitAnswer.isPending && submitAnswer.variables?.questionId === q.id}
                  onClick={() => handleSubmitAnswer(q.id)}
                >
                  Submit Answer
                </Button>
              </Card>
            ))}
          </div>

          <Button
            type="button"
            className="mt-6"
            variant="primary"
            onClick={() => complete.mutate()}
            loading={complete.isPending}
          >
            Complete Interview
          </Button>
        </>
      )}

      {result && (
        <Card className="mt-6 border-success/30">
          <p className="text-sm text-muted">Overall performance</p>
          <p className="text-3xl font-bold gradient-text">{String(result.overallScore ?? '—')}</p>
          <p className="mt-3 text-sm text-text">{String(result.feedback ?? '')}</p>
        </Card>
      )}
    </div>
  )
}
