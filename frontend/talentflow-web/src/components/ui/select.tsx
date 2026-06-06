import { cn } from '@/lib/utils'
import { forwardRef, type SelectHTMLAttributes } from 'react'

export const Select = forwardRef<HTMLSelectElement, SelectHTMLAttributes<HTMLSelectElement>>(
  ({ className, children, ...props }, ref) => (
    <select
      ref={ref}
      className={cn(
        'w-full rounded-xl border border-border bg-surface/80 px-4 py-2.5 text-sm text-text outline-none transition focus:border-primary/60 focus:ring-2 focus:ring-primary/20',
        className
      )}
      {...props}
    >
      {children}
    </select>
  )
)
Select.displayName = 'Select'
