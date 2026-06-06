import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { apiPost, getApiError } from '@/lib/api'
import { toast } from '@/lib/toast'
import { Card } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { FormField } from '@/components/ui/form-field'
import { Label } from '@/components/ui/label'
import { Skeleton } from '@/components/ui/skeleton'
import { EmptyState } from '@/components/ui/empty-state'
import { Code2 } from 'lucide-react'

const LANGUAGES = ['JavaScript', 'TypeScript', 'Java', 'Python'] as const

export function CodingPage() {
  const [testId, setTestId] = useState<number | null>(null)
  const [problem, setProblem] = useState('')
  const [code, setCode] = useState('')
  const [review, setReview] = useState<Record<string, unknown> | null>(null)
  const [language, setLanguage] = useState<(typeof LANGUAGES)[number]>('JavaScript')

  const generate = useMutation({
    mutationFn: () =>
      apiPost<{ testId: number; problemStatement: string; starterCode: string }>('/coding/tests', {
        language,
        difficulty: 'MEDIUM',
      }),
    onSuccess: (data) => {
      setTestId(data.testId)
      setProblem(data.problemStatement)
      setCode(data.starterCode ?? '')
      setReview(null)
      toast.success('Coding challenge generated')
    },
    onError: (err) => toast.error('Failed to generate challenge', getApiError(err)),
  })

  const submit = useMutation({
    mutationFn: () => apiPost<Record<string, unknown>>(`/coding/tests/${testId}/submit`, { code }),
    onSuccess: (data) => {
      setReview(data)
      toast.success('Code reviewed', `Score: ${data.aiScore ?? '—'}`)
    },
    onError: (err) => toast.error('Submission failed', getApiError(err)),
  })

  return (
    <div className="max-w-5xl">
      <h1 className="text-3xl font-bold">Coding Assessment</h1>
      <Card className="mt-6">
        <Label required>Language</Label>
        <div className="mt-2 flex flex-wrap gap-2">
          {LANGUAGES.map((l) => (
            <button
              key={l}
              type="button"
              onClick={() => setLanguage(l)}
              className={`rounded-lg px-4 py-2 text-sm transition ${
                language === l ? 'bg-primary text-white' : 'bg-surface text-muted hover:text-text'
              }`}
            >
              {l}
            </button>
          ))}
        </div>
        <Button
          type="button"
          className="mt-4"
          onClick={() => generate.mutate()}
          loading={generate.isPending}
        >
          New Challenge
        </Button>
      </Card>

      {generate.isPending && (
        <div className="mt-6 grid gap-4 lg:grid-cols-2">
          <Skeleton className="h-48 rounded-2xl" />
          <Skeleton className="h-48 rounded-2xl" />
        </div>
      )}

      {!generate.isPending && !problem && (
        <div className="mt-8">
          <EmptyState
            icon={Code2}
            title="No challenge yet"
            description="Select a language and click New Challenge to start."
          />
        </div>
      )}

      {problem && !generate.isPending && (
        <div className="mt-6 grid gap-4 lg:grid-cols-2">
          <Card>
            <h3 className="font-semibold">Problem</h3>
            <pre className="mt-3 whitespace-pre-wrap text-sm text-muted">{problem}</pre>
          </Card>
          <Card className="overflow-hidden p-0">
            <FormField label="Your solution" htmlFor="codeEditor" required className="p-4 pb-0">
              <textarea
                id="codeEditor"
                className="h-72 w-full resize-none rounded-xl bg-[#0d1117] p-4 font-mono text-sm text-green-400 outline-none"
                value={code}
                onChange={(e) => setCode(e.target.value)}
                spellCheck={false}
              />
            </FormField>
            <div className="border-t border-border p-4">
              <Button
                type="button"
                onClick={() => submit.mutate()}
                loading={submit.isPending}
                disabled={!testId || !code.trim()}
              >
                Submit for AI Review
              </Button>
            </div>
          </Card>
        </div>
      )}

      {review && (
        <Card className="mt-6">
          <p className="font-semibold">
            Score: {String(review.aiScore)} — {review.passed ? 'Passed' : 'Needs work'}
          </p>
          <p className="mt-2 text-sm text-muted">{String(review.complexityAnalysis)}</p>
          <ul className="mt-2 list-disc pl-5 text-sm text-muted">
            {(review.suggestions as string[] | undefined)?.map((s) => (
              <li key={s}>{s}</li>
            ))}
          </ul>
        </Card>
      )}
    </div>
  )
}
