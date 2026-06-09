import { useState, useRef, useEffect } from 'react'
import { useMutation } from '@tanstack/react-query'
import { apiPost, getApiError } from '@/lib/api'
import { toast } from '@/lib/toast'
import { Card } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { FormField } from '@/components/ui/form-field'
import { Send } from 'lucide-react'

interface Message {
  role: string
  content: string
}

export function MockInterviewPage() {
  const [sessionId, setSessionId] = useState<number | null>(null)
  const [messages, setMessages] = useState<Message[]>([])
  const [input, setInput] = useState('')
  const [title, setTitle] = useState('Senior Engineer Mock')
  const [roleTarget, setRoleTarget] = useState('Software Engineer')
  const [progress, setProgress] = useState(0)
  const [typing, setTyping] = useState(false)
  const bottomRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages, typing])

  const createSession = useMutation({
    mutationFn: () =>
      apiPost<{ id: number; messages: Message[]; progressPercent: number }>('/mock-interviews', {
        title,
        roleTarget,
      }),
    onSuccess: (data) => {
      setSessionId(data.id)
      setMessages(data.messages)
      setProgress(data.progressPercent)
      toast.success('Mock interview started')
    },
    onError: (err) => toast.error('Failed to start session', getApiError(err)),
  })

  const send = useMutation({
    mutationFn: (msg: string) =>
      apiPost<{ reply: string; messages: Message[]; progressPercent: number }>(
        `/mock-interviews/${sessionId}/messages`,
        { message: msg }
      ),
    onMutate: () => setTyping(true),
    onSuccess: (data) => {
      setMessages(data.messages)
      setProgress(data.progressPercent)
      setTyping(false)
    },
    onError: (err) => {
      setTyping(false)
      toast.error('Message failed', getApiError(err))
    },
  })

  const handleSend = () => {
    if (!input.trim() || !sessionId) return
    const msg = input.trim()
    setMessages((m) => [...m, { role: 'user', content: msg }])
    setInput('')
    send.mutate(msg)
  }

  return (
    <div className="flex h-[calc(100vh-8rem)] flex-col">
      <h1 className="text-3xl font-bold">Mock Interview</h1>
      {!sessionId ? (
        <Card className="mt-6 max-w-lg">
          <div className="space-y-4">
            <FormField label="Session title" htmlFor="sessionTitle" required>
              <Input id="sessionTitle" value={title} onChange={(e) => setTitle(e.target.value)} />
            </FormField>
            <FormField label="Target role" htmlFor="roleTarget" required>
              <Input id="roleTarget" value={roleTarget} onChange={(e) => setRoleTarget(e.target.value)} />
            </FormField>
          </div>
          <Button
            type="button"
            className="mt-4"
            onClick={() => createSession.mutate()}
            loading={createSession.isPending}
          >
            Start Session
          </Button>
        </Card>
      ) : (
        <>
          <div className="mb-2 h-2 overflow-hidden rounded-full bg-surface">
            <div className="h-full bg-primary transition-all" style={{ width: `${progress}%` }} />
          </div>
          <Card className="flex flex-1 flex-col overflow-hidden p-0">
            <div className="flex-1 space-y-4 overflow-y-auto p-4">
              {messages.length === 0 && !typing && (
                <p className="text-sm text-muted">Waiting for the interviewer to begin…</p>
              )}
              {messages.map((m, i) => (
                <div
                  key={i}
                  className={`max-w-[85%] rounded-2xl px-4 py-3 text-sm ${
                    m.role === 'user' ? 'ml-auto bg-primary/30' : 'bg-surface neu-inset'
                  }`}
                >
                  {m.content}
                </div>
              ))}
              {typing && (
                <div className="flex w-16 gap-1 rounded-2xl bg-surface px-4 py-3">
                  <span className="h-2 w-2 animate-bounce rounded-full bg-muted" />
                  <span className="h-2 w-2 animate-bounce rounded-full bg-muted [animation-delay:0.1s]" />
                  <span className="h-2 w-2 animate-bounce rounded-full bg-muted [animation-delay:0.2s]" />
                </div>
              )}
              <div ref={bottomRef} />
            </div>
            <div className="flex gap-2 border-t border-border p-4">
              <FormField label="Your message" htmlFor="chatInput" className="flex-1">
                <Input
                  id="chatInput"
                  value={input}
                  onChange={(e) => setInput(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && !e.shiftKey && handleSend()}
                  placeholder="Type your answer..."
                />
              </FormField>
              <Button type="button" onClick={handleSend} disabled={send.isPending} className="self-end">
                <Send className="h-4 w-4" />
              </Button>
            </div>
          </Card>
        </>
      )}
    </div>
  )
}
