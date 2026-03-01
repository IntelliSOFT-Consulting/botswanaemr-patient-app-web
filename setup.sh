#!/usr/bin/env bash
# =============================================================================
# BotswanaEMR Enterprise Deployment Script
# =============================================================================
# Version:      2.0.0
# Author:       Platform Engineering Team
# Description:  Production-grade, idempotent deployment orchestrator for the
#               BotswanaEMR modular Spring Boot system.
#
# Usage:
#   ./deploy_botswana_emr.sh [OPTIONS]
#
# Options:
#   --allow-root          Allow execution as root (not recommended)
#   --non-interactive     Run without prompts (requires --env-profile)
#   --env-profile <path>  Path to environment profile override file
#   --dry-run             Simulate execution without making changes
#   --help                Display this help message
#
# Examples:
#   ./deploy_botswana_emr.sh
#   ./deploy_botswana_emr.sh --dry-run
#   ./deploy_botswana_emr.sh --non-interactive --env-profile ./ci.env
#   ./deploy_botswana_emr.sh --allow-root --dry-run
#
# Exit Codes:
#   0   Success
#   1   General error
#   2   Invalid argument / usage error
#   3   Missing dependency
#   4   Environment validation failure
#   5   Port conflict or validation failure
#   6   Docker error
#   7   Git error
#   8   Build failure
#
# Security Notes:
#   - Never run as root unless absolutely required (use --allow-root)
#   - .env files must be chmod 600 (script enforces this)
#   - Secrets are never echoed or logged
#   - All user inputs are sanitized against injection
#   - Docker network is validated before container attachment
#
# CI/CD Integration:
#   Set the following environment variables before invoking:
#     CI_MODE=true
#     SELECTED_MODULES="BotswanaEMRAmbulance,BotswanaEMRAuthentication"
#     MODULE_PORTS="BotswanaEMRAmbulance=8090,BotswanaEMRAuthentication=8091"
#     DEPLOY_ACTION=build
#   Then call: ./deploy_botswana_emr.sh --non-interactive --env-profile ./ci.env
# =============================================================================

set -euo pipefail
IFS=$'\n\t'

# =============================================================================
# CONSTANTS & GLOBALS
# =============================================================================

readonly SCRIPT_NAME="$(basename "${BASH_SOURCE[0]}")"
readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly TIMESTAMP="$(date +%Y%m%d_%H%M%S)"
readonly LOG_FILE="${SCRIPT_DIR}/logs/deploy_${TIMESTAMP}.log"
readonly DOCKER_NETWORK="docker_emr_network"
readonly MAIN_BRANCH="main"
readonly CONTAINER_PORT=8081   # Internal port is always 8081 per spec
readonly VALID_PORT_MIN=1024
readonly VALID_PORT_MAX=65535

readonly -a MODULES=(
  "BotswanaEMRAmbulance"
  "BotswanaEMRAuthentication"
  "BotswanaEMRFacility"
  "BotswanaEMRFileStorage"
  "BotswanaEMRNotifications"
)

# Runtime flags (mutated by argument parsing)
ALLOW_ROOT=false
NON_INTERACTIVE=false
DRY_RUN=false
ENV_PROFILE=""

# Runtime state
declare -A SELECTED_MODULE_PORTS  # module -> external port
declare -a SELECTED_MODULES_LIST  # ordered list of selected modules

# =============================================================================
# LOGGING
# =============================================================================

_ensure_log_dir() {
  mkdir -p "${SCRIPT_DIR}/logs"
}

_log() {
  local level="$1"
  shift
  local message="$*"
  local ts
  ts="$(date '+%Y-%m-%d %H:%M:%S')"
  local formatted="[${ts}] [${level}] ${message}"
  # Write to stdout (color) and log file (plain)
  case "${level}" in
    INFO)  printf "\033[0;32m%s\033[0m\n" "${formatted}" ;;
    WARN)  printf "\033[0;33m%s\033[0m\n" "${formatted}" ;;
    ERROR) printf "\033[0;31m%s\033[0m\n" "${formatted}" >&2 ;;
    AUDIT) printf "\033[0;36m%s\033[0m\n" "${formatted}" ;;
    *)     printf "%s\n" "${formatted}" ;;
  esac
  echo "${formatted}" >> "${LOG_FILE}"
}

