#!/bin/bash
set -e

# Function to handle errors
fail() {
  echo " Error: $1"
  if [ "$2" == "postgres" ]; then
    echo "--- Last 50 lines of PostgreSQL logs ---"
    docker compose logs --tail=50 postgres
    echo "--------------------------------------"
  fi
  exit 1
}

is_port_in_use() {
  ss -ltn "( sport = :$1 )" | grep -q ":$1"
}

find_available_port() {
  local port="$1"
  while [ "$port" -le 8090 ]; do
    if ! is_port_in_use "$port"; then
      echo "$port"
      return 0
    fi
    port=$((port + 1))
  done

  return 1
}

echo "▶ Starting local PostgreSQL container..."
docker compose up -d postgres || fail "Failed to start PostgreSQL container."

echo "⏳ Waiting for PostgreSQL to become healthy..."

for i in {1..30}; do
  health_status=$(docker inspect -f "{{.State.Health.Status}}" ahadith-postgres 2>/dev/null || echo "starting")

  if [ "$health_status" = "healthy" ]; then
    echo " PostgreSQL is healthy!"
    break
  fi

  echo "  - Health status is '$health_status'. Waiting... (Attempt $i of 30)"
  sleep 2

  if [ "$i" -eq 30 ]; then
    fail "PostgreSQL container did not become healthy after 60 seconds." "postgres"
  fi
done

echo " Starting Spring Boot application..."

if [ -f ".env" ]; then
  # Read each line, trim it, and export it if it's a valid VAR=VALUE pair.
  # This is safer than `source` for .env files with special characters.
  while IFS= read -r line || [ -n "$line" ]; do
    line_trimmed=$(echo "$line" | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//' -e 's/\r$//')
    if [[ ! "$line_trimmed" =~ ^# ]] && [[ "$line_trimmed" =~ = ]]; then
      export "$line_trimmed"
    fi
  done < ".env"
fi

LOCAL_POSTGRES_PORT="${LOCAL_POSTGRES_PORT:-5433}"
LOCAL_POSTGRES_DB="${LOCAL_POSTGRES_DB:-ahadith}"
LOCAL_POSTGRES_USERNAME="${LOCAL_POSTGRES_USERNAME:-postgres}"
LOCAL_POSTGRES_PASSWORD="${LOCAL_POSTGRES_PASSWORD:-postgres}"

export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-dev}"
export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:${LOCAL_POSTGRES_PORT}/${LOCAL_POSTGRES_DB}"
export SPRING_DATASOURCE_USERNAME="$LOCAL_POSTGRES_USERNAME"
export SPRING_DATASOURCE_PASSWORD="$LOCAL_POSTGRES_PASSWORD"

requested_port="${PORT:-8080}"
available_port=$(find_available_port "$requested_port") || fail "No available application port found between $requested_port and 8090."
if [ "$available_port" != "$requested_port" ]; then
  echo " Port $requested_port is already in use; using port $available_port instead."
fi
export PORT="$available_port"

echo " Using local database: $SPRING_DATASOURCE_URL"
echo " Application port: $PORT"

./mvnw spring-boot:run || fail "Spring Boot application failed to start."
