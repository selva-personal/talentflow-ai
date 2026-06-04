import { HeroScene } from '@/components/three/HeroScene'
import { Navbar } from '@/components/layout/Navbar'
import { Footer } from '@/components/layout/Footer'
import { Button } from '@/components/ui/button'
import { Card } from '@/components/ui/card'
import { motion, useInView } from 'framer-motion'
import { Link } from 'react-router-dom'
import { useEffect, useRef, useState } from 'react'
import {
  Brain,
  Check,
  Code2,
  FileText,
  Map,
  Sparkles,
  Upload,
} from 'lucide-react'

function Counter({ end, suffix = '' }: { end: number; suffix?: string }) {
  const ref = useRef(null)
  const inView = useInView(ref, { once: true })
  const [val, setVal] = useState(0)
  useEffect(() => {
    if (!inView) return
    let start = 0
    const step = Math.ceil(end / 40)
    const t = setInterval(() => {
      start += step
      if (start >= end) {
        setVal(end)
        clearInterval(t)
      } else setVal(start)
    }, 30)
    return () => clearInterval(t)
  }, [inView, end])
  return (
    <span ref={ref} className="text-4xl font-bold gradient-text md:text-5xl">
      {val.toLocaleString()}
      {suffix}
    </span>
  )
}

const features = [
  { icon: Upload, title: 'ATS Analysis', desc: 'Upload PDF resumes and get AI-powered ATS scores and recommendations.' },
  { icon: Brain, title: 'Mock Interviews', desc: 'Chat-style AI interviewer with real-time feedback and session history.' },
  { icon: Code2, title: 'Coding Assessments', desc: 'Practice in JS, TS, Java, Python with AI code review and complexity analysis.' },
  { icon: Map, title: 'Career Roadmaps', desc: 'Personalized learning paths, projects, and milestones to your target role.' },
  { icon: FileText, title: 'Cover Letters', desc: 'Generate professional cover letters and export to PDF instantly.' },
]

const plans = [
  { name: 'Starter', price: 0, features: ['3 resume scans/mo', '5 mock sessions', 'Basic analytics'] },
  { name: 'Pro', price: 29, popular: true, features: ['Unlimited resumes', 'Unlimited mocks', 'Coding + roadmaps', 'Priority AI'] },
  { name: 'Enterprise', price: 99, features: ['Team dashboard', 'SSO', 'Custom branding', 'Dedicated support'] },
]

const faqs = [
  { q: 'How does ATS scoring work?', a: 'We extract text from your PDF and analyze it with Gemini against industry ATS criteria.' },
  { q: 'Is my data secure?', a: 'Yes. JWT auth, encrypted passwords, and your files stay in your private workspace.' },
  { q: 'Which languages are supported for coding?', a: 'JavaScript, TypeScript, Java, and Python with AI-powered review.' },
]

