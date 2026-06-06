import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { apiPost, getApiError } from '@/lib/api'
import { toast } from '@/lib/toast'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { FormField } from '@/components/ui/form-field'
import { Card } from '@/components/ui/card'
import { ThemeToggle } from '@/components/ui/theme-toggle'
import { motion } from 'framer-motion'

const schema = z
  .object({
    newPassword: z.string().min(8, 'Password must be at least 8 characters'),
    confirmPassword: z.string().min(8, 'Confirm your password'),
  })
  .refine((data) => data.newPassword === data.confirmPassword, {
    message: 'Passwords do not match',
    path: ['confirmPassword'],
  })

type Form = z.infer<typeof schema>

export function ResetPasswordPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token') ?? ''

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<Form>({ resolver: zodResolver(schema) })

  const onSubmit = async (data: Form) => {
    if (!token) {
      toast.error('Invalid reset link', 'Request a new password reset email.')
      return
    }
    try {
      await apiPost('/auth/reset-password', { token, newPassword: data.newPassword })
      toast.success('Password updated', 'You can now sign in with your new password.')
      navigate('/login')
    } catch (err) {
      toast.error('Reset failed', getApiError(err))
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-bg px-4">
      <div className="absolute right-4 top-4">
        <ThemeToggle compact />
      </div>
      <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} className="w-full max-w-md">
        <Card>
          <Link to="/" className="text-xl font-bold gradient-text">
            TalentFlow AI
          </Link>
          <h1 className="mt-6 text-2xl font-bold">Reset password</h1>
          <p className="text-sm text-muted">Choose a new password for your account.</p>
          {!token && (
            <p className="mt-4 rounded-xl border border-warning/40 bg-warning/10 p-3 text-sm text-warning">
              Missing reset token. Use the link from your email or request a new reset.
            </p>
          )}
          <form onSubmit={handleSubmit(onSubmit)} className="mt-6 space-y-4" noValidate>
            <FormField label="New password" htmlFor="newPassword" required error={errors.newPassword?.message}>
              <Input id="newPassword" type="password" autoComplete="new-password" {...register('newPassword')} />
            </FormField>
            <FormField
              label="Confirm password"
              htmlFor="confirmPassword"
              required
              error={errors.confirmPassword?.message}
            >
              <Input
                id="confirmPassword"
                type="password"
                autoComplete="new-password"
                {...register('confirmPassword')}
              />
            </FormField>
            <Button type="submit" className="w-full" loading={isSubmitting} disabled={!token}>
              Update password
            </Button>
          </form>
          <p className="mt-4 text-center text-sm text-muted">
            <Link to="/login" className="text-primary hover:underline">
              Back to sign in
            </Link>
          </p>
        </Card>
      </motion.div>
    </div>
  )
}
