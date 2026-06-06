import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { applyTheme, useThemeStore } from '@/stores/themeStore'
import './index.css'
import App from './App.tsx'

applyTheme(useThemeStore.getState().mode)

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>
)
