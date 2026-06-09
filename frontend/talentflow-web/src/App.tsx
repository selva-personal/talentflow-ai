import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { lazy, Suspense } from 'react'
import { Toaster } from 'sonner'
import { ProtectedRoute } from '@/routes/ProtectedRoute'
import { PublicRoute } from '@/routes/PublicRoute'
import { AdminRoute } from '@/routes/AdminRoute'
import { DashboardLayout } from '@/components/layout/DashboardLayout'

const LandingPage = lazy(() => import('@/features/landing/LandingPage').then((m) => ({ default: m.LandingPage })))
const LoginPage = lazy(() => import('@/features/auth/LoginPage').then((m) => ({ default: m.LoginPage })))
const RegisterPage = lazy(() => import('@/features/auth/RegisterPage').then((m) => ({ default: m.RegisterPage })))
const ForgotPasswordPage = lazy(() => import('@/features/auth/ForgotPasswordPage').then((m) => ({ default: m.ForgotPasswordPage })))
const ResetPasswordPage = lazy(() => import('@/features/auth/ResetPasswordPage').then((m) => ({ default: m.ResetPasswordPage })))
const DashboardPage = lazy(() => import('@/features/dashboard/DashboardPage').then((m) => ({ default: m.DashboardPage })))
const AdminPage = lazy(() => import('@/features/admin/AdminPage').then((m) => ({ default: m.AdminPage })))
const AdminAiDiagnosticsPage = lazy(() =>
  import('@/features/admin/AdminAiDiagnosticsPage').then((m) => ({ default: m.AdminAiDiagnosticsPage }))
)
const ResumePage = lazy(() => import('@/features/resume/ResumePage').then((m) => ({ default: m.ResumePage })))
const InterviewPage = lazy(() => import('@/features/interview/InterviewPage').then((m) => ({ default: m.InterviewPage })))
const MockInterviewPage = lazy(() => import('@/features/mock/MockInterviewPage').then((m) => ({ default: m.MockInterviewPage })))
const CodingPage = lazy(() => import('@/features/coding/CodingPage').then((m) => ({ default: m.CodingPage })))
const RoadmapPage = lazy(() => import('@/features/roadmap/RoadmapPage').then((m) => ({ default: m.RoadmapPage })))
const CoverLetterPage = lazy(() => import('@/features/cover-letter/CoverLetterPage').then((m) => ({ default: m.CoverLetterPage })))
const AnalyticsPage = lazy(() => import('@/features/analytics/AnalyticsPage').then((m) => ({ default: m.AnalyticsPage })))

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { staleTime: 60_000, retry: 1 },
    mutations: { retry: 0 },
  },
})

function PageLoader() {
  return (
    <div className="flex min-h-[40vh] items-center justify-center">
      <div className="h-10 w-10 animate-spin rounded-full border-2 border-primary/30 border-t-primary" />
    </div>
  )
}

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Suspense fallback={<PageLoader />}>
          <Routes>
            <Route path="/" element={<LandingPage />} />
            <Route path="/reset-password" element={<ResetPasswordPage />} />
            <Route element={<PublicRoute />}>
              <Route path="/login" element={<LoginPage />} />
              <Route path="/register" element={<RegisterPage />} />
              <Route path="/forgot-password" element={<ForgotPasswordPage />} />
            </Route>
            <Route element={<ProtectedRoute />}>
              <Route path="/app" element={<DashboardLayout />}>
                <Route index element={<Navigate to="dashboard" replace />} />
                <Route path="dashboard" element={<DashboardPage />} />
                <Route path="resume" element={<ResumePage />} />
                <Route path="interviews" element={<InterviewPage />} />
                <Route path="mock" element={<MockInterviewPage />} />
                <Route path="coding" element={<CodingPage />} />
                <Route path="roadmap" element={<RoadmapPage />} />
                <Route path="cover-letter" element={<CoverLetterPage />} />
                <Route path="analytics" element={<AnalyticsPage />} />
                <Route element={<AdminRoute />}>
                  <Route path="admin" element={<AdminPage />} />
                  <Route path="admin/ai-diagnostics" element={<AdminAiDiagnosticsPage />} />
                </Route>
              </Route>
            </Route>
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </Suspense>
      </BrowserRouter>
      <Toaster
        position="top-right"
        richColors
        closeButton
        toastOptions={{
          className: 'glass border border-border',
          duration: 4000,
        }}
      />
    </QueryClientProvider>
  )
}
