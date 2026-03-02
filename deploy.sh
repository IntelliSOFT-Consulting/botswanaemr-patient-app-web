#!/bin/bash

set -e

# ============================================================
# CONFIGURATION
# ============================================================
REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="$REPO_ROOT/docker-compose.yml"

MODULES=(
  "BotswanaEMRAuthentication"
  "BotswanaEMRFileStorage"
  "BotswanaEMRAmbulance"
  "BotswanaEMRFacility"
  "BotswanaEMRAppointments"
  "BotswanaEMRNotifications"
)

SERVICES=(
  "authservice"
  "fileservice"
  "ambulanceservice"
  "facilityservice"
  "appointmentservice"
  "notificationservice"
)

# ============================================================
# COLORS
# ============================================================
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

# ============================================================
# STEP 1: Pull latest from git
# ============================================================
echo -e "${CYAN}========================================${NC}"
echo -e "${CYAN}  Git pull                             ${NC}"
echo -e "${CYAN}========================================${NC}"
cd "$REPO_ROOT"

CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "unknown")
echo -e "Current branch: ${YELLOW}${CURRENT_BRANCH}${NC}"
echo ""
echo -e "  1) Pull from current branch ${YELLOW}(${CURRENT_BRANCH})${NC}"
echo -e "  2) Switch to ${YELLOW}main${NC} and pull"
echo ""
read -rp "Choose [1/2] (default: 1): " BRANCH_CHOICE

case "${BRANCH_CHOICE:-1}" in
  2)
    TARGET_BRANCH="main"
    echo -e "Switching to ${YELLOW}main${NC}..."
    if ! git checkout main; then
      echo -e "${RED}✘ Failed to checkout main. Aborting.${NC}"
      exit 1
    fi
    ;;
  *)
    TARGET_BRANCH="$CURRENT_BRANCH"
    ;;
esac

# Attempt pull; if object errors occur, do a fetch + reset instead
echo -e "Pulling latest from origin/${TARGET_BRANCH}..."
if ! git pull origin "$TARGET_BRANCH" 2>&1; then
  echo -e "${YELLOW}⚠ Standard pull failed (possibly corrupted objects). Attempting fetch + reset...${NC}"
  git fetch --all
  if git reset --hard "origin/${TARGET_BRANCH}"; then
    echo -e "${GREEN}✔ Reset to origin/${TARGET_BRANCH} succeeded.${NC}"
  else
    echo -e "${RED}✘ Could not sync with origin/${TARGET_BRANCH}. Check your git remote and try:${NC}"
    echo -e "    git remote prune origin"
    echo -e "    git fetch --all"
    echo -e "    git reset --hard origin/${TARGET_BRANCH}"
    exit 1
  fi
fi

echo -e "${GREEN}✔ Repository up to date on branch: ${TARGET_BRANCH}${NC}\n"


# ============================================================
# STEP 2: Ask which module(s) to build
# ============================================================
echo -e "${YELLOW}Which module(s) would you like to build and run?${NC}"
echo ""
echo "  0) ALL modules"
for i in "${!MODULES[@]}"; do
  echo "  $((i+1))) ${MODULES[$i]}"
done
echo ""
read -rp "Enter your choice (e.g. 1  or  1 3 5  or  0 for all): " -a CHOICES

SELECTED_SERVICES=()

if [[ "${CHOICES[0]}" == "0" ]]; then
  SELECTED_SERVICES=("${SERVICES[@]}")
  echo -e "\n${GREEN}✔ Building all modules.${NC}"
