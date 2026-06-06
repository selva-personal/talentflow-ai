import { useState, useRef } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { apiGet, apiUpload, getApiError, type SpringPage } from '@/lib/api'
import { toast } from '@/lib/toast'
import { Card } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { FormField } from '@/components/ui/form-field'
import { Skeleton } from '@/components/ui/skeleton'
import { EmptyState } from '@/components/ui/empty-state'
import { Upload, FileText } from 'lucide-react'
import { cn } from '@/lib/utils'

interface Analysis {
  atsScore: number
  strengths: string[]
  weaknesses: string[]
  missingSkills: string[]
  recommendations: string[]
}

interface UploadResponse {
  resumeId: number
  fileName: string
  analysis: Analysis
}

export function ResumePage() {
  const [analysis, setAnalysis] = useState<Analysis | null>(null)
  const [selectedId, setSelectedId] = useState<number | null>(null)
  const fileRef = useRef<HTMLInputElement>(null)
  const queryClient = useQueryClient()

  const { data: resumes, isLoading } = useQuery({
    queryKey: ['resumes'],
    queryFn: () => apiGet<SpringPage<{ id: number; fileName: string; createdAt: string }>>('/resumes'),
  })

  const loadAnalysis = useMutation({
    mutationFn: (resumeId: number) => apiGet<Analysis>(`/resumes/${resumeId}/analysis`),
    onSuccess: (data, resumeId) => {
      setAnalysis(data)
      setSelectedId(resumeId)
    },
    onError: (err) => toast.error('Could not load analysis', getApiError(err)),
  })

  const upload = useMutation({
    mutationFn: (file: File) => apiUpload<UploadResponse>('/resumes/upload', file),
    onSuccess: (data) => {
      setAnalysis(data.analysis)
      setSelectedId(data.resumeId)
      queryClient.invalidateQueries({ queryKey: ['resumes'] })
      queryClient.invalidateQueries({ queryKey: ['analytics-dashboard'] })
      toast.success('Resume uploaded successfully', `ATS score: ${data.analysis.atsScore}`)
    },
    onError: (err) => toast.error('Upload failed', getApiError(err)),
  })

  const handleFile = (file: File | undefined) => {
    if (!file) return
    if (file.type !== 'application/pdf') {
      toast.warning('Invalid file', 'Please upload a PDF resume.')
      return
    }
    upload.mutate(file)
  }

  return (
    <div className="max-w-4xl">
      <h1 className="text-3xl font-bold">Resume Analyzer</h1>
      <p className="mt-2 text-muted">Upload a PDF for ATS scoring and AI recommendations</p>

      <Card className="mt-8">
        <FormField label="Resume file" required hint="PDF only, max 10MB">
          <div
            className="flex cursor-pointer flex-col items-center gap-4 rounded-xl border border-dashed border-primary/40 p-12 transition hover:bg-primary/5"
            onClick={() => fileRef.current?.click()}
            onKeyDown={(e) => e.key === 'Enter' && fileRef.current?.click()}
            role="button"
            tabIndex={0}
          >
            <Upload className="h-10 w-10 text-primary" />
            <input
              ref={fileRef}
              type="file"
              accept="application/pdf"
              className="hidden"
              onChange={(e) => handleFile(e.target.files?.[0])}
            />
            <Button type="button" loading={upload.isPending} variant="primary">
              Upload & Analyze
            </Button>
          </div>
        </FormField>
      </Card>

      {(upload.isPending || loadAnalysis.isPending) && (
        <div className="mt-8 grid gap-4 lg:grid-cols-2">
          {[1, 2, 3, 4].map((i) => (
            <Card key={i}>
              <Skeleton className="h-5 w-32" />
              <Skeleton className="mt-3 h-16 w-full" />
            </Card>
          ))}
        </div>
      )}

      {analysis && !upload.isPending && !loadAnalysis.isPending && (
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

      <h2 className="mt-10 text-xl font-semibold">Recent uploads</h2>
      <div className="mt-4">
        {isLoading && <Skeleton className="h-24 w-full rounded-2xl" />}
        {!isLoading && (resumes?.content?.length ?? 0) === 0 && (
          <EmptyState
            icon={FileText}
            title="No resumes yet"
            description="Upload your first PDF to get an ATS analysis."
          />
        )}
        {!isLoading && (resumes?.content?.length ?? 0) > 0 && (
          <Card>
            <ul className="divide-y divide-border">
              {resumes!.content.map((r) => (
                <li key={r.id}>
                  <button
                    type="button"
                    onClick={() => loadAnalysis.mutate(r.id)}
                    className={cn(
                      'flex w-full justify-between py-3 text-left text-sm transition first:pt-0 last:pb-0 hover:text-primary',
                      selectedId === r.id && 'text-primary'
                    )}
                  >
                    <span>{r.fileName}</span>
                    <span className="text-muted">{new Date(r.createdAt).toLocaleDateString()}</span>
                  </button>
                </li>
              ))}
            </ul>
          </Card>
        )}
      </div>
    </div>
  )
}
