# Multi-Provider AI — Architecture & Migration Guide

## Overview

TalentFlow AI uses a **Strategy + Chain of Responsibility** pattern for AI text generation. All feature modules call `AiService`, which delegates to `AiProviderOrchestrator` for automatic failover.

## Provider chain

Default failover order (when `AI_PROVIDER=gemini`):

```
Gemini → OpenAI → Anthropic → Ollama → Fallback (offline rules)
```

If the preferred provider fails (429 quota, network, auth), the next configured provider is tried automatically.

## Environment variables

| Variable | Description | Default |
|----------|-------------|---------|
| `AI_PROVIDER` | Preferred provider: `gemini`, `openai`, `anthropic`, `ollama`, `fallback` | `gemini` |
| `APP_AI_MODE` | `gemini` = use provider chain; `fallback` = force offline only | `gemini` |
| `GEMINI_API_KEY` | Google Gemini API key | — |
| `GEMINI_MODEL` | Gemini model | `gemini-2.0-flash` |
| `OPENAI_API_KEY` | OpenAI API key | — |
| `OPENAI_MODEL` | OpenAI model | `gpt-4o-mini` |
| `OPENAI_BASE_URL` | OpenAI API base | `https://api.openai.com/v1` |
| `ANTHROPIC_API_KEY` | Anthropic API key | — |
| `ANTHROPIC_MODEL` | Claude model | `claude-3-5-haiku-20241022` |
| `ANTHROPIC_BASE_URL` | Anthropic API base | `https://api.anthropic.com/v1` |
| `OLLAMA_BASE_URL` | Local Ollama server | `http://localhost:11434` |
| `OLLAMA_MODEL` | Ollama model | `llama3.2` |

## API endpoints

| Endpoint | Auth | Description |
|----------|------|-------------|
| `GET /api/v1/ai/status` | User | Active provider + per-provider status |
| `GET /api/v1/ai/diagnostics` | User | Full diagnostics snapshot |
| `POST /api/v1/ai/test` | User | Run failover test with prompt |
| `POST /api/v1/ai/diagnostics/probe` | User | Probe all providers |
| `GET /api/v1/admin/ai-status` | Admin | Admin AI monitor |
| `GET /api/v1/admin/ai-providers` | Admin | Provider diagnostics |
| `POST /api/v1/admin/ai-provider` | Admin | Switch preferred provider `{ "provider": "openai" }` |
| `POST /api/v1/admin/ai-providers/probe` | Admin | Probe all providers |

## Architecture

```
Feature Services (Resume, Interview, etc.)
        ↓
    AiService.generateText / generateJson
        ↓
    AiProviderOrchestrator.generateWithFailover
        ↓
    AiProviderRegistry → GeminiProvider | OpenAiProvider | AnthropicProvider | OllamaProvider
        ↓ (all fail)
    FallbackAiService (offline rules)
```

### Key classes

| Class | Role |
|-------|------|
| `AiProvider` | Strategy interface |
| `AiProviderOrchestrator` | Failover chain executor |
| `AiProviderRegistry` | Provider lookup |
| `AiProviderConfigService` | Runtime provider preference (admin switchable) |
| `AiStatusService` | Per-provider health tracking |
| `AiDiagnosticsService` | Probes and diagnostics |

## Migration from Gemini-only

### No code changes required for feature modules

All modules already use `AiService`. No direct `GeminiService` calls in feature code.

### Steps

1. **Keep existing Gemini config** — `GEMINI_API_KEY` continues to work.

2. **Add backup provider keys** (recommended when Gemini quota is exhausted):
   ```bash
   OPENAI_API_KEY=sk-...
   # or
   ANTHROPIC_API_KEY=sk-ant-...
   # or local
   OLLAMA_BASE_URL=http://localhost:11434
   ```

3. **Set preferred provider**:
   ```bash
   AI_PROVIDER=gemini   # tries Gemini first, then failover
   ```

4. **Restart backend**:
   ```bash
   ./scripts/run-backend.sh
   ```

5. **Verify** in Admin → AI Diagnostics → **Probe All Providers** or **Test Failover Chain**.

### Admin UI switch (no restart)

Admin Console → AI Monitor → **Apply Provider** selects the preferred starting provider for the failover chain.

> Runtime switch is in-memory; restart resets to `AI_PROVIDER` env value. Set env on Render for persistence.

## Render deployment

Add environment variables on the Render web service:

```
AI_PROVIDER=gemini
GEMINI_API_KEY=...
OPENAI_API_KEY=...        # optional failover
ANTHROPIC_API_KEY=...     # optional failover
```

## Troubleshooting

| Symptom | Cause | Fix |
|---------|-------|-----|
| Always fallback | No provider keys configured | Add at least one API key |
| Gemini 429, no failover | OpenAI/Anthropic not configured | Add `OPENAI_API_KEY` or `ANTHROPIC_API_KEY` |
| Ollama fails | Ollama not running locally | `ollama serve` + pull model |
| Provider switch resets | In-memory config | Set `AI_PROVIDER` in Render env |

## Frontend

- **Offline AI Mode** badge — active provider is `fallback`
- **Online AI Mode** badge — any cloud/local provider succeeded
- **Admin → AI Diagnostics** — per-provider status, latency, quota
