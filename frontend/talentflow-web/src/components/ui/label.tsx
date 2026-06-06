import * as LabelPrimitive from '@radix-ui/react-label'
import { cn } from '@/lib/utils'

export function Label({
  className,
  required,
  children,
  ...props
}: React.ComponentProps<typeof LabelPrimitive.Root> & { required?: boolean }) {
  return (
    <LabelPrimitive.Root
      className={cn('mb-1.5 block text-sm font-medium text-text', className)}
      {...props}
    >
      {children}
      {required && <span className="ml-0.5 text-warning">*</span>}
    </LabelPrimitive.Root>
  )
}
