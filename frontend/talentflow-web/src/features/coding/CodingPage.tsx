import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { apiPost } from '@/lib/api'
import { Card } from '@/components/ui/card'
import { Button } from '@/components/ui/button'

const LANGUAGES = ['JavaScript', 'TypeScript', 'Java', 'Python']

export function CodingPage() {
  const [testId, setTestId] = useState<number | null>(null)
  const [problem, setProblem] = useState('')
  const [code, setCode] = useState('')
  const [review, setReview] = useState<Record<string, unknown> | null>(null)
  const [language, setLanguage] = useState('JavaScript')

  const generate = useMutation({
    mutationFn: () => apiPost<{ testId: number; problemStatement: string; starterCode: string }>('/coding/tests', {
      language,
      difficulty: 'MEDIUM',
    }),
    onSuccess: (data) => {
      setTestId(data.testId)
      setProblem(data.problemStatement)
      setCode(data.starterCode)
    },
  })

  const submit = useMutation({
    mutationFn: () => apiPost<Record<string, unknown>>(`/coding/tests/${testId}/submit`, { code }),
    onSuccess: setReview,
  })

  return (
    <div>
      <h1 className="text-3xl font-bold">Coding Assessment</h1>
      <div className="mt-4 flex flex-wrap gap-2">
        {LANGUAGES.map((l) => (
          <button
            key={l}
            type="button"
            onClick={() => setLanguage(l)}
            className={`rounded-lg px-4 py-2 text-sm ${language === l ? 'bg-primary text-white' : 'bg-surface text-muted'}`}
          >
            {l}
          </button>
        ))}
        <Button onClick={() => generate.mutate()} loading={generate.isPending}>New Challenge</Button>
      </div>

      {problem && (
        <div className="mt-6 grid gap-4 lg:grid-cols-2">
          <Card>
            <h3 className="font-semibold">Problem</h3>
            <pre className="mt-3 whitespace-pre-wrap text-sm text-muted">{problem}</pre>
          </Card>
          <Card className="p-0 overflow-hidden">
            <textarea
              className="h-80 w-full resize-none bg-[#0d1117] p-4 font-mono text-sm text-green-400 outline-none"
              value={code}
              onChange={(e) => setCode(e.target.value)}
              spellCheck={false}
            />
            <div className="border-t border-white/5 p-4">
              <Button onClick={() => submit.mutate()} loading={submit.isPending} disabled={!testId}>
                Submit for AI Review
              </Button>
            </div>
          </Card>
        </div>
      )}

      {review && (
        <Card className="mt-6">
          <p className="font-semibold">Score: {String(review.aiScore)} — {review.passed ? 'Passed' : 'Needs work'}</p>
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
