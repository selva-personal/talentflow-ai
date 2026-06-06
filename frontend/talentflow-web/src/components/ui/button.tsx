import { cn } from '@/lib/utils'
import { forwardRef, type ButtonHTMLAttributes } from 'react'

type Variant = 'primary' | 'secondary' | 'ghost' | 'outline'

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant
  loading?: boolean
}

const variants: Record<Variant, string> = {
  primary:
    'bg-primary text-white hover:bg-primary/90 shadow-lg shadow-primary/30',
  secondary:
    'bg-secondary/20 text-secondary border border-secondary/40 hover:bg-secondary/30',
  ghost: 'bg-transparent text-text hover:bg-border/50',
  outline:
    'border border-border bg-transparent hover:border-primary/50 hover:bg-primary/10',
}

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant = 'primary', loading, children, disabled, type = 'button', ...props }, ref) => (
    <button
      ref={ref}
      type={type}
      disabled={disabled || loading}
      className={cn(
        'inline-flex items-center justify-center gap-2 rounded-xl px-5 py-2.5 text-sm font-semibold transition-all duration-200 disabled:opacity-50',
        variants[variant],
        className
      )}
      {...props}
    >
      {loading ? (
        <span className="h-4 w-4 animate-spin rounded-full border-2 border-white/30 border-t-white" />
      ) : null}
      {children}
    </button>
  )
)
Button.displayName = 'Button'
