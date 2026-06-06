import type { LucideIcon } from 'lucide-react'
import { Card } from '@/components/ui/card'

interface EmptyStateProps {
  icon: LucideIcon
  title: string
  description: string
}

export function EmptyState({ icon: Icon, title, description }: EmptyStateProps) {
  return (
    <Card className="flex flex-col items-center py-12 text-center">
      <Icon className="h-10 w-10 text-muted" />
      <h3 className="mt-4 font-semibold">{title}</h3>
      <p className="mt-2 max-w-sm text-sm text-muted">{description}</p>
    </Card>
  )
}
