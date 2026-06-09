import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { apiPost, getApiError } from '@/lib/api'
import { toast } from '@/lib/toast'
import { Card } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { EmptyState } from '@/components/ui/empty-state'
import { Skeleton } from '@/components/ui/skeleton'
import { FileText } from 'lucide-react'
import { Input } from '@/components/ui/input'
import { FormField } from '@/components/ui/form-field'
import { Select } from '@/components/ui/select'
import { Textarea } from '@/components/ui/textarea'
import { jsPDF } from 'jspdf'

export function CoverLetterPage() {
  const [form, setForm] = useState({
    jobTitle: 'Software Engineer',
    companyName: 'Acme Corp',
    tone: 'PROFESSIONAL',
    highlights: '5 years full-stack experience',
  })
  const [content, setContent] = useState('')

  const generate = useMutation({
    mutationFn: () => apiPost<{ content: string }>('/cover-letters', form),
    onSuccess: (data) => {
      setContent(data.content)
      toast.success('Cover letter generated')
    },
    onError: (err) => toast.error('Generation failed', getApiError(err)),
  })

  const exportPdf = () => {
    const doc = new jsPDF()
    const lines = doc.splitTextToSize(content, 180)
    doc.text(lines, 15, 20)
    doc.save(`cover-letter-${form.companyName}.pdf`)
    toast.success('PDF exported')
  }

  return (
    <div className="max-w-3xl">
      <h1 className="text-3xl font-bold">Cover Letter Generator</h1>
      <Card className="mt-6 space-y-4">
        <div className="grid gap-4 md:grid-cols-2">
          <FormField label="Job title" htmlFor="jobTitle" required>
            <Input
              id="jobTitle"
              value={form.jobTitle}
              onChange={(e) => setForm({ ...form, jobTitle: e.target.value })}
            />
          </FormField>
          <FormField label="Company" htmlFor="companyName" required>
            <Input
              id="companyName"
              value={form.companyName}
              onChange={(e) => setForm({ ...form, companyName: e.target.value })}
            />
          </FormField>
        </div>
        <FormField label="Tone" htmlFor="tone" required>
          <Select
            id="tone"
            value={form.tone}
            onChange={(e) => setForm({ ...form, tone: e.target.value })}
          >
            <option value="PROFESSIONAL">Professional</option>
            <option value="ENTHUSIASTIC">Enthusiastic</option>
            <option value="FORMAL">Formal</option>
          </Select>
        </FormField>
        <FormField label="Highlights" htmlFor="highlights" hint="Key skills or achievements to emphasize">
          <Textarea
            id="highlights"
            rows={3}
            value={form.highlights}
            onChange={(e) => setForm({ ...form, highlights: e.target.value })}
          />
        </FormField>
        <Button type="button" onClick={() => generate.mutate()} loading={generate.isPending}>
          Generate
        </Button>
      </Card>

      {generate.isPending && (
        <Card className="mt-8">
          <Skeleton className="h-5 w-40" />
          <Skeleton className="mt-4 h-48 w-full" />
        </Card>
      )}

      {!content && !generate.isPending && (
        <div className="mt-8">
          <EmptyState
            icon={FileText}
            title="No cover letter yet"
            description="Fill in the form above and click Generate."
          />
        </div>
      )}

      {content && !generate.isPending && (
        <Card className="mt-8">
          <FormField label="Generated letter">
            <pre className="whitespace-pre-wrap font-sans text-sm">{content}</pre>
          </FormField>
          <Button type="button" className="mt-4" variant="secondary" onClick={exportPdf}>
            Export PDF
          </Button>
        </Card>
      )}
    </div>
  )
}
