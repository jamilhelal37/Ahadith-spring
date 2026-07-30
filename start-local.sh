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

./mvnw spring-boot:run || fail "Spring Boot application failed to start."