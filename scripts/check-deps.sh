#!/usr/bin/env sh
# Quick check that dependency ports are open (so mvn spring-boot:run can connect).
# Run from repo root. Requires: nc (netcat) or equivalent.

set -e
REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO_ROOT"

echo "Checking dependency ports (expect: MySQL 3306, Redis 6379, Cassandra 9042, RabbitMQ 61613, MinIO 9000)..."
echo ""

check() {
  host="${1:-127.0.0.1}"
  port="$2"
  name="${3:-$port}"
  if command -v nc >/dev/null 2>&1; then
    if nc -z "${host}" "${port}" 2>/dev/null; then
      echo "  OK   ${name} (${host}:${port})"
    else
      echo "  FAIL ${name} (${host}:${port}) - not open"
    fi
  else
    echo "  (skip ${name} - nc not found)"
  fi
}

check 127.0.0.1 3306 "MySQL"
check 127.0.0.1 6379 "Redis"
check 127.0.0.1 9042 "Cassandra"
check 127.0.0.1 61613 "RabbitMQ STOMP"
check 127.0.0.1 9000 "MinIO"

echo ""
echo "If any show FAIL, start dependencies first:"
echo "  docker-compose -f docker-compose/dependencies.yml up -d mysql redis cassandra rabbitmq-stomp minio"
echo "Then wait 30-60 seconds and run this script again."
