import { Moon, Sun, Monitor } from 'lucide-react'
import { useThemeStore, type ThemeMode } from '@/stores/themeStore'
import { cn } from '@/lib/utils'

const modes: { value: ThemeMode; icon: typeof Sun; label: string }[] = [
  { value: 'light', icon: Sun, label: 'Light' },
  { value: 'dark', icon: Moon, label: 'Dark' },
  { value: 'system', icon: Monitor, label: 'System' },
]

export function ThemeToggle({ compact }: { compact?: boolean }) {
  const mode = useThemeStore((s) => s.mode)
  const setMode = useThemeStore((s) => s.setMode)

  if (compact) {
    const next: ThemeMode = mode === 'dark' ? 'light' : mode === 'light' ? 'system' : 'dark'
    const Icon = mode === 'light' ? Sun : mode === 'dark' ? Moon : Monitor
    return (
      <button
        type="button"
        onClick={() => setMode(next)}
        className="rounded-xl border border-border p-2 text-muted transition hover:bg-surface hover:text-text"
        aria-label={`Theme: ${mode}. Click to switch.`}
        title={`Theme: ${mode}`}
      >
        <Icon className="h-4 w-4" />
      </button>
    )
  }

  return (
    <div className="flex rounded-xl border border-border p-1" role="group" aria-label="Theme">
      {modes.map(({ value, icon: Icon, label }) => (
        <button
          key={value}
          type="button"
          onClick={() => setMode(value)}
          className={cn(
            'flex items-center gap-1.5 rounded-lg px-2.5 py-1.5 text-xs font-medium transition',
            mode === value ? 'bg-primary/20 text-primary' : 'text-muted hover:text-text'
          )}
          aria-pressed={mode === value}
        >
          <Icon className="h-3.5 w-3.5" />
          {label}
        </button>
      ))}
    </div>
  )
}
