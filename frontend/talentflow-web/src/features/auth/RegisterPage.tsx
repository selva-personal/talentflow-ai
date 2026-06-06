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
  firstName: z.string().min(1, 'First name is required'),
  lastName: z.string().min(1, 'Last name is required'),
  email: z.string().email('Enter a valid email'),
  password: z.string().min(8, 'Password must be at least 8 characters'),
})

type Form = z.infer<typeof schema>

export function RegisterPage() {
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
        '/auth/register',
        data
      )
      setTokens(res.accessToken, res.refreshToken, res.user)
      toast.success('Account created', 'Welcome to TalentFlow AI')
      navigate('/app/dashboard')
    } catch (err) {
      toast.error('Registration failed', getApiError(err))
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
          <h1 className="mt-6 text-2xl font-bold">Create your account</h1>
          <form onSubmit={handleSubmit(onSubmit)} className="mt-6 space-y-4" noValidate>
            <div className="grid grid-cols-2 gap-3">
              <FormField label="First name" htmlFor="firstName" required error={errors.firstName?.message}>
                <Input id="firstName" autoComplete="given-name" {...register('firstName')} />
              </FormField>
              <FormField label="Last name" htmlFor="lastName" required error={errors.lastName?.message}>
                <Input id="lastName" autoComplete="family-name" {...register('lastName')} />
              </FormField>
            </div>
            <FormField label="Email" htmlFor="email" required error={errors.email?.message}>
              <Input id="email" type="email" autoComplete="email" {...register('email')} />
            </FormField>
            <FormField label="Password" htmlFor="password" required error={errors.password?.message}>
              <Input id="password" type="password" autoComplete="new-password" {...register('password')} />
            </FormField>
            <Button type="submit" className="w-full" loading={isSubmitting}>Create account</Button>
          </form>
          <p className="mt-4 text-center text-sm text-muted">
            Already have an account? <Link to="/login" className="text-primary hover:underline">Sign in</Link>
          </p>
        </Card>
      </motion.div>
    </div>
  )
}