log_info()  { _log "INFO"  "$@"; }
log_warn()  { _log "WARN"  "$@"; }
log_error() { _log "ERROR" "$@"; }
log_audit() { _log "AUDIT" "$@"; }

# =============================================================================
# ERROR TRAP
# =============================================================================

_error_trap() {
  local exit_code=$?
  local line_number="${BASH_LINENO[0]}"
  local command="${BASH_COMMAND}"
  log_error "Unexpected failure at line ${line_number}: command='${command}' exit_code=${exit_code}"
  log_error "Review log file for full trace: ${LOG_FILE}"
  exit "${exit_code}"
}
trap '_error_trap' ERR

_cleanup() {
  log_info "Cleanup complete. Log saved to: ${LOG_FILE}"
}
trap '_cleanup' EXIT

# =============================================================================
# ARGUMENT PARSING
# =============================================================================

parse_arguments() {
  while [[ $# -gt 0 ]]; do
    case "$1" in
      --allow-root)
        ALLOW_ROOT=true
        shift
        ;;
      --non-interactive)
        NON_INTERACTIVE=true
        shift
        ;;
      --dry-run)
        DRY_RUN=true
        log_warn "DRY-RUN MODE ENABLED — no changes will be made."
        shift
        ;;
      --env-profile)
        if [[ -z "${2:-}" ]]; then
          log_error "--env-profile requires a path argument."
          exit 2
        fi
        ENV_PROFILE="$2"
        shift 2
        ;;
      --help|-h)
        grep '^#' "${BASH_SOURCE[0]}" | sed 's/^# \?//'
        exit 0
        ;;
      *)
        log_error "Unknown argument: $1"
        exit 2
        ;;
    esac
  done
}

# =============================================================================
# DEPENDENCY VALIDATION
# =============================================================================

validate_dependencies() {
  log_info "Validating required system dependencies..."
  local missing=()

  local required_bins=("git" "docker" "mvn" "awk" "sed" "grep")

  for bin in "${required_bins[@]}"; do
    if ! command -v "${bin}" &>/dev/null; then
      missing+=("${bin}")
    fi
  done

  # Docker Compose plugin (v2) check
  if ! docker compose version &>/dev/null 2>&1; then
    missing+=("docker-compose-plugin")
  fi

  if [[ ${#missing[@]} -gt 0 ]]; then
    log_error "Missing required dependencies: ${missing[*]}"
    log_error "Install missing tools and re-run the script."
    exit 3
  fi

  log_info "All dependencies satisfied."
}

# =============================================================================
# ROOT GUARD
# =============================================================================

check_root_privilege() {
  if [[ "${EUID}" -eq 0 ]] && [[ "${ALLOW_ROOT}" != "true" ]]; then
    log_error "Script must not be run as root. Use --allow-root to override (not recommended)."
    exit 1
  fi
  if [[ "${EUID}" -eq 0 ]]; then
    log_warn "Running as root — this is strongly discouraged in production."
    log_audit "Root execution authorized via --allow-root flag."
  fi
}

# =============================================================================
# INPUT SANITIZATION
# =============================================================================

# Strips characters that could cause injection in shell or Docker contexts
sanitize_input() {
  local input="$1"
  # Allow only alphanumeric, dash, underscore, dot, slash, colon for URLs
  echo "${input}" | tr -cd '[:alnum:]._/:@-'
}

sanitize_port() {
  local input="$1"
  echo "${input}" | tr -cd '[:digit:]'
}

# =============================================================================
# DEPLOYMENT MODE SELECTION
# =============================================================================

select_deployment_mode() {
  log_info "========================================"
  log_info "  BotswanaEMR Deployment Orchestrator  "
  log_info "========================================"

  if [[ "${NON_INTERACTIVE}" == "true" ]]; then
    log_info "Non-interactive mode: defaulting to codebase deployment."
    return 0
  fi

  echo ""
  echo "Select deployment mode:"
  echo "  [1] Deploy from cloned codebase (current)"
  echo "  [2] Deploy from prebuilt Docker image (future feature)"
  echo ""

  local choice
  while true; do
    read -rp "Enter choice [1-2]: " choice
    choice="$(sanitize_input "${choice}")"
    case "${choice}" in
      1)
        log_info "Deployment mode: Codebase selected."
        break
        ;;
      2)
        log_warn "Docker image deployment is not yet implemented."
        log_info "This feature is planned for a future release."
        log_info "Exiting cleanly."
        exit 0
        ;;
      *)
        log_warn "Invalid selection. Please enter 1 or 2."
        ;;
    esac
  done
}

