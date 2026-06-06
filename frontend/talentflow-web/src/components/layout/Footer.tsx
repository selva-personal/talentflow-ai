import { Link } from 'react-router-dom'

export function Footer() {
  return (
    <footer className="border-t border-border bg-surface/50 py-12">
      <div className="mx-auto grid max-w-7xl gap-8 px-6 md:grid-cols-4">
        <div>
          <p className="text-lg font-bold gradient-text">TalentFlow AI</p>
          <p className="mt-2 text-sm text-muted">Your Personal AI Interview Coach</p>
        </div>
        <div>
          <p className="font-semibold">Product</p>
          <ul className="mt-3 space-y-2 text-sm text-muted">
            <li><a href="#features">Features</a></li>
            <li><a href="#pricing">Pricing</a></li>
          </ul>
        </div>
        <div>
          <p className="font-semibold">Account</p>
          <ul className="mt-3 space-y-2 text-sm text-muted">
            <li><Link to="/login">Login</Link></li>
            <li><Link to="/register">Register</Link></li>
          </ul>
        </div>
        <div>
          <p className="font-semibold">Legal</p>
          <ul className="mt-3 space-y-2 text-sm text-muted">
            <li>Privacy Policy</li>
            <li>Terms of Service</li>
          </ul>
        </div>
      </div>
      <p className="mt-10 text-center text-xs text-muted">© 2026 TalentFlow AI. All rights reserved.</p>
    </footer>
  )
}
