import { cn } from '@/lib/utils'
import type { HTMLAttributes } from 'react'

export function Card({ className, ...props }: HTMLAttributes<HTMLDivElement>) {
  return (
    <div
      className={cn('glass rounded-2xl p-6 transition hover:border-primary/40', className)}
      {...props}
    />
  )
}
