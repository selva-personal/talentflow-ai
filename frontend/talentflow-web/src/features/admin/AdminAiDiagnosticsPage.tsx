import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { apiGet, apiPost, getApiError } from '@/lib/api'
import { toast } from '@/lib/toast'
import { Card } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { EmptyState } from '@/components/ui/empty-state'
import { Zap, ArrowLeft } from 'lucide-react'
import { Link } from 'react-router-dom'
import { cn } from '@/lib/utils'
import { useAiStore } from '@/stores/aiStore'

interface ProviderDiag {
  provider: string
  status: string
  quotaAvailable: boolean
  reachable: boolean
  configured: boolean
  model: string
  latencyMs: number | null
  lastError: string | null
  lastDiagnosis: string | null
  lastHttpStatus: number | null
}

interface AiDiagnostics {
  activeProvider: string
  configuredProvider: string
  fallbackActive: boolean
  lastFailureReason: string | null
  lastSuccessfulRequest: string | null
  lastDiagnosis: string | null
  providers: ProviderDiag[]
}

interface AiTestResult {
  provider: string
  response: string
  fallbackActive: boolean
  responseTimeMs: number
  httpStatus: number | null
  diagnosis: string
  googleErrorMessage?: string | null
  errorBody?: string | null
}

function statusColor(status: string) {
  if (status === 'ONLINE') return 'text-success'
  if (status === 'OFFLINE') return 'text-warning'
  return 'text-muted'
}

