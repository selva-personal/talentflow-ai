import { Label } from '@/components/ui/label'
import { cn } from '@/lib/utils'
import type { ReactNode } from 'react'

interface FormFieldProps {
  label: string
  htmlFor?: string
  required?: boolean
  hint?: string
  error?: string
  children: ReactNode
  className?: string
}

export function FormField({
  label,
  htmlFor,
  required,
  hint,
  error,
  children,
  className,
}: FormFieldProps) {
  return (
    <div className={cn('flex flex-col', className)}>
      <Label htmlFor={htmlFor} required={required}>
        {label}
      </Label>
      {children}
      {hint && !error && <p className="mt-1 text-xs text-muted">{hint}</p>}
      {error && <p className="mt-1 text-xs text-warning">{error}</p>}
    </div>
  )
}
