import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Link, useNavigate } from 'react-router-dom'
import { apiPost, getApiError } from '@/lib/api'
import { toast } from '@/lib/toast'
import { useAuthStore, type AuthUser } from '@/stores/authStore'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { FormField } from '@/components/ui/form-field'
import { Card } from '@/components/ui/card'
import { ThemeToggle } from '@/components/ui/theme-toggle'
import { motion } from 'framer-motion'

const schema = z.object({
  email: z.string().email('Enter a valid email'),
  password: z.string().min(8, 'Password must be at least 8 characters'),
  rememberMe: z.boolean().optional(),
})

type Form = z.infer<typeof schema>

export function LoginPage() {
  const navigate = useNavigate()
  const setTokens = useAuthStore((s) => s.setTokens)
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<Form>({ resolver: zodResolver(schema) })

  const onSubmit = async (data: Form) => {
    try {
      const res = await apiPost<{ accessToken: string; refreshToken: string; user: AuthUser }>(
        '/auth/login',
        { email: data.email, password: data.password, rememberMe: data.rememberMe ?? false }
      )
      setTokens(res.accessToken, res.refreshToken, res.user)
      toast.success('Welcome back!', `Signed in as ${res.user.firstName}`)
      navigate('/app/dashboard')
    } catch (err) {
      toast.error('Sign in failed', getApiError(err, 'Invalid email or password'))
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-bg px-4">
      <div className="absolute right-4 top-4">
        <ThemeToggle compact />
      </div>
      <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} className="w-full max-w-md">
        <Card>
          <Link to="/" className="text-xl font-bold gradient-text">TalentFlow AI</Link>
          <h1 className="mt-6 text-2xl font-bold">Welcome back</h1>
          <p className="text-sm text-muted">Sign in to continue your interview prep</p>
          <form onSubmit={handleSubmit(onSubmit)} className="mt-6 space-y-4" noValidate>
            <FormField label="Email" htmlFor="email" required error={errors.email?.message}>
              <Input id="email" type="email" autoComplete="email" {...register('email')} />
            </FormField>
            <FormField label="Password" htmlFor="password" required error={errors.password?.message}>
              <Input id="password" type="password" autoComplete="current-password" {...register('password')} />
            </FormField>
            <label className="flex items-center gap-2 text-sm text-muted">
              <input type="checkbox" className="rounded border-border" {...register('rememberMe')} />
              Remember me for 28 days
            </label>
            <Button type="submit" className="w-full" loading={isSubmitting}>Sign in</Button>
          </form>
          <p className="mt-4 text-center text-sm text-muted">
            <Link to="/forgot-password" className="text-primary hover:underline">Forgot password?</Link>
          </p>
          <p className="mt-2 text-center text-sm text-muted">
            No account? <Link to="/register" className="text-primary hover:underline">Register</Link>
          </p>
        </Card>
      </motion.div>
    </div>
  )
}