export function AdminAiDiagnosticsPage() {
  const queryClient = useQueryClient()
  const setStatus = useAiStore((s) => s.setStatus)
  const [testResult, setTestResult] = useState<AiTestResult | null>(null)

  const { data, isLoading, isError, refetch, isFetching } = useQuery({
    queryKey: ['ai-diagnostics'],
    queryFn: () => apiGet<AiDiagnostics>('/ai/diagnostics'),
    refetchInterval: 30_000,
  })

  const probeAll = useMutation({
    mutationFn: () => apiPost('/ai/diagnostics/probe'),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['ai-diagnostics'] })
      queryClient.invalidateQueries({ queryKey: ['admin-ai-status'] })
      toast.success('All providers probed')
    },
    onError: (err) => toast.error('Probe failed', getApiError(err)),
  })

  const testAi = useMutation({
    mutationFn: () =>
      apiPost<AiTestResult>('/ai/test', {
        prompt: 'Tell me a joke about software developers',
      }),
    onSuccess: (result) => {
      setTestResult(result)
      queryClient.invalidateQueries({ queryKey: ['ai-diagnostics'] })
      queryClient.invalidateQueries({ queryKey: ['admin-ai-status'] })
      queryClient.invalidateQueries({ queryKey: ['ai-status'] })

      const online = result.provider !== 'fallback'
      setStatus({
        geminiStatus: online ? 'ONLINE' : 'OFFLINE',
        quotaStatus: result.diagnosis === 'QUOTA_EXHAUSTED' ? 'EXHAUSTED' : online ? 'AVAILABLE' : 'UNAVAILABLE',
        fallbackActive: result.fallbackActive,
        activeProvider: result.provider,
        lastFailureReason: result.googleErrorMessage ?? null,
      })

      if (online) {
        toast.success(`${result.provider} online`, 'Real AI mode active')
      } else {
        toast.warning('Fallback mode', result.googleErrorMessage ?? result.diagnosis)
      }
    },
    onError: (err) => toast.error('AI test failed', getApiError(err)),
  })

  return (
    <div>
      <div className="flex items-center gap-3">
        <Zap className="h-8 w-8 text-primary" />
        <div>
          <h1 className="text-3xl font-bold">AI Diagnostics</h1>
          <p className="text-muted">Multi-provider connectivity, quota, and failover status</p>
        </div>
      </div>

      <div className="mt-4 flex flex-wrap gap-2">
        <Button type="button" variant="secondary" onClick={() => refetch()} loading={isFetching}>
          Refresh
        </Button>
        <Button type="button" variant="secondary" onClick={() => probeAll.mutate()} loading={probeAll.isPending}>
          Probe All Providers
        </Button>
        <Button type="button" onClick={() => testAi.mutate()} loading={testAi.isPending}>
          Test Failover Chain
        </Button>
      </div>

      <Card className="mt-6">
        {isLoading ? (
          <Skeleton className="h-48 w-full" />
        ) : isError ? (
          <EmptyState icon={Zap} title="Diagnostics unavailable" description="Could not load AI diagnostics." />
        ) : (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <DiagItem label="Active Provider" value={data?.activeProvider ?? '—'} />
            <DiagItem label="Configured Provider" value={data?.configuredProvider ?? '—'} />
            <DiagItem label="Fallback Active" value={data?.fallbackActive ? 'Yes' : 'No'} warn={data?.fallbackActive} />
            <DiagItem label="Last Diagnosis" value={data?.lastDiagnosis ?? '—'} />
          </div>
        )}
      </Card>

      <h2 className="mt-8 text-xl font-semibold">Provider Status</h2>
      <div className="mt-4 grid gap-4 md:grid-cols-2">
        {(data?.providers ?? []).map((p) => (
          <Card key={p.provider}>
            <div className="flex items-center justify-between">
              <h3 className="font-semibold capitalize">{p.provider}</h3>
              <span className={cn('text-sm font-medium', statusColor(p.status))}>{p.status}</span>
            </div>
            <div className="mt-3 grid gap-2 text-sm">
              <Row label="Configured" value={p.configured ? 'Yes' : 'No'} />
              <Row label="Reachable" value={p.reachable ? 'Yes' : 'No'} />
              <Row label="Quota Available" value={p.quotaAvailable ? 'Yes' : 'No'} />
              <Row label="Model" value={p.model ?? '—'} />
              <Row label="Latency" value={p.latencyMs != null ? `${p.latencyMs}ms` : '—'} />
              <Row label="Last HTTP" value={p.lastHttpStatus?.toString() ?? '—'} />
              {p.lastError && (
                <p className="mt-1 text-xs text-warning line-clamp-3">{p.lastError}</p>
              )}
            </div>
          </Card>
        ))}
      </div>

      {testResult && (
        <Card className="mt-6">
          <h2 className="text-lg font-semibold">Failover Test Result</h2>
          <div className="mt-4 grid gap-3 sm:grid-cols-2">
            <DiagItem label="Provider Used" value={testResult.provider} />
            <DiagItem label="Response Time" value={`${testResult.responseTimeMs}ms`} />
            <DiagItem label="HTTP Status" value={testResult.httpStatus?.toString() ?? '—'} />
            <DiagItem label="Diagnosis" value={testResult.diagnosis} />
          </div>
          <pre className="mt-4 max-h-48 overflow-auto rounded-xl border border-border bg-bg/50 p-3 text-sm whitespace-pre-wrap">
            {testResult.response}
          </pre>
          {testResult.googleErrorMessage && (
            <pre className="mt-4 max-h-48 overflow-auto rounded-xl border border-warning/30 bg-warning/5 p-3 text-xs whitespace-pre-wrap text-warning">
              {testResult.googleErrorMessage}
            </pre>
          )}
        </Card>
      )}

      <p className="mt-6 text-sm text-muted">
        <Link to="/app/admin" className="inline-flex items-center gap-1 text-primary hover:underline">
          <ArrowLeft className="h-4 w-4" /> Back to Admin Console
        </Link>
      </p>
    </div>
  )
}

function DiagItem({ label, value, warn }: { label: string; value: string; warn?: boolean }) {
  return (
    <div>
      <p className="text-sm text-muted">{label}</p>
      <p className={cn('text-sm font-semibold', warn ? 'text-warning' : 'text-text')}>{value}</p>
    </div>
  )
}

function Row({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex justify-between gap-2">
      <span className="text-muted">{label}</span>
      <span>{value}</span>
    </div>
  )
}
