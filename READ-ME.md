# Botswana EMR Backend – Setup and Environment Variables

See [README-BACKEND.md](README-BACKEND.md) for Keycloak, SSL, and deployment.

## Overview

1. Copy the env from env_samples
2. Fill in values in `.env` (do not commit; it is gitignore).
3. Make sure to save them at the project root level

## Prerequisites

JDK 11+, Maven 3, PostgreSQL, Docker, Keycloak (see README-BACKEND.md).

---

## Deployment Script (`deploy.sh`)

A `deploy.sh` script is provided at the repository root to streamline pulling the latest code and starting services via Docker Compose. It handles branch selection, git sync, env file merging, and selective service builds in one step.

### Repository Structure

```
repo-root/
├── deploy.sh                          # ← Deployment script
├── docker-compose.yml                 # ← Root-level compose file
├── .env.general                       # ← Shared env vars (all services)
├── .env.BotswanaEMRAuthentication     # ← Auth service env vars
├── .env.BotswanaEMRFileStorage        # ← FileStorage service env vars
├── .env.BotswanaEMRAmbulance          # ← Ambulance service env vars
├── .env.BotswanaEMRFacility           # ← Facility service env vars
├── .env.BotswanaEMRAppointments       # ← Appointments service env vars
├── .env.BotswanaEMRNotifications      # ← Notifications service env vars
├── BotswanaEMRAuthentication/
│   └── Dockerfile
├── BotswanaEMRFileStorage/
│   └── Dockerfile
└── ...
```

Each module keeps its own `Dockerfile` inside its folder. The root `docker-compose.yml` references each one via `context: ./ModuleName`.

### First-Time Setup

Make the script executable:

```bash
chmod +x deploy.sh
```

### Running the Script

```bash
sudo ./deploy.sh
```

The script will guide you through three steps interactively.

#### Step 1 — Branch Selection and Pull

The script detects the current branch and checkouts to the main branch and pulls the latest changes.

#### Step 2 — Module Selection

```
Which module(s) would you like to build and run?

  0) ALL modules
  1) BotswanaEMRAuthentication
  2) BotswanaEMRFileStorage
  3) BotswanaEMRAmbulance
  4) BotswanaEMRFacility
  5) BotswanaEMRAppointments
  6) BotswanaEMRNotifications

Enter your choice (e.g. 1  or  1 3 5  or  0 for all):
```

Enter `0` to build and start all services, or a space-separated list of numbers to target specific ones (e.g. `1 3` builds only Auth and Ambulance).

#### Step 3 — Environment Merge and Build

The script merges environment files in this order:

1. `.env.general` — always loaded first (shared variables for all services)
2. `.env.<ModuleName>` — loaded for each selected module

The merged result is written to `.env.merged` at the repo root, which the `docker-compose.yml` reads via `env_file: .env.merged`. 
This means module-specific values override general ones if there are duplicates.

Docker Compose then builds and starts only the selected services:

```bash
docker compose -f docker-compose.yml --env-file .env.merged up -d --build <services>
```

### Environment Files

| File | Purpose |
|------|---------|
| `.env.general` | Shared config: OpenMRS, Keycloak, Mail, internal service URLs |
| `.env.BotswanaEMRAuthentication` | Auth-specific: DB URL, port, app name, JPA settings |
| `.env.BotswanaEMRFileStorage` | FileStorage-specific: DB URL, upload dir, port, multipart settings |
| `.env.BotswanaEMRAmbulance` | Ambulance-specific: DB URL, port |
| `.env.BotswanaEMRFacility` | Facility-specific: DB URL, port |
| `.env.BotswanaEMRAppointments` | Appointments-specific: DB URL, port, OpenMRS endpoints |
| `.env.BotswanaEMRNotifications` | Notifications-specific: DB URL, port |

> **Note:** None of the `.env.*` files should be committed to version control. They are gitignored.

---

Each backend service loads the root `.env` via `env_file`.

---

## Environment Variables Reference

All variables in `.env.example` are documented below. **Required** = service will not work without it.

### Profile

- **SPRING_PROFILES_ACTIVE**: Active profile (dev/server/prod). Used by: Auth, Notifications, Ambulance, FileStorage. Optional.

