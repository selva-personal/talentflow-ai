import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Link, useNavigate } from 'react-router-dom'
import { apiPost } from '@/lib/api'
import { useAuthStore, type AuthUser } from '@/stores/authStore'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Card } from '@/components/ui/card'
import { useState } from 'react'
import { motion } from 'framer-motion'

const schema = z.object({
  email: z.string().email(),
  password: z.string().min(8),
})

type Form = z.infer<typeof schema>

export function LoginPage() {
  const navigate = useNavigate()
  const setTokens = useAuthStore((s) => s.setTokens)
  const [error, setError] = useState('')
  const { register, handleSubmit, formState: { isSubmitting } } = useForm<Form>({
    resolver: zodResolver(schema),
  })

  const onSubmit = async (data: Form) => {
    setError('')
    try {
      const res = await apiPost<{ accessToken: string; refreshToken: string; user: AuthUser }>(
        '/auth/login',
        data
      )
      setTokens(res.accessToken, res.refreshToken, res.user)
      navigate('/app/dashboard')
    } catch {
      setError('Invalid email or password')
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-bg px-4">
      <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} className="w-full max-w-md">
        <Card>
          <Link to="/" className="text-xl font-bold gradient-text">TalentFlow AI</Link>
          <h1 className="mt-6 text-2xl font-bold">Welcome back</h1>
          <p className="text-sm text-muted">Sign in to continue your interview prep</p>
          <form onSubmit={handleSubmit(onSubmit)} className="mt-6 space-y-4">
            <div>
              <label className="text-sm text-muted">Email</label>
              <Input type="email" className="mt-1" {...register('email')} />
            </div>
            <div>
              <label className="text-sm text-muted">Password</label>
              <Input type="password" className="mt-1" {...register('password')} />
            </div>
            {error && <p className="text-sm text-warning">{error}</p>}
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
