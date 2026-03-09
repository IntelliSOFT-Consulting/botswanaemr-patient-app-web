#!/bin/bash

# =============================================================================
# PgBouncer userlist.txt Generator
# Fetches SCRAM-SHA-256 password hashes from PostgreSQL and writes userlist.txt
# =============================================================================

set -e

# ─── Configuration ────────────────────────────────────────────────────────────
PG_HOST="${PG_HOST:-172.105.157.130}"
PG_PORT="${PG_PORT:-5432}"
PG_SUPERUSER="${PG_SUPERUSER:-intellisoft}"          # Must be a superuser
OUTPUT_FILE="${OUTPUT_FILE:-./pgbouncer/userlist.txt}"

# Users to fetch hashes for (space-separated)
# These are the users your microservices connect with
DB_USERS=("intellisoft")                              # Add more if needed e.g. ("user1" "user2")

# ─── Colors ───────────────────────────────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# ─── Functions ────────────────────────────────────────────────────────────────

print_banner() {
  echo -e "${BLUE}"
  echo "╔══════════════════════════════════════════════╗"
  echo "║     PgBouncer userlist.txt Generator         ║"
  echo "╚══════════════════════════════════════════════╝"
  echo -e "${NC}"
}

check_dependencies() {
  echo -e "${YELLOW}► Checking dependencies...${NC}"
  if ! command -v psql &>/dev/null; then
    echo -e "${RED}✗ psql not found. Install PostgreSQL client:${NC}"
    echo "  Ubuntu/Debian: sudo apt install postgresql-client"
    echo "  Mac:           brew install libpq && brew link --force libpq"
    exit 1
  fi
  echo -e "${GREEN}✓ psql found${NC}"
}

check_connection() {
  echo -e "${YELLOW}► Testing connection to PostgreSQL at ${PG_HOST}:${PG_PORT}...${NC}"
  if ! PGPASSWORD="$PG_SUPERUSER_PASSWORD" psql \
    -h "$PG_HOST" \
    -p "$PG_PORT" \
    -U "$PG_SUPERUSER" \
    -d postgres \
    -c '\q' 2>/dev/null; then
    echo -e "${RED}✗ Cannot connect to PostgreSQL. Check host, port, and credentials.${NC}"
    exit 1
  fi
  echo -e "${GREEN}✓ Connected successfully${NC}"
}

ensure_scram_encryption() {
  echo -e "${YELLOW}► Checking password_encryption setting...${NC}"

  ENCRYPTION=$(PGPASSWORD="$PG_SUPERUSER_PASSWORD" psql \
    -h "$PG_HOST" \
    -p "$PG_PORT" \
    -U "$PG_SUPERUSER" \
    -d postgres \
    -tAc "SHOW password_encryption;")

  echo -e "  Current setting: ${BLUE}${ENCRYPTION}${NC}"

  if [ "$ENCRYPTION" != "scram-sha-256" ]; then
    echo -e "${YELLOW}  ⚠ Setting password_encryption to scram-sha-256...${NC}"
    PGPASSWORD="$PG_SUPERUSER_PASSWORD" psql \
      -h "$PG_HOST" \
      -p "$PG_PORT" \
      -U "$PG_SUPERUSER" \
      -d postgres \
      -c "ALTER SYSTEM SET password_encryption = 'scram-sha-256';"

    PGPASSWORD="$PG_SUPERUSER_PASSWORD" psql \
      -h "$PG_HOST" \
      -p "$PG_PORT" \
      -U "$PG_SUPERUSER" \
      -d postgres \
      -c "SELECT pg_reload_conf();"

    echo -e "${GREEN}  ✓ Updated to scram-sha-256${NC}"
    echo -e "${YELLOW}  ⚠ NOTE: Existing users need their password reset to use SCRAM.${NC}"
  else
    echo -e "${GREEN}  ✓ Already using scram-sha-256${NC}"
  fi
}

