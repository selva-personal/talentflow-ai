import { useState } from 'react'
import { useMutation, useQuery } from '@tanstack/react-query'
import { apiGet, apiUpload } from '@/lib/api'
import { Card } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Upload } from 'lucide-react'

interface Analysis {
  atsScore: number
  strengths: string[]
  weaknesses: string[]
  missingSkills: string[]
  recommendations: string[]
}

export function ResumePage() {
  const [analysis, setAnalysis] = useState<Analysis | null>(null)
  const { data: resumes } = useQuery({
    queryKey: ['resumes'],
    queryFn: () => apiGet<{ content: Array<{ id: number; fileName: string }> }>('/resumes').catch(() => ({ content: [] })),
  })

  const upload = useMutation({
    mutationFn: (file: File) =>
      apiUpload<{ analysis: Analysis }>('/resumes/upload', file),
    onSuccess: (data) => setAnalysis(data.analysis),
  })

  return (
    <div>
      <h1 className="text-3xl font-bold">Resume Analyzer</h1>
      <p className="mt-2 text-muted">Upload a PDF for ATS scoring and AI recommendations</p>

      <Card className="mt-8">
        <label className="flex cursor-pointer flex-col items-center gap-4 rounded-xl border border-dashed border-primary/40 p-12 transition hover:bg-primary/5">
          <Upload className="h-10 w-10 text-primary" />
          <span className="text-sm text-muted">PDF only, max 10MB</span>
          <input
            type="file"
            accept="application/pdf"
            className="hidden"
            onChange={(e) => {
              const f = e.target.files?.[0]
              if (f) upload.mutate(f)
            }}
          />
          <Button loading={upload.isPending} variant="primary">Upload & Analyze</Button>
        </label>
      </Card>

      {analysis && (
        <div className="mt-8 grid gap-4 lg:grid-cols-2">
          <Card>
            <p className="text-sm text-muted">ATS Score</p>
            <p className="text-5xl font-bold gradient-text">{analysis.atsScore}</p>
          </Card>
          {(['strengths', 'weaknesses', 'missingSkills', 'recommendations'] as const).map((key) => (
            <Card key={key}>
              <h3 className="font-semibold capitalize">{key.replace(/([A-Z])/g, ' $1')}</h3>
              <ul className="mt-3 list-inside list-disc space-y-1 text-sm text-muted">
                {(analysis[key] as string[]).map((item) => (
                  <li key={item}>{item}</li>
                ))}
              </ul>
            </Card>
          ))}
        </div>
      )}

      {resumes && 'content' in (resumes as object) && (
        <Card className="mt-8">
          <h3 className="font-semibold">Recent uploads</h3>
          <p className="text-sm text-muted">View history in Analytics</p>
        </Card>
      )}
    </div>
  )
}
