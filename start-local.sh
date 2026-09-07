#!/usr/bin/env bash
set -euo pipefail

# Function to handle errors
fail() {
  echo " Error: $1"
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

[ -f ".env" ] || fail "Missing .env file. Copy .env.example to .env and configure the Neon datasource."

# Export .env values for validation and to ensure they take precedence over stale
# datasource variables in the parent shell. Values are never evaluated as shell code.
while IFS= read -r line || [ -n "$line" ]; do
  line="${line%$'\r'}"
  [[ "$line" =~ ^[[:space:]]*$ || "$line" =~ ^[[:space:]]*# ]] && continue
  [[ "$line" == *=* ]] || continue

  name="${line%%=*}"
  value="${line#*=}"
  name="${name#"${name%%[![:space:]]*}"}"
  name="${name%"${name##*[![:space:]]}"}"
  [[ "$name" =~ ^[A-Za-z_][A-Za-z0-9_]*$ ]] || continue

  if [[ "$value" == \"*\" && "$value" == *\" ]] || [[ "$value" == \'*\' && "$value" == *\' ]]; then
    value="${value:1:${#value}-2}"
  fi
  export "$name=$value"
done < ".env"

for name in SPRING_DATASOURCE_URL SPRING_DATASOURCE_USERNAME SPRING_DATASOURCE_PASSWORD; do
  value="${!name:-}"
  if [ -z "$value" ] || [[ "$value" == *YOUR_* ]]; then
    fail "Missing required environment variable: $name. Set it in .env first."
  fi
done

export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-dev}"

requested_port="${PORT:-8080}"
available_port=$(find_available_port "$requested_port") || fail "No available application port found between $requested_port and 8090."
if [ "$available_port" != "$requested_port" ]; then
  echo " Port $requested_port is already in use; using port $available_port instead."
fi
export PORT="$available_port"

echo " Starting Spring Boot with the datasource configured in .env."
echo " Application port: $PORT"

./mvnw spring-boot:run || fail "Spring Boot application failed to start."