### OpenMRS

- **OPENMRS_USERNAME**, **OPENMRS_PASSWORD**, **OPENMRS_URL**: API credentials and base URL. Used by: Auth, Appointments. Required.
- **OPENMRS_TIMESLOTS**, **OPENMRS_TYPES**, **OPENMRS_BOOK**, **OPENMRS_SLOT**, **OPENMRS_APPOINTMENT**, **OPENMRS_SCHEDULE**, **OPENMRS_APPS**, **OPENMRS_VISITS**: Appointment/visit endpoints. Used by: Appointments. Required for Appointments.

### Keycloak

- **KEYCLOAK_AUTH_SERVER_URL**, **KEYCLOAK_RESOURCE**, **KEYCLOAK_REALM**: Server URL, client ID, realm. Used by: Auth, Notifications, Appointments, Ambulance. Required.
- **APP_KEYCLOAK_CLIENT_SECRET**: Client secret. Used by: Auth, Notifications, Appointments, Ambulance. Required.
- **KEYCLOAK_PUBLIC_CLIENT**, **KEYCLOAK_SSL_REQUIRED**, **KEYCLOAK_USE_RESOURCE_ROLE_MAPPINGS**, **KEYCLOAK_BEARER_ONLY**, **KEYCLOAK_DISABLE_TRUST_MANAGER**, **KEYCLOAK_CONFIDENTIAL_PORT**: Adapter options. Optional.
- **APP_KEYCLOAK_GRANT_TYPE**, **APP_KEYCLOAK_SCOPE**, **APP_KEYCLOAK_CLIENT_ID**, **APP_KEYCLOAK_AUTH_SERVER_URL**, **APP_KEYCLOAK_REFRESH**, **APP_KEYCLOAK_REALM_USERNAME**, **APP_KEYCLOAK_REALM_PASSWORD**: App Keycloak config. Optional.

### Mail (SMTP)

- **SPRING_MAIL_HOST**, **SPRING_MAIL_PORT**, **SPRING_MAIL_USERNAME**, **SPRING_MAIL_PASSWORD**: SMTP config. Used by: Auth, Notifications. Required if mail is used.
- **SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH**, **SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE**: Optional.

### Database

- **SPRING_DATASOURCE_URL**: JDBC URL; per-service DB name (authentication, bitri_notifications, file_storage, bitri_ambulance, facility). Used by: Auth, Notifications, FileStorage, Ambulance, Facility. Required.
- **SPRING_DATASOURCE_USERNAME**, **SPRING_DATASOURCE_PASSWORD**: DB credentials. Required.
- **SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE**, **SPRING_JPA_HIBERNATE_DDL_AUTO**, **SPRING_JPA_SHOW_SQL**: Optional.

### Service URLs (internal)

- **AUTHENTICATION_SERVICE_URL**: Auth service base URL for internal calls. Used by: Notifications, Appointments, Ambulance. Required.
- **NOTIFICATION_SERVICE_URL**: Notification service base URL. Used by: Notifications, Appointments, Ambulance. Required.

### FileStorage

- **FILE_UPLOAD_DIR**: Upload directory. Used by: FileStorage. Required.
- **FILE_STORAGE_SERVER_PORT**, **SPRING_SERVLET_MULTIPART_***: Port and multipart settings (e.g. MAX_FILE_SIZE, MAX_REQUEST_SIZE). Optional.

### Appointments (OAuth2 / codec)

- **SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_ARTICLES_CLIENT_OIDC_CLIENT_ID**, **SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_SPRING_ISSUER_URI**: OIDC client. Optional.
- **SPRING_CODEC_MAX_IN_MEMORY_SIZE**, **SPRING_MVC_PATHMATCH_MATCHING_STRATEGY**: Optional.

### Server / application

- **SERVER_PORT**: HTTP port (per service: Auth 8081, Notifications 7001, FileStorage 7003, Appointments 8082, Ambulance 7002, Facility 7000). Optional.
- **SPRING_APPLICATION_NAME**, **SERVER_ERROR_INCLUDE_MESSAGE**: Optional.