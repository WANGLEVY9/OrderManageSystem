#!/usr/bin/env bash
set -euo pipefail
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="$ROOT_DIR/../docker-compose.yml"

if [[ "${1:-}" == "--rebuild" ]]; then
  docker-compose -f "$COMPOSE_FILE" up -d --build
else
  docker-compose -f "$COMPOSE_FILE" up -d
fi

echo "Compose started. Tail backend logs with: docker-compose -f $COMPOSE_FILE logs -f backend"