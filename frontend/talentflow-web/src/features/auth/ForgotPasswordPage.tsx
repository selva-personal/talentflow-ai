import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Link } from 'react-router-dom'
import { apiPost } from '@/lib/api'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Card } from '@/components/ui/card'
import { useState } from 'react'

const schema = z.object({ email: z.string().email() })

export function ForgotPasswordPage() {
  const [sent, setSent] = useState(false)
  const { register, handleSubmit, formState: { isSubmitting } } = useForm({
    resolver: zodResolver(schema),
  })

  const onSubmit = async (data: z.infer<typeof schema>) => {
    await apiPost('/auth/forgot-password', data)
    setSent(true)
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-bg px-4">
      <Card className="w-full max-w-md">
        <h1 className="text-2xl font-bold">Reset password</h1>
        {sent ? (
          <p className="mt-4 text-muted">If your email exists, reset instructions were sent.</p>
        ) : (
          <form onSubmit={handleSubmit(onSubmit)} className="mt-6 space-y-4">
            <Input type="email" placeholder="Email" {...register('email')} />
            <Button type="submit" className="w-full" loading={isSubmitting}>Send reset link</Button>
          </form>
        )}
        <Link to="/login" className="mt-4 block text-sm text-primary">Back to login</Link>
      </Card>
    </div>
  )
}
