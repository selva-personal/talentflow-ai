import { cn } from '@/lib/utils'
import { forwardRef, type InputHTMLAttributes } from 'react'

export const Input = forwardRef<HTMLInputElement, InputHTMLAttributes<HTMLInputElement>>(
  ({ className, ...props }, ref) => (
    <input
      ref={ref}
      className={cn(
        'w-full rounded-xl border border-border bg-surface/80 px-4 py-2.5 text-sm text-text placeholder:text-muted outline-none transition focus:border-primary/60 focus:ring-2 focus:ring-primary/20 neu-inset',
        className
      )}
      {...props}
    />
  )
)
Input.displayName = 'Input'