fetch_hashes() {
  echo -e "${YELLOW}► Fetching password hashes from pg_shadow...${NC}"

  # Create output directory if it doesn't exist
  mkdir -p "$(dirname "$OUTPUT_FILE")"

  # Clear or create the output file
  > "$OUTPUT_FILE"

  local found=0
  local missing=0

  for USER in "${DB_USERS[@]}"; do
    echo -e "  Processing user: ${BLUE}${USER}${NC}"

    HASH=$(PGPASSWORD="$PG_SUPERUSER_PASSWORD" psql \
      -h "$PG_HOST" \
      -p "$PG_PORT" \
      -U "$PG_SUPERUSER" \
      -d postgres \
      -tAc "SELECT passwd FROM pg_shadow WHERE usename = '${USER}';")

    if [ -z "$HASH" ]; then
      echo -e "  ${RED}✗ User '${USER}' not found or has no password set${NC}"
      ((missing++)) || true

    elif [[ "$HASH" != SCRAM-SHA-256* ]]; then
      echo -e "  ${YELLOW}⚠ User '${USER}' is using ${HASH:0:10}... (not SCRAM-SHA-256)${NC}"
      echo -e "  ${YELLOW}  Run option [3] to reset the password with SCRAM encryption${NC}"
      ((missing++)) || true

    else
      echo "\"${USER}\" \"${HASH}\"" >> "$OUTPUT_FILE"
      echo -e "  ${GREEN}✓ Hash written for '${USER}'${NC}"
      ((found++)) || true
    fi
  done

  echo ""
  echo -e "  Users written:  ${GREEN}${found}${NC}"
  echo -e "  Users skipped:  ${RED}${missing}${NC}"
}

reset_user_password() {
  echo ""
  read -rp "Enter the username to reset: " RESET_USER
  read -rsp "Enter new password for '${RESET_USER}': " NEW_PASS
  echo ""

  PGPASSWORD="$PG_SUPERUSER_PASSWORD" psql \
    -h "$PG_HOST" \
    -p "$PG_PORT" \
    -U "$PG_SUPERUSER" \
    -d postgres \
    -c "ALTER USER \"${RESET_USER}\" WITH PASSWORD '${NEW_PASS}';"

  echo -e "${GREEN}✓ Password updated. Re-run option [1] to regenerate userlist.txt${NC}"
}

reload_pgbouncer() {
  echo -e "${YELLOW}► Reloading PgBouncer...${NC}"

  if docker ps --format '{{.Names}}' | grep -q "spring_pgbouncer"; then
    docker restart spring_pgbouncer
    echo -e "${GREEN}✓ PgBouncer restarted${NC}"
  else
    echo -e "${RED}✗ Container 'spring_pgbouncer' not found. Is it running?${NC}"
    echo "  Start it with: docker compose -f docker-compose-pgbouncer.yaml up -d"
  fi
}

show_userlist() {
  if [ -f "$OUTPUT_FILE" ]; then
    echo -e "${BLUE}► Contents of ${OUTPUT_FILE}:${NC}"
    echo "──────────────────────────────────────────────"
    cat "$OUTPUT_FILE"
    echo "──────────────────────────────────────────────"
  else
    echo -e "${RED}✗ ${OUTPUT_FILE} not found. Run option [1] first.${NC}"
  fi
}

# ─── Main Menu ────────────────────────────────────────────────────────────────

print_banner
check_dependencies

# Prompt for superuser password securely
read -rsp "Enter password for PostgreSQL superuser '${PG_SUPERUSER}': " PG_SUPERUSER_PASSWORD
echo ""

check_connection
echo ""

echo -e "${BLUE}What would you like to do?${NC}"
echo "  [1] Generate userlist.txt (fetch hashes)"
echo "  [2] Reset a user's password to SCRAM-SHA-256"
echo "  [3] Generate + reload PgBouncer"
echo "  [4] Show current userlist.txt"
echo "  [5] Exit"
echo ""
read -rp "Choose an option [1-5]: " OPTION

case $OPTION in
  1)
    ensure_scram_encryption
    fetch_hashes
    echo ""
    echo -e "${GREEN}✅ Done! userlist.txt written to: ${OUTPUT_FILE}${NC}"
    ;;
  2)
    reset_user_password
    ;;
  3)
    ensure_scram_encryption
    fetch_hashes
    reload_pgbouncer
    echo ""
    echo -e "${GREEN}✅ Done! userlist.txt updated and PgBouncer reloaded.${NC}"
    ;;
  4)
    show_userlist
    ;;
  5)
    echo "Bye!"
    exit 0
    ;;
  *)
    echo -e "${RED}Invalid option${NC}"
    exit 1
    ;;
esac