# =============================================================================
# GIT / CODEBASE HANDLING
# =============================================================================

handle_codebase() {
  log_info "Checking codebase state..."

  if git -C "${SCRIPT_DIR}" rev-parse --is-inside-work-tree &>/dev/null 2>&1; then
    log_info "Git repository detected."
    local current_branch
    current_branch="$(git -C "${SCRIPT_DIR}" rev-parse --abbrev-ref HEAD)"
    log_info "Current branch: ${current_branch}"

    if [[ "${current_branch}" != "${MAIN_BRANCH}" ]]; then
      log_warn "Not on '${MAIN_BRANCH}'. Switching branches..."
      if [[ "${DRY_RUN}" != "true" ]]; then
        git -C "${SCRIPT_DIR}" checkout "${MAIN_BRANCH}" || {
          log_error "Failed to checkout branch '${MAIN_BRANCH}'."
          exit 7
        }
      else
        log_info "[DRY-RUN] Would checkout branch '${MAIN_BRANCH}'."
      fi
    fi

    log_info "Pulling latest changes from origin/${MAIN_BRANCH}..."
    if [[ "${DRY_RUN}" != "true" ]]; then
      git -C "${SCRIPT_DIR}" pull origin "${MAIN_BRANCH}" || {
        log_error "Git pull failed. Check remote connectivity and branch permissions."
        exit 7
      }
    else
      log_info "[DRY-RUN] Would pull latest from origin/${MAIN_BRANCH}."
    fi
    log_info "Codebase is up to date."

  else
    log_warn "Current directory is NOT a Git repository."

    local repo_url
    if [[ "${NON_INTERACTIVE}" == "true" ]]; then
      log_error "Non-interactive mode requires an existing Git repo. Cannot clone interactively."
      exit 7
    fi

    read -rp "Enter Git repository URL to clone: " repo_url
    repo_url="$(sanitize_input "${repo_url}")"

    if [[ -z "${repo_url}" ]]; then
      log_error "Repository URL cannot be empty."
      exit 7
    fi

    log_info "Cloning repository: ${repo_url}"
    if [[ "${DRY_RUN}" != "true" ]]; then
      git clone "${repo_url}" . || {
        log_error "Git clone failed. Verify the URL and your network/credentials."
        exit 7
      }
      git checkout "${MAIN_BRANCH}" || {
        log_error "Failed to checkout branch '${MAIN_BRANCH}' after clone."
        exit 7
      }
    else
      log_info "[DRY-RUN] Would clone '${repo_url}' and checkout '${MAIN_BRANCH}'."
    fi

    log_info "Repository cloned and checked out to '${MAIN_BRANCH}'."
  fi

  log_audit "Codebase: $(git -C "${SCRIPT_DIR}" rev-parse HEAD 2>/dev/null || echo 'unknown')"
}

# =============================================================================
# ENVIRONMENT FILE STRATEGY
# =============================================================================

_env_file_path() {
  local module="$1"
  echo "${SCRIPT_DIR}/.env.${module}"
}

_general_env_path() {
  echo "${SCRIPT_DIR}/.env.general"
}

_enforce_env_permissions() {
  local env_file="$1"
  local current_perms
  current_perms="$(stat -c '%a' "${env_file}" 2>/dev/null || stat -f '%A' "${env_file}" 2>/dev/null)"
  if [[ "${current_perms}" != "600" ]]; then
    log_warn "Insecure permissions on ${env_file} (${current_perms}). Correcting to 600."
    if [[ "${DRY_RUN}" != "true" ]]; then
      chmod 600 "${env_file}"
    else
      log_info "[DRY-RUN] Would chmod 600 ${env_file}."
    fi
  fi
}

_generate_general_env() {
  local env_file
  env_file="$(_general_env_path)"
  log_warn "Generating placeholder .env.general at: ${env_file}"
  if [[ "${DRY_RUN}" != "true" ]]; then
    cat > "${env_file}" <<'ENVEOF'
# =============================================================================
# BotswanaEMR General Environment Configuration
# REQUIRED — Populate all values before deployment
# =============================================================================

# Database
DB_HOST=CHANGE_ME
DB_PORT=5432
DB_NAME=botswana_emr
DB_USER=CHANGE_ME
DB_PASSWORD=CHANGE_ME

# Spring Datasource
SPRING_DATASOURCE_URL=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
SPRING_DATASOURCE_USERNAME=${DB_USER}
SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}