else
  for choice in "${CHOICES[@]}"; do
    idx=$((choice-1))
    if [[ $idx -lt 0 || $idx -ge ${#MODULES[@]} ]]; then
      echo -e "${RED}Invalid choice: $choice — skipping.${NC}"
    else
      SELECTED_SERVICES+=("${SERVICES[$idx]}")
      echo -e "${GREEN}  + ${MODULES[$idx]}${NC}"
    fi
  done
fi

if [[ ${#SELECTED_SERVICES[@]} -eq 0 ]]; then
  echo -e "${RED}No valid modules selected. Exiting.${NC}"
  exit 1
fi

# ============================================================
# STEP 3: Merge .env files
#   Combines env.general + module-specific .env into one
#   temporary .env that docker-compose picks up.
# ============================================================
echo -e "\n${CYAN}Merging environment files...${NC}"

MERGED_ENV="$REPO_ROOT/.env.merged"

# Start fresh
> "$MERGED_ENV"

# Always include general env
if [[ -f "$REPO_ROOT/.env.general" ]]; then
  echo "# === .env.general ===" >> "$MERGED_ENV"
  cat "$REPO_ROOT/.env.general" >> "$MERGED_ENV"
  echo "" >> "$MERGED_ENV"
  echo -e "${GREEN}  ✔ Loaded .env.general${NC}"
else
  echo -e "${RED}  ✘ .env.general not found at $REPO_ROOT/.env.general${NC}"
fi

# Determine which module-specific .env files to load
# If ALL modules selected, load all; otherwise load matching ones
if [[ "${CHOICES[0]}" == "0" ]]; then
  MODULE_INDICES=("${!MODULES[@]}")
else
  MODULE_INDICES=()
  for choice in "${CHOICES[@]}"; do
    idx=$((choice-1))
    if [[ $idx -ge 0 && $idx -lt ${#MODULES[@]} ]]; then
      MODULE_INDICES+=("$idx")
    fi
  done
fi

for idx in "${MODULE_INDICES[@]}"; do
  MODULE_ENV="$REPO_ROOT/.env.${MODULES[$idx]}"
  if [[ -f "$MODULE_ENV" ]]; then
    echo "# === .env.${MODULES[$idx]} ===" >> "$MERGED_ENV"
    cat "$MODULE_ENV" >> "$MERGED_ENV"
    echo "" >> "$MERGED_ENV"
    echo -e "${GREEN}  ✔ Loaded .env.${MODULES[$idx]}${NC}"
  else
    echo -e "${YELLOW}  ⚠ No module env found: $MODULE_ENV (skipping)${NC}"
  fi
done

echo -e "${GREEN}✔ Merged env written to .env.merged${NC}\n"

# ============================================================
# STEP 4: Build and run selected services
# ============================================================
echo -e "${CYAN}========================================${NC}"
echo -e "${CYAN}  Building and starting services...${NC}"
echo -e "${CYAN}========================================${NC}"

# Export merged env path so compose can reference it
export ENV_FILE="$MERGED_ENV"

# Build the --env-file flag and service list for docker compose
ENV_FILE_FLAG="--env-file $MERGED_ENV"

docker compose -f "$COMPOSE_FILE" $ENV_FILE_FLAG \
  up -d --build "${SELECTED_SERVICES[@]}"

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  ✔ Done! Running containers:${NC}"
echo -e "${GREEN}========================================${NC}"
docker compose -f "$COMPOSE_FILE" ps "${SELECTED_SERVICES[@]}"

# ============================================================
# STEP 5: Optionally push images to Docker Hub
# ============================================================
echo ""
read -rp "Push built images to Docker Hub? [y/N]: " PUSH_CHOICE

if [[ "$PUSH_CHOICE" == "y" || "$PUSH_CHOICE" == "Y" ]]; then
  echo -e "\n${CYAN}========================================${NC}"
  echo -e "${CYAN}  Pushing images to Docker Hub...      ${NC}"
  echo -e "${CYAN}========================================${NC}"

  # Check docker login
  if ! docker info 2>/dev/null | grep -q "Username"; then
    echo -e "${YELLOW}⚠ Not logged in to Docker Hub. Running docker login...${NC}"
    docker login || { echo -e "${RED}✘ Docker login failed. Aborting push.${NC}"; exit 1; }
  fi

  docker compose -f "$COMPOSE_FILE" $ENV_FILE_FLAG push "${SELECTED_SERVICES[@]}"

  echo ""
  echo -e "${GREEN}✔ Images pushed successfully.${NC}"
else
  echo -e "${YELLOW}Skipping push.${NC}"
fi

# Cleanup merged env (optional — comment out if you want to inspect it)
 rm -f "$MERGED_ENV"