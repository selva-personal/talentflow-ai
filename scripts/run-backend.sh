#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT/backend/talentflow-api"

if [[ -f .env ]]; then
  set -a
  # shellcheck disable=SC1091
  source .env
  set +a
fi

export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-dev}"

echo "Starting TalentFlow API (profile: $SPRING_PROFILES_ACTIVE)"
echo "  API:    http://localhost:8080/api/v1"
echo "  Swagger http://localhost:8080/swagger-ui.html"

if [[ -x ./mvnw ]]; then
  ./mvnw spring-boot:run
else
  mvn spring-boot:run
fi