# JWT / Auth
JWT_SECRET=CHANGE_ME
JWT_EXPIRATION_MS=86400000

# External Integrations
SMTP_HOST=CHANGE_ME
SMTP_PORT=587
SMTP_USER=CHANGE_ME
SMTP_PASSWORD=CHANGE_ME
ENVEOF
    chmod 600 "${env_file}"
  else
    log_info "[DRY-RUN] Would generate ${env_file}."
  fi
}

_generate_module_env() {
  local module="$1"
  local env_file
  env_file="$(_env_file_path "${module}")"
  log_warn "Generating placeholder env file: ${env_file}"
  if [[ "${DRY_RUN}" != "true" ]]; then
    cat > "${env_file}" <<ENVEOF
# =============================================================================
# ${module} Module Environment Configuration
# REQUIRED — Populate all values before deployment
# =============================================================================

# Server port (set dynamically by deployment script)
SERVER_PORT=CHANGE_ME

# Module-specific settings
SPRING_APPLICATION_NAME=${module}
LOG_LEVEL=INFO

# (Add module-specific secrets here)
ENVEOF
    chmod 600 "${env_file}"
  else
    log_info "[DRY-RUN] Would generate ${env_file}."
  fi
}

_validate_env_mandatory_vars() {
  local env_file="$1"
  local failed=false

  log_info "Validating mandatory variables in: ${env_file}"
  while IFS= read -r line; do
    # Skip comments and empty lines
    [[ "${line}" =~ ^#.*$ || -z "${line}" ]] && continue
    # Check for CHANGE_ME placeholder
    if echo "${line}" | grep -q "CHANGE_ME"; then
      local var_name
      var_name="$(echo "${line}" | cut -d= -f1)"
      log_error "Mandatory variable not set: ${var_name} in ${env_file}"
      failed=true
    fi
  done < "${env_file}"

  if [[ "${failed}" == "true" ]]; then
    log_error "Environment validation failed. Populate all CHANGE_ME values and re-run."
    exit 4
  fi

  log_info "Environment file validated: ${env_file}"
}

validate_environment_files() {
  local module="$1"

  # General env
  local general_env
  general_env="$(_general_env_path)"
  if [[ ! -f "${general_env}" ]]; then
    _generate_general_env
    log_warn "Please populate ${general_env} and re-run the script."
    if [[ "${NON_INTERACTIVE}" != "true" ]]; then
      read -rp "Press ENTER after populating ${general_env} to continue..."
    else
      log_error "Non-interactive mode: .env.general missing and cannot prompt user. Exiting."
      exit 4
    fi
  fi
  _enforce_env_permissions "${general_env}"
  _validate_env_mandatory_vars "${general_env}"

  # Module-specific env
  local module_env
  module_env="$(_env_file_path "${module}")"
  if [[ ! -f "${module_env}" ]]; then
    _generate_module_env "${module}"
    log_warn "Please populate ${module_env} and re-run the script."
    if [[ "${NON_INTERACTIVE}" != "true" ]]; then
      read -rp "Press ENTER after populating ${module_env} to continue..."
    else
      log_error "Non-interactive mode: ${module_env} missing and cannot prompt user. Exiting."
      exit 4
    fi
  fi
  _enforce_env_permissions "${module_env}"
  # Note: SERVER_PORT validation is handled by port allocation; skip CHANGE_ME check for it
  # so we validate after port injection
}

update_module_port_in_env() {
  local module="$1"
  local port="$2"
  local module_env
  module_env="$(_env_file_path "${module}")"

  if [[ "${DRY_RUN}" != "true" ]]; then
    # Update or append SERVER_PORT
    if grep -q "^SERVER_PORT=" "${module_env}"; then
      sed -i "s|^SERVER_PORT=.*|SERVER_PORT=${port}|" "${module_env}"
    else
      echo "SERVER_PORT=${port}" >> "${module_env}"
    fi
    log_info "SERVER_PORT=${port} written to ${module_env}"
  else
    log_info "[DRY-RUN] Would set SERVER_PORT=${port} in ${module_env}"
  fi
}

# =============================================================================
# MODULE SELECTION
# =============================================================================

select_modules() {
  # CI/CD: Allow env-driven pre-selection
  if [[ -n "${CI_SELECTED_MODULES:-}" ]]; then
    IFS=',' read -ra SELECTED_MODULES_LIST <<< "${CI_SELECTED_MODULES}"
    log_info "CI mode: pre-selected modules: ${SELECTED_MODULES_LIST[*]}"
    return 0
  fi

  if [[ "${NON_INTERACTIVE}" == "true" ]]; then
    log_error "--non-interactive requires CI_SELECTED_MODULES env var to be set."
    exit 2
  fi

  echo ""
  log_info "Available BotswanaEMR Modules:"
  local i=1
  for module in "${MODULES[@]}"; do
    echo "  [${i}] ${module}"
    ((i++))
  done
  echo "  [A] All modules"
  echo ""

  local raw_selection
  read -rp "Enter module numbers (comma-separated) or 'A' for all: " raw_selection
  raw_selection="$(echo "${raw_selection}" | tr '[:lower:]' '[:upper:]' | tr -cd '[:alnum:],A')"

  SELECTED_MODULES_LIST=()

  if [[ "${raw_selection}" == "A" ]]; then
    SELECTED_MODULES_LIST=("${MODULES[@]}")
  else
    IFS=',' read -ra choices <<< "${raw_selection}"
    for choice in "${choices[@]}"; do
      local idx=$(( choice - 1 ))
      if [[ "${idx}" -ge 0 && "${idx}" -lt "${#MODULES[@]}" ]]; then
        SELECTED_MODULES_LIST+=("${MODULES[${idx}]}")
      else
        log_warn "Invalid module index: ${choice} — skipped."
      fi
    done
  fi

  if [[ ${#SELECTED_MODULES_LIST[@]} -eq 0 ]]; then
    log_error "No valid modules selected. Exiting."
    exit 2
  fi

  echo ""
  log_info "Selected modules:"
  for m in "${SELECTED_MODULES_LIST[@]}"; do
    echo "    -> ${m}"
  done
  echo ""

  if [[ "${NON_INTERACTIVE}" != "true" ]]; then
    local confirm
    read -rp "Confirm selection? [y/N]: " confirm
    if [[ "${confirm,,}" != "y" ]]; then
      log_warn "Selection cancelled by user."
      exit 0
    fi
  fi
}

# =============================================================================
# DYNAMIC PORT ALLOCATION
# =============================================================================

_is_port_numeric() {
  local port="$1"
  [[ "${port}" =~ ^[0-9]+$ ]]
}

_is_port_in_range() {
  local port="$1"
  [[ "${port}" -ge "${VALID_PORT_MIN}" && "${port}" -le "${VALID_PORT_MAX}" ]]
}

_is_port_in_use() {
  local port="$1"
  # Try ss first, fall back to netstat
  if command -v ss &>/dev/null; then
    ss -tlnp 2>/dev/null | awk '{print $4}' | grep -q ":${port}$"
  elif command -v netstat &>/dev/null; then
    netstat -tlnp 2>/dev/null | awk '{print $4}' | grep -q ":${port}$"
  else
    # Last resort: attempt a TCP bind test
    (echo "" > /dev/tcp/127.0.0.1/"${port}") 2>/dev/null
  fi
}

_is_port_used_by_selection() {
  local port="$1"
  for assigned_port in "${SELECTED_MODULE_PORTS[@]}"; do
    if [[ "${assigned_port}" == "${port}" ]]; then
      return 0
    fi
  done
  return 1
}

allocate_port_for_module() {
  local module="$1"

  # CI mode: read from CI_MODULE_PORTS env var (format: Module=Port,Module=Port)
  if [[ -n "${CI_MODULE_PORTS:-}" ]]; then
    local ci_port
    ci_port="$(echo "${CI_MODULE_PORTS}" | tr ',' '\n' | grep "^${module}=" | cut -d= -f2)"
    if [[ -n "${ci_port}" ]]; then
      ci_port="$(sanitize_port "${ci_port}")"
      _is_port_numeric "${ci_port}" && _is_port_in_range "${ci_port}" || {
        log_error "CI_MODULE_PORTS: invalid port '${ci_port}' for ${module}."
        exit 5
      }
      SELECTED_MODULE_PORTS["${module}"]="${ci_port}"
      log_info "CI mode: ${module} assigned port ${ci_port}"
      return 0
    fi
  fi

  if [[ "${NON_INTERACTIVE}" == "true" ]]; then
    log_error "Non-interactive mode requires CI_MODULE_PORTS to be set for: ${module}"
    exit 5
  fi

  local port
  while true; do
    read -rp "Enter external port for ${module} (${VALID_PORT_MIN}-${VALID_PORT_MAX}): " port
    port="$(sanitize_port "${port}")"

    if ! _is_port_numeric "${port}"; then
      log_warn "Port must be a numeric value. Try again."
      continue
    fi

    if ! _is_port_in_range "${port}"; then
      log_warn "Port ${port} is out of valid range (${VALID_PORT_MIN}-${VALID_PORT_MAX}). Try again."
      continue
    fi

    if _is_port_in_use "${port}"; then
      log_warn "Port ${port} is already bound on this host. Choose a different port."
      continue
    fi

    if _is_port_used_by_selection "${port}"; then
      log_warn "Port ${port} is already assigned to another selected module. Choose a different port."
      continue
    fi

    SELECTED_MODULE_PORTS["${module}"]="${port}"
    log_info "Port ${port} assigned to ${module}."
    break
  done
}

allocate_ports() {
  log_info "Starting dynamic port allocation..."
  for module in "${SELECTED_MODULES_LIST[@]}"; do
    allocate_port_for_module "${module}"
  done
  log_info "Port allocation complete."
}

# =============================================================================
# DOCKER NETWORK
# =============================================================================

ensure_docker_network() {
  log_info "Checking Docker network: ${DOCKER_NETWORK}"
  if docker network inspect "${DOCKER_NETWORK}" &>/dev/null; then
    log_info "Docker network '${DOCKER_NETWORK}' already exists."
  else
    log_warn "Docker network '${DOCKER_NETWORK}' not found. Creating..."
    if [[ "${DRY_RUN}" != "true" ]]; then
      docker network create \
        --driver bridge \
        --opt com.docker.network.bridge.enable_icc=true \
        "${DOCKER_NETWORK}" || {
          log_error "Failed to create Docker network '${DOCKER_NETWORK}'."
          exit 6
        }
      log_info "Docker network '${DOCKER_NETWORK}' created."
    else
      log_info "[DRY-RUN] Would create Docker network '${DOCKER_NETWORK}'."
    fi
  fi
}

validate_docker_running() {
  log_info "Validating Docker daemon is running..."
  if ! docker info &>/dev/null; then
    log_error "Docker daemon is not running. Start Docker and re-run."
    exit 6
  fi
  log_info "Docker daemon is running."
}

# =============================================================================
# DOCKERFILE GENERATION
# =============================================================================

_dockerfile_path() {
  local module="$1"
  echo "${SCRIPT_DIR}/${module}/Dockerfile"
}

generate_dockerfile_if_missing() {
  local module="$1"
  local dockerfile
  dockerfile="$(_dockerfile_path "${module}")"

  if [[ -f "${dockerfile}" ]]; then
    log_info "Dockerfile exists for ${module}."
    return 0
  fi

  log_warn "Dockerfile missing for ${module}. Generating multi-stage build template..."

  # Infer JAR name from module name (Maven convention: lowercase artifact ID)
  local jar_name="${module,,}-*.jar"

  if [[ "${DRY_RUN}" != "true" ]]; then
    mkdir -p "$(dirname "${dockerfile}")"
    cat > "${dockerfile}" <<DOCKERFILE
# =============================================================================
# ${module} — Multi-Stage Dockerfile
# Internal container port: ${CONTAINER_PORT}
# External port: injected via SERVER_PORT env var at runtime
# =============================================================================

# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /build

# Cache dependencies layer
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Build application
COPY src ./src
RUN mvn clean package -DskipTests -q

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

# Non-root runtime user
RUN addgroup -S emrgroup && adduser -S emruser -G emrgroup

WORKDIR /app

# Copy artifact from builder
COPY --from=builder /build/target/${jar_name} app.jar

# Enforce ownership
RUN chown emruser:emrgroup /app/app.jar

USER emruser

# Container always exposes ${CONTAINER_PORT} internally
EXPOSE ${CONTAINER_PORT}

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:${CONTAINER_PORT}/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
DOCKERFILE
    log_info "Dockerfile generated: ${dockerfile}"
  else
    log_info "[DRY-RUN] Would generate Dockerfile at ${dockerfile}"
  fi
}

# =============================================================================
# DOCKER COMPOSE FILE GENERATION / VALIDATION
# =============================================================================

_compose_file_path() {
  local module="$1"
  echo "${SCRIPT_DIR}/${module}/docker-compose.yml"
}

ensure_docker_compose_file() {
  local module="$1"
  local port="${SELECTED_MODULE_PORTS[${module}]}"
  local compose_file
  compose_file="$(_compose_file_path "${module}")"
  local module_lower="${module,,}"

  if [[ -f "${compose_file}" ]]; then
    log_info "docker-compose.yml exists for ${module}."
    # Update port mapping if present
    if [[ "${DRY_RUN}" != "true" ]]; then
      sed -i "s|\"[0-9]*:${CONTAINER_PORT}\"|\"${port}:${CONTAINER_PORT}\"|g" "${compose_file}" || true
    fi
    return 0
  fi

  log_warn "docker-compose.yml missing for ${module}. Generating..."

  if [[ "${DRY_RUN}" != "true" ]]; then
    mkdir -p "$(dirname "${compose_file}")"
    cat > "${compose_file}" <<COMPOSEEOF
# =============================================================================
# ${module} — Docker Compose
# External port is sourced from SERVER_PORT (set by deployment script)
# =============================================================================

version: '3.9'

services:
  ${module_lower}:
    build:
      context: .
      dockerfile: Dockerfile
    image: ${module_lower}:latest
    container_name: ${module_lower}
    restart: unless-stopped
    env_file:
      - ../.env.general
      - ../.env.${module}
    ports:
      - "\${SERVER_PORT}:${CONTAINER_PORT}"
    networks:
      - emr_network
    healthcheck:
      test: ["CMD", "wget", "-qO-", "http://localhost:${CONTAINER_PORT}/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    deploy:
      resources:
        limits:
          memory: 512M
        reservations:
          memory: 256M
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "5"

networks:
  emr_network:
    external: true
    name: ${DOCKER_NETWORK}
COMPOSEEOF
    log_info "docker-compose.yml generated: ${compose_file}"
  else
    log_info "[DRY-RUN] Would generate ${compose_file}"
  fi
}

# =============================================================================
# DEPLOYMENT ACTION SELECTION
# =============================================================================

select_deploy_action() {
  # CI/CD override
  if [[ -n "${CI_DEPLOY_ACTION:-}" ]]; then
    echo "${CI_DEPLOY_ACTION}"
    return 0
  fi

  if [[ "${NON_INTERACTIVE}" == "true" ]]; then
    echo "build"
    return 0
  fi

  echo ""
  echo "Select deployment action:"
  echo "  [1] Build & Deploy (fresh build)"
  echo "  [2] Rebuild (force rebuild)"
  echo "  [3] Restart (restart existing container)"
  echo "  [4] Stop"
  echo "  [5] Remove container"
  echo ""

  local choice
  read -rp "Enter choice [1-5]: " choice
  choice="$(sanitize_input "${choice}")"

  case "${choice}" in
    1) echo "build" ;;
    2) echo "rebuild" ;;
    3) echo "restart" ;;
    4) echo "stop" ;;
    5) echo "remove" ;;
    *)
      log_warn "Invalid choice. Defaulting to 'build'."
      echo "build"
      ;;
  esac
}

