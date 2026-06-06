import { motion, AnimatePresence } from 'framer-motion'
import { X } from 'lucide-react'
import { cn } from '@/lib/utils'
import type { ReactNode } from 'react'

type SheetSide = 'left' | 'right'

interface SheetProps {
  open: boolean
  onClose: () => void
  title: string
  description?: string
  side?: SheetSide
  children: ReactNode
  className?: string
}

const slideVariants = {
  left: { initial: { x: '-100%' }, animate: { x: 0 }, exit: { x: '-100%' } },
  right: { initial: { x: '100%' }, animate: { x: 0 }, exit: { x: '100%' } },
}

export function Sheet({
  open,
  onClose,
  title,
  description,
  side = 'right',
  children,
  className,
}: SheetProps) {
  return (
    <AnimatePresence>
      {open && (
        <>
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 z-50 bg-black/50 backdrop-blur-sm"
            onClick={onClose}
            aria-hidden
          />
          <motion.aside
            role="dialog"
            aria-modal="true"
            aria-label={title}
            initial={slideVariants[side].initial}
            animate={slideVariants[side].animate}
            exit={slideVariants[side].exit}
            transition={{ type: 'spring', damping: 28, stiffness: 320 }}
            className={cn(
              'glass fixed top-0 z-50 flex h-full w-full max-w-sm flex-col border-border shadow-2xl',
              side === 'left' ? 'left-0 border-r' : 'right-0 border-l',
              className
            )}
          >
            <div className="flex items-start justify-between border-b border-border p-4">
              <div>
                <h2 className="text-lg font-semibold">{title}</h2>
                {description && <p className="mt-1 text-sm text-muted">{description}</p>}
              </div>
              <button
                type="button"
                onClick={onClose}
                className="rounded-lg p-1 text-muted transition hover:bg-border hover:text-text"
                aria-label="Close drawer"
              >
                <X className="h-5 w-5" />
              </button>
            </div>
            <div className="flex-1 overflow-y-auto p-4">{children}</div>
          </motion.aside>
        </>
      )}
    </AnimatePresence>
  )
}
