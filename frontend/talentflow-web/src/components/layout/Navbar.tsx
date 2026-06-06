import { Link } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import { ThemeToggle } from '@/components/ui/theme-toggle'
import { useAuthStore } from '@/stores/authStore'
import { motion } from 'framer-motion'

export function Navbar() {
  const isAuth = useAuthStore((s) => s.isAuthenticated())

  return (
    <motion.header
      initial={{ y: -20, opacity: 0 }}
      animate={{ y: 0, opacity: 1 }}
      className="fixed top-0 z-50 w-full border-b border-border bg-bg/80 backdrop-blur-xl"
    >
      <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-6">
        <Link to="/" className="text-xl font-bold tracking-tight">
          <span className="gradient-text">TalentFlow</span>
          <span className="text-muted"> AI</span>
        </Link>
        <nav className="hidden items-center gap-8 text-sm text-muted md:flex">
          <a href="#features" className="transition hover:text-text">Features</a>
          <a href="#pricing" className="transition hover:text-text">Pricing</a>
          <a href="#faq" className="transition hover:text-text">FAQ</a>
        </nav>
        <div className="flex items-center gap-3">
          <ThemeToggle compact />
          {isAuth ? (
            <Link to="/app/dashboard">
              <Button variant="primary">Dashboard</Button>
            </Link>
          ) : (
            <>
              <Link to="/login">
                <Button variant="ghost">Log in</Button>
              </Link>
              <Link to="/register">
                <Button variant="primary">Get Started</Button>
              </Link>
            </>
          )}
        </div>
      </div>
    </motion.header>
  )
}