export function LandingPage() {
  return (
    <div className="min-h-screen bg-bg">
      <Navbar />
      <section className="relative overflow-hidden pt-24 pb-20">
        <HeroScene />
        <div className="relative mx-auto max-w-7xl px-6 text-center">
          <motion.div initial={{ opacity: 0, y: 30 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.7 }}>
            <span className="inline-flex items-center gap-2 rounded-full border border-primary/30 bg-primary/10 px-4 py-1 text-sm text-primary">
              <Sparkles className="h-4 w-4" /> AI-Powered Interview Coach
            </span>
            <h1 className="mt-6 text-5xl font-extrabold tracking-tight md:text-7xl">
              Land your dream job with{' '}
              <span className="gradient-text">TalentFlow AI</span>
            </h1>
            <p className="mx-auto mt-6 max-w-2xl text-lg text-muted">
              Resume ATS analysis, mock interviews, coding practice, career roadmaps, and cover letters — all in one premium platform.
            </p>
            <div className="mt-10 flex flex-wrap justify-center gap-4">
              <Link to="/register">
                <Button variant="primary" className="px-8 py-3 text-base">
                  Start Free — No Card Required
                </Button>
              </Link>
              <a href="#features">
                <Button variant="outline" className="px-8 py-3 text-base">
                  Explore Features
                </Button>
              </a>
            </div>
          </motion.div>
        </div>
      </section>

      <section id="features" className="py-20">
        <div className="mx-auto max-w-7xl px-6">
          <h2 className="text-center text-3xl font-bold md:text-4xl">Everything you need to <span className="gradient-text">ace interviews</span></h2>
          <div className="mt-12 grid gap-6 md:grid-cols-2 lg:grid-cols-3">
            {features.map((f, i) => (
              <motion.div key={f.title} initial={{ opacity: 0, y: 20 }} whileInView={{ opacity: 1, y: 0 }} transition={{ delay: i * 0.08 }} viewport={{ once: true }}>
                <Card className="h-full hover:scale-[1.02]">
                  <f.icon className="h-8 w-8 text-primary" />
                  <h3 className="mt-4 text-lg font-semibold">{f.title}</h3>
                  <p className="mt-2 text-sm text-muted">{f.desc}</p>
                </Card>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      <section className="border-y border-white/5 bg-surface/30 py-16">
        <div className="mx-auto grid max-w-5xl grid-cols-2 gap-8 px-6 text-center md:grid-cols-4">
          <div><Counter end={50000} suffix="+" /><p className="mt-2 text-sm text-muted">Questions practiced</p></div>
          <div><Counter end={12000} suffix="+" /><p className="mt-2 text-sm text-muted">Resumes analyzed</p></div>
          <div><Counter end={98} suffix="%" /><p className="mt-2 text-sm text-muted">User satisfaction</p></div>
          <div><Counter end={150} suffix="+" /><p className="mt-2 text-sm text-muted">Countries</p></div>
        </div>
      </section>

      <section className="py-20">
        <div className="mx-auto max-w-7xl px-6">
          <h2 className="text-center text-3xl font-bold">Loved by candidates worldwide</h2>
          <div className="mt-10 grid gap-6 md:grid-cols-3">
            {['TalentFlow helped me 3x my interview callbacks.', 'The mock chat feels like a real senior engineer interview.', 'ATS score went from 62 to 89 in one week.'].map((t, i) => (
              <Card key={i}><p className="text-muted">&ldquo;{t}&rdquo;</p><p className="mt-4 text-sm font-medium">— Verified User</p></Card>
            ))}
          </div>
        </div>
      </section>

      <section id="pricing" className="py-20">
        <div className="mx-auto max-w-7xl px-6">
          <h2 className="text-center text-3xl font-bold">Simple, transparent pricing</h2>
          <div className="mt-12 grid gap-6 md:grid-cols-3">
            {plans.map((p) => (
              <Card key={p.name} className={p.popular ? 'border-primary ring-2 ring-primary/30' : ''}>
                {p.popular && <span className="text-xs font-semibold text-primary">Most Popular</span>}
                <h3 className="mt-2 text-xl font-bold">{p.name}</h3>
                <p className="mt-2 text-3xl font-bold">${p.price}<span className="text-sm text-muted">/mo</span></p>
                <ul className="mt-6 space-y-2 text-sm text-muted">
                  {p.features.map((f) => (
                    <li key={f} className="flex items-center gap-2"><Check className="h-4 w-4 text-success" />{f}</li>
                  ))}
                </ul>
                <Link to="/register" className="mt-6 block">
                  <Button variant={p.popular ? 'primary' : 'outline'} className="w-full">Get started</Button>
                </Link>
              </Card>
            ))}
          </div>
        </div>
      </section>

      <section id="faq" className="py-20">
        <div className="mx-auto max-w-3xl px-6">
          <h2 className="text-center text-3xl font-bold">FAQ</h2>
          <div className="mt-10 space-y-4">
            {faqs.map((f) => (
              <Card key={f.q}>
                <h3 className="font-semibold">{f.q}</h3>
                <p className="mt-2 text-sm text-muted">{f.a}</p>
              </Card>
            ))}
          </div>
        </div>
      </section>

      <Footer />
    </div>
  )
}
