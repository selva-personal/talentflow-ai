import * as DialogPrimitive from '@radix-ui/react-dialog'
import { motion, AnimatePresence } from 'framer-motion'
import { X } from 'lucide-react'
import { cn } from '@/lib/utils'
import type { ReactNode } from 'react'

export type ModalVariant = 'default' | 'success' | 'error' | 'warning' | 'info'

const variantStyles: Record<ModalVariant, string> = {
  default: 'border-primary/30',
  success: 'border-success/40',
  error: 'border-warning/50',
  warning: 'border-warning/40',
  info: 'border-secondary/40',
}

interface DialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  title: string
  description?: string
  children?: ReactNode
  variant?: ModalVariant
  confirmLabel?: string
  cancelLabel?: string
  onConfirm?: () => void
  hideClose?: boolean
}

export function Dialog({
  open,
  onOpenChange,
  title,
  description,
  children,
  variant = 'default',
  confirmLabel = 'OK',
  cancelLabel,
  onConfirm,
  hideClose,
}: DialogProps) {
  return (
    <DialogPrimitive.Root open={open} onOpenChange={onOpenChange}>
      <AnimatePresence>
        {open && (
          <DialogPrimitive.Portal forceMount>
            <DialogPrimitive.Overlay asChild>
              <motion.div
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0 }}
                className="fixed inset-0 z-50 bg-black/60 backdrop-blur-sm"
              />
            </DialogPrimitive.Overlay>
            <DialogPrimitive.Content asChild>
              <motion.div
                initial={{ opacity: 0, scale: 0.95, y: 8 }}
                animate={{ opacity: 1, scale: 1, y: 0 }}
                exit={{ opacity: 0, scale: 0.95, y: 8 }}
                transition={{ duration: 0.2 }}
                className={cn(
                  'glass fixed left-1/2 top-1/2 z-50 w-[calc(100%-2rem)] max-w-md -translate-x-1/2 -translate-y-1/2 rounded-2xl p-6 shadow-2xl',
                  variantStyles[variant]
                )}
              >
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <DialogPrimitive.Title className="text-lg font-semibold text-text">
                      {title}
                    </DialogPrimitive.Title>
                    {description && (
                      <DialogPrimitive.Description className="mt-2 text-sm text-muted">
                        {description}
                      </DialogPrimitive.Description>
                    )}
                  </div>
                  {!hideClose && (
                    <DialogPrimitive.Close
                      className="rounded-lg p-1 text-muted transition hover:bg-border hover:text-text"
                      aria-label="Close"
                    >
                      <X className="h-4 w-4" />
                    </DialogPrimitive.Close>
                  )}
                </div>
                {children && <div className="mt-4">{children}</div>}
                {(onConfirm || cancelLabel) && (
                  <div className="mt-6 flex justify-end gap-2">
                    {cancelLabel && (
                      <DialogPrimitive.Close className="rounded-xl border border-border px-4 py-2 text-sm font-medium text-muted transition hover:text-text">
                        {cancelLabel}
                      </DialogPrimitive.Close>
                    )}
                    {onConfirm && (
                      <button
                        type="button"
                        onClick={() => {
                          onConfirm()
                          onOpenChange(false)
                        }}
                        className="rounded-xl bg-primary px-4 py-2 text-sm font-semibold text-white transition hover:bg-primary/90"
                      >
                        {confirmLabel}
                      </button>
                    )}
                  </div>
                )}
              </motion.div>
            </DialogPrimitive.Content>
          </DialogPrimitive.Portal>
        )}
      </AnimatePresence>
    </DialogPrimitive.Root>
  )
}