# =============================================================================
# MODULE DEPLOYMENT
# =============================================================================

_run_compose() {
  local compose_file="$1"
  local module_env="$2"
  local general_env="$3"
  local action="$4"
  local module="$5"

  local compose_dir
  compose_dir="$(dirname "${compose_file}")"

  log_audit "Executing compose action='${action}' for module='${module}'"

  if [[ "${DRY_RUN}" == "true" ]]; then
    log_info "[DRY-RUN] Would run docker compose action '${action}' for ${module}"
    return 0
  fi

  case "${action}" in
    build)
      docker compose \
        --env-file "${general_env}" \
        --env-file "${module_env}" \
        -f "${compose_file}" \
        up -d --build 2>&1 | tee -a "${LOG_FILE}"
      ;;
    rebuild)
      docker compose \
        --env-file "${general_env}" \
        --env-file "${module_env}" \
        -f "${compose_file}" \
        up -d --build --force-recreate 2>&1 | tee -a "${LOG_FILE}"
      ;;
    restart)
      docker compose \
        --env-file "${general_env}" \
        --env-file "${module_env}" \
        -f "${compose_file}" \
        restart 2>&1 | tee -a "${LOG_FILE}"
      ;;
    stop)
      docker compose \
        -f "${compose_file}" \
        stop 2>&1 | tee -a "${LOG_FILE}"
      ;;
    remove)
      docker compose \
        -f "${compose_file}" \
        down --remove-orphans 2>&1 | tee -a "${LOG_FILE}"
      ;;
    *)
      log_error "Unknown action: ${action}"
      exit 1
      ;;
  esac
}

