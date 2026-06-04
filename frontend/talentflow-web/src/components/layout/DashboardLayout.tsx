import { Link, NavLink, Outlet } from 'react-router-dom'
import { useAuthStore } from '@/stores/authStore'
import {
  BarChart3,
  Brain,
  Code2,
  FileText,
  LayoutDashboard,
  LogOut,
  Map,
  MessageSquare,
  Upload,
} from 'lucide-react'
import { cn } from '@/lib/utils'

const nav = [
  { to: '/app/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/app/resume', icon: Upload, label: 'Resume' },
  { to: '/app/interviews', icon: Brain, label: 'Interviews' },
  { to: '/app/mock', icon: MessageSquare, label: 'Mock Chat' },
  { to: '/app/coding', icon: Code2, label: 'Coding' },
  { to: '/app/roadmap', icon: Map, label: 'Roadmap' },
  { to: '/app/cover-letter', icon: FileText, label: 'Cover Letter' },
  { to: '/app/analytics', icon: BarChart3, label: 'Analytics' },
]

export function DashboardLayout() {
  const user = useAuthStore((s) => s.user)
  const logout = useAuthStore((s) => s.logout)

  return (
    <div className="flex min-h-screen bg-bg">
      <aside className="hidden w-64 flex-col border-r border-white/5 bg-surface/80 p-4 md:flex">
        <Link to="/" className="mb-8 text-lg font-bold gradient-text">
          TalentFlow AI
        </Link>
        <nav className="flex flex-1 flex-col gap-1">
          {nav.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                cn(
                  'flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm transition',
                  isActive
                    ? 'bg-primary/20 text-primary'
                    : 'text-muted hover:bg-white/5 hover:text-text'
                )
              }
            >
              <item.icon className="h-4 w-4" />
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className="border-t border-white/5 pt-4">
          <p className="truncate text-sm font-medium">
            {user?.firstName} {user?.lastName}
          </p>
          <p className="truncate text-xs text-muted">{user?.email}</p>
          <button
            type="button"
            onClick={logout}
            className="mt-3 flex items-center gap-2 text-sm text-muted hover:text-text"
          >
            <LogOut className="h-4 w-4" /> Sign out
          </button>
        </div>
      </aside>
      <main className="flex-1 overflow-auto p-6 md:p-8">
        <Outlet />
      </main>
    </div>
  )
}
