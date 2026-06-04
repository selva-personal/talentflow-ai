import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { apiPost } from '@/lib/api'
import { Card } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
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
    onSuccess: (data) => setContent(data.content),
  })

  const exportPdf = () => {
    const doc = new jsPDF()
    const lines = doc.splitTextToSize(content, 180)
    doc.text(lines, 15, 20)
    doc.save(`cover-letter-${form.companyName}.pdf`)
  }

  return (
    <div>
      <h1 className="text-3xl font-bold">Cover Letter Generator</h1>
      <Card className="mt-6 grid gap-4 md:grid-cols-2">
        <Input value={form.jobTitle} onChange={(e) => setForm({ ...form, jobTitle: e.target.value })} placeholder="Job title" />
        <Input value={form.companyName} onChange={(e) => setForm({ ...form, companyName: e.target.value })} placeholder="Company" />
        <Input className="md:col-span-2" value={form.highlights} onChange={(e) => setForm({ ...form, highlights: e.target.value })} placeholder="Highlights" />
      </Card>
      <Button className="mt-4" onClick={() => generate.mutate()} loading={generate.isPending}>Generate</Button>

      {content && (
        <Card className="mt-8">
          <pre className="whitespace-pre-wrap font-sans text-sm">{content}</pre>
          <Button className="mt-4" variant="secondary" onClick={exportPdf}>Export PDF</Button>
        </Card>
      )}
    </div>
  )
}
