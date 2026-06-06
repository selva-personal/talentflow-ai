import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Link } from 'react-router-dom'
import { apiPost, getApiError } from '@/lib/api'
import { toast } from '@/lib/toast'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { FormField } from '@/components/ui/form-field'
import { Card } from '@/components/ui/card'
import { ThemeToggle } from '@/components/ui/theme-toggle'

const schema = z.object({ email: z.string().email('Enter a valid email') })

export function ForgotPasswordPage() {
  const [devResetUrl, setDevResetUrl] = useState<string | null>(null)
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm({ resolver: zodResolver(schema) })

  const onSubmit = async (data: z.infer<typeof schema>) => {
    try {
      const res = await apiPost<{ resetToken?: string; resetUrl?: string }>('/auth/forgot-password', data)
      toast.success('Check your email', 'If the account exists, reset instructions were sent.')
      if (import.meta.env.DEV && res.resetUrl) {
        setDevResetUrl(res.resetUrl)
      }
    } catch (err) {
      toast.error('Request failed', getApiError(err))
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-bg px-4">
      <div className="absolute right-4 top-4">
        <ThemeToggle compact />
      </div>
      <Card className="w-full max-w-md">
        <h1 className="text-2xl font-bold">Reset password</h1>
        <p className="mt-2 text-sm text-muted">We will send a reset link if your email is registered.</p>
        <form onSubmit={handleSubmit(onSubmit)} className="mt-6 space-y-4" noValidate>
          <FormField label="Email" htmlFor="email" required error={errors.email?.message}>
            <Input id="email" type="email" autoComplete="email" {...register('email')} />
          </FormField>
          <Button type="submit" className="w-full" loading={isSubmitting}>
            Send reset link
          </Button>
        </form>
        {devResetUrl && (
          <div className="mt-4 rounded-xl border border-primary/30 bg-primary/5 p-3 text-sm">
            <p className="font-medium text-primary">Dev mode reset link</p>
            <Link to={devResetUrl} className="mt-1 block break-all text-primary hover:underline">
              {devResetUrl}
            </Link>
          </div>
        )}
        <Link to="/login" className="mt-4 block text-sm text-primary">
          Back to login
        </Link>
      </Card>
    </div>
  )
}