deploy_module() {
  local module="$1"
  local action="$2"
  local port="${SELECTED_MODULE_PORTS[${module}]}"

  log_info "--------------------------------------------"
  log_info "Deploying module: ${module} on port ${port}"
  log_info "--------------------------------------------"

  # Validate / generate environment files
  validate_environment_files "${module}"

  # Inject port into .env.<module>
  update_module_port_in_env "${module}" "${port}"

  # Validate .env.general mandatory vars (after port injection .env.<module> should be clean too)
  _validate_env_mandatory_vars "$(_general_env_path)"
  _validate_env_mandatory_vars "$(_env_file_path "${module}")"

  # Generate Dockerfile if missing
  generate_dockerfile_if_missing "${module}"

  # Ensure Compose file exists
  ensure_docker_compose_file "${module}"

  # Execute
  _run_compose \
    "$(_compose_file_path "${module}")" \
    "$(_env_file_path "${module}")" \
    "$(_general_env_path)" \
    "${action}" \
    "${module}"

  log_info "Module ${module} — action '${action}' completed successfully."
  log_audit "MODULE_DEPLOYED module=${module} port=${port} action=${action} time=$(date '+%Y-%m-%dT%H:%M:%S')"
}

# =============================================================================
# MAIN ORCHESTRATOR
# =============================================================================

