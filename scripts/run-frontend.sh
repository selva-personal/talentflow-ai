#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT/frontend/talentflow-web"

if [[ -f .env ]]; then
  set -a
  # shellcheck disable=SC1091
  source .env
  set +a
fi

export VITE_API_BASE_URL="${VITE_API_BASE_URL:-http://localhost:8080/api/v1}"

echo "Starting TalentFlow frontend at http://localhost:5173"
npm run dev
