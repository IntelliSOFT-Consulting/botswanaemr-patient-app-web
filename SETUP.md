# Botswana EMR Backend – Setup and Environment Variables

See [README-BACKEND.md](README-BACKEND.md) for Keycloak, SSL, and deployment.

## Overview

1. Copy `.env.example` to `.env`: `cp .env.example .env`
2. Fill in values in `.env` (do not commit; it is gitignored).
3. Run via Docker Compose or Maven. Spring Boot maps `UPPER_SNAKE` env vars to `lowercase.dots` properties.

## Prerequisites

JDK 11+, Maven 3, PostgreSQL, Docker, Keycloak (see README-BACKEND.md).

## Profiles

- **dev**: Local DB and service URLs (localhost or 0.0.0.0).
- **server**: Deployed or Docker; remote DB and internal hostnames (e.g. authservice:8081).
- **prod**: FileStorage only; production DB and upload paths.

Set `SPRING_PROFILES_ACTIVE=dev` or `server` or `prod` in `.env`.

## Docker

From repo root, with `.env` present:

```bash
cd docker
docker compose --env-file ../.env up -d
```

Each backend service loads the root `.env` via `env_file`.

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
- **FILE_STORAGE_SERVER_PORT**, **SPRING_SERVLET_MULTIPART_*: Port and multipart settings (e.g. MAX_FILE_SIZE, MAX_REQUEST_SIZE). Optional.

### Appointments (OAuth2 / codec)

- **SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_ARTICLES_CLIENT_OIDC_CLIENT_ID**, **SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_SPRING_ISSUER_URI**: OIDC client. Optional.
- **SPRING_CODEC_MAX_IN_MEMORY_SIZE**, **SPRING_MVC_PATHMATCH_MATCHING_STRATEGY**: Optional.

### Server / application

- **SERVER_PORT**: HTTP port (per service: Auth 8081, Notifications 7001, FileStorage 7003, Appointments 8082, Ambulance 7002, Facility 7000). Optional.
- **SPRING_APPLICATION_NAME**, **SERVER_ERROR_INCLUDE_MESSAGE**: Optional.