main() {
  _ensure_log_dir
  log_audit "====== BotswanaEMR Deployment Started: $(date '+%Y-%m-%dT%H:%M:%S') ======"
  log_audit "Invoked by: $(whoami)@$(hostname) | Script: ${SCRIPT_NAME} | Args: $*"

  parse_arguments "$@"

  # Load optional env profile
  if [[ -n "${ENV_PROFILE}" ]]; then
    if [[ ! -f "${ENV_PROFILE}" ]]; then
      log_error "Specified --env-profile file not found: ${ENV_PROFILE}"
      exit 4
    fi
    # shellcheck source=/dev/null
    source "${ENV_PROFILE}"
    log_info "Loaded env profile: ${ENV_PROFILE}"
  fi

  check_root_privilege
  validate_dependencies
  select_deployment_mode
  handle_codebase
  validate_docker_running
  ensure_docker_network
  select_modules
  allocate_ports

  # Determine action (single prompt covers all selected modules)
  local deploy_action
  deploy_action="$(select_deploy_action)"
  log_info "Deploy action selected: ${deploy_action}"

  # Deploy each selected module
  local success_count=0
  local failed_modules=()

  for module in "${SELECTED_MODULES_LIST[@]}"; do
    if deploy_module "${module}" "${deploy_action}"; then
      ((success_count++))
    else
      log_error "Deployment failed for module: ${module}"
      failed_modules+=("${module}")
    fi
  done

  # Summary
  echo ""
  log_info "============================================================"
  log_info "  DEPLOYMENT SUMMARY"
  log_info "============================================================"
  log_info "  Modules targeted : ${#SELECTED_MODULES_LIST[@]}"
  log_info "  Succeeded        : ${success_count}"
  log_info "  Failed           : ${#failed_modules[@]}"

  if [[ ${#failed_modules[@]} -gt 0 ]]; then
    log_error "  Failed modules   : ${failed_modules[*]}"
  fi

  log_info "  Port assignments :"
  for module in "${SELECTED_MODULES_LIST[@]}"; do
    log_info "    ${module} -> :${SELECTED_MODULE_PORTS[${module}]}"
  done

  log_info "  Log file         : ${LOG_FILE}"
  log_info "============================================================"
  log_audit "====== BotswanaEMR Deployment Finished: $(date '+%Y-%m-%dT%H:%M:%S') ======"

  if [[ ${#failed_modules[@]} -gt 0 ]]; then
    exit 1
  fi

  exit 0
}

# =============================================================================
# ENTRY POINT
# =============================================================================

main "$@"