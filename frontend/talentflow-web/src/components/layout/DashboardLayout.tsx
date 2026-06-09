import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useState } from 'react'
import { useAuthStore } from '@/stores/authStore'
import { useAiStore } from '@/stores/aiStore'
import { useQuery, useQueryClient, useMutation } from '@tanstack/react-query'
import { toast } from '@/lib/toast'
import { apiGet, apiPost, apiPatch, type SpringPage } from '@/lib/api'
import {
  BarChart3,
  Bell,
  Brain,
  Code2,
  FileText,
  LayoutDashboard,
  LogOut,
  Map,
  Menu,
  MessageSquare,
  Settings,
  Shield,
  Upload,
  User,
} from 'lucide-react'
import { cn } from '@/lib/utils'
import { Sheet } from '@/components/ui/sheet'
import { ThemeToggle } from '@/components/ui/theme-toggle'
import { Dialog } from '@/components/ui/dialog'

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

interface NotificationItem {
  id: number
  title: string
  message: string
  type: string
  read: boolean
  createdAt: string
}

function NavItems({ onNavigate, isAdmin }: { onNavigate?: () => void; isAdmin?: boolean }) {
  return (
    <>
      {nav.map((item) => (
        <NavLink
          key={item.to}
          to={item.to}
          onClick={onNavigate}
          className={({ isActive }) =>
            cn(
              'flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm transition',
              isActive
                ? 'bg-primary/20 text-primary'
                : 'text-muted hover:bg-border/50 hover:text-text'
            )
          }
        >
          <item.icon className="h-4 w-4" />
          {item.label}
        </NavLink>
      ))}
      {isAdmin && (
        <NavLink
          to="/app/admin"
          onClick={onNavigate}
          className={({ isActive }) =>
            cn(
              'flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm transition',
              isActive
                ? 'bg-primary/20 text-primary'
                : 'text-muted hover:bg-border/50 hover:text-text'
            )
          }
        >
          <Shield className="h-4 w-4" />
          Admin
        </NavLink>
      )}
    </>
  )
}

export function DashboardLayout() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const user = useAuthStore((s) => s.user)
  const logout = useAuthStore((s) => s.logout)
  const [mobileOpen, setMobileOpen] = useState(false)
  const [profileOpen, setProfileOpen] = useState(false)
  const [notifOpen, setNotifOpen] = useState(false)
  const [settingsOpen, setSettingsOpen] = useState(false)
  const [logoutOpen, setLogoutOpen] = useState(false)

  const { data: dashboard } = useQuery({
    queryKey: ['analytics-dashboard'],
    queryFn: () =>
      apiGet<{
        unreadNotifications: number
        recentActivity: { action: string; createdAt: string }[]
      }>('/analytics/dashboard'),
  })

  const { data: notifications, isLoading: notifLoading } = useQuery({
    queryKey: ['notifications'],
    queryFn: () => apiGet<SpringPage<NotificationItem>>('/notifications?size=20'),
    enabled: notifOpen,
  })

  const markRead = useMutation({
    mutationFn: (id: number) => apiPatch(`/notifications/${id}/read`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] })
      queryClient.invalidateQueries({ queryKey: ['analytics-dashboard'] })
    },
  })

  const markAllRead = useMutation({
    mutationFn: () => apiPost('/notifications/read-all'),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] })
      queryClient.invalidateQueries({ queryKey: ['analytics-dashboard'] })
      toast.success('All notifications marked as read')
    },
  })

  const unread = dashboard?.unreadNotifications ?? 0
  const notifList = notifications?.content ?? []
  const isAdmin = user?.role === 'ADMIN'
  const aiFallback = useAiStore((s) => s.fallbackActive)
  const activeProvider = useAiStore((s) => s.provider)
  const aiOnline = !aiFallback && activeProvider !== 'fallback'

  useQuery({
    queryKey: ['ai-status'],
    queryFn: async () => {
      const status = await apiGet<{
        geminiStatus: string
        quotaStatus: string
        fallbackActive: boolean
        activeProvider: string
        lastSuccessfulRequest?: string | null
        lastFailureReason?: string | null
      }>('/ai/status')
      useAiStore.getState().setStatus(status)
      return status
    },
    refetchInterval: 60_000,
  })

  const handleLogout = async () => {
    try {
      await apiPost('/auth/logout')
    } catch {
      // Continue client logout even if server call fails
    }
    logout()
    useAiStore.getState().clear()
    queryClient.clear()
    setLogoutOpen(false)
    toast.success('Signed out', 'See you next time!')
    navigate('/login', { replace: true })
  }

  return (
    <div className="flex min-h-screen bg-bg">
      <aside className="hidden w-64 flex-col border-r border-border bg-surface/80 p-4 md:flex">
        <Link to="/" className="mb-8 text-lg font-bold gradient-text">
          TalentFlow AI
        </Link>
        <nav className="flex flex-1 flex-col gap-1">
          <NavItems isAdmin={isAdmin} />
        </nav>
        <div className="space-y-3 border-t border-border pt-4">
          <ThemeToggle />
          <button
            type="button"
            onClick={() => setProfileOpen(true)}
            className="flex w-full items-center gap-2 rounded-xl px-3 py-2 text-sm text-muted transition hover:bg-border/50 hover:text-text"
          >
            <User className="h-4 w-4" /> Profile
          </button>
          <button
            type="button"
            onClick={() => setLogoutOpen(true)}
            className="flex w-full items-center gap-2 text-sm text-muted transition hover:text-text"
          >
            <LogOut className="h-4 w-4" /> Sign out
          </button>
        </div>
      </aside>

      <div className="flex flex-1 flex-col">
        <header className="flex h-14 items-center justify-between border-b border-border px-4 md:px-8">
          <button
            type="button"
            className="rounded-xl p-2 text-muted md:hidden"
            onClick={() => setMobileOpen(true)}
            aria-label="Open menu"
          >
            <Menu className="h-5 w-5" />
          </button>
          <p className="hidden text-sm text-muted md:block">
            {user?.firstName} {user?.lastName}
            {aiOnline && (
              <span className="ml-2 rounded-full border border-success/40 bg-success/10 px-2 py-0.5 text-xs font-medium text-success">
                Online AI Mode
              </span>
            )}
            {aiFallback && !aiOnline && (
              <span className="ml-2 rounded-full border border-warning/40 bg-warning/10 px-2 py-0.5 text-xs font-medium text-warning">
                Offline AI Mode
              </span>
            )}
          </p>
          <div className="flex items-center gap-2">
            <ThemeToggle compact />
            <button
              type="button"
              onClick={() => setNotifOpen(true)}
              className="relative rounded-xl p-2 text-muted transition hover:bg-border/50 hover:text-text"
              aria-label="Notifications"
            >
              <Bell className="h-5 w-5" />
              {unread > 0 && (
                <span className="absolute right-1 top-1 flex h-4 w-4 items-center justify-center rounded-full bg-primary text-[10px] font-bold text-white">
                  {unread}
                </span>
              )}
            </button>
            <button
              type="button"
              onClick={() => setSettingsOpen(true)}
              className="rounded-xl p-2 text-muted transition hover:bg-border/50 hover:text-text"
              aria-label="Settings"
            >
              <Settings className="h-5 w-5" />
            </button>
          </div>
        </header>

        <main className="flex-1 overflow-auto p-4 md:p-8">
          <Outlet />
        </main>
      </div>

      <Sheet open={mobileOpen} onClose={() => setMobileOpen(false)} title="Menu" side="left">
        <nav className="flex flex-col gap-1">
          <NavItems onNavigate={() => setMobileOpen(false)} isAdmin={isAdmin} />
        </nav>
        <div className="mt-6 space-y-2 border-t border-border pt-4">
          <ThemeToggle />
          <button
            type="button"
            onClick={() => {
              setMobileOpen(false)
              setLogoutOpen(true)
            }}
            className="flex w-full items-center gap-2 rounded-xl px-3 py-2.5 text-sm text-muted transition hover:bg-border/50 hover:text-text"
          >
            <LogOut className="h-4 w-4" /> Sign out
          </button>
        </div>
      </Sheet>

      <Sheet open={profileOpen} onClose={() => setProfileOpen(false)} title="Profile" side="right">
        <div className="space-y-3 text-sm">
          <p>
            <span className="text-muted">Name</span>
            <br />
            {user?.firstName} {user?.lastName}
          </p>
          <p>
            <span className="text-muted">Email</span>
            <br />
            {user?.email}
          </p>
          <p>
            <span className="text-muted">Role</span>
            <br />
            {user?.role}
          </p>
        </div>
      </Sheet>

      <Sheet
        open={notifOpen}
        onClose={() => setNotifOpen(false)}
        title="Notifications"
        description={unread > 0 ? `${unread} unread` : 'All caught up'}
        side="right"
      >
        {unread > 0 && (
          <button
            type="button"
            onClick={() => markAllRead.mutate()}
            className="mb-4 text-sm font-medium text-primary hover:underline"
            disabled={markAllRead.isPending}
          >
            Mark all as read
          </button>
        )}
        {notifLoading ? (
          <p className="text-sm text-muted">Loading notifications…</p>
        ) : notifList.length === 0 ? (
          <p className="text-sm text-muted">No notifications yet.</p>
        ) : (
          <ul className="space-y-3">
            {notifList.map((n) => (
              <li
                key={n.id}
                className={cn(
                  'rounded-xl border border-border p-3 text-sm',
                  !n.read && 'border-primary/30 bg-primary/5'
                )}
              >
                <div className="flex items-start justify-between gap-2">
                  <p className="font-medium">{n.title}</p>
                  {!n.read && (
                    <button
                      type="button"
                      onClick={() => markRead.mutate(n.id)}
                      className="shrink-0 text-xs text-primary hover:underline"
                    >
                      Mark read
                    </button>
                  )}
                </div>
                <p className="mt-1 text-muted">{n.message}</p>
                <p className="mt-2 text-xs text-muted">{new Date(n.createdAt).toLocaleString()}</p>
              </li>
            ))}
          </ul>
        )}
      </Sheet>

      <Sheet open={settingsOpen} onClose={() => setSettingsOpen(false)} title="Settings" side="right">
        <div className="space-y-4">
          <div>
            <p className="mb-2 text-sm font-medium">Appearance</p>
            <ThemeToggle />
          </div>
        </div>
      </Sheet>

      <Dialog
        open={logoutOpen}
        onOpenChange={setLogoutOpen}
        title="Sign out?"
        description="You will need to sign in again to access your dashboard."
        variant="warning"
        confirmLabel="Sign out"
        cancelLabel="Cancel"
        onConfirm={handleLogout}
      />
    </div>
  )
}
