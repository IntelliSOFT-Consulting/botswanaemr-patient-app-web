# AGENTS.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Overview

Botswana EMR Patient App — a microservices backend (Spring Boot 2.x / Java 11 + Kotlin) with a React frontend. The backend integrates with **OpenMRS** (via REST and FHIR R4 APIs) for clinical data and **Keycloak** for authentication/authorization. Each microservice is an independent Maven project with its own Dockerfile.

## Build & Run Commands

### Backend (per-service, from the service directory)

```shell
# Build a single service (e.g. BotswanaEMRAuthentication)
./mvnw clean package -DskipTests          # inside BotswanaEMRAuthentication/

# Run a single service locally
./mvnw spring-boot:run                    # inside the service directory

# Run tests for a single service
./mvnw test                               # inside the service directory

# Run a specific test class
./mvnw -Dtest=BotswanaEmrAuthenticationApplicationTests test
```

### Docker (from repo root)

```shell
# Full stack (all services + nginx + frontend)
sudo ./deploy.sh                          # interactive: picks branch, modules, merges env files

# Build & start specific services manually
docker compose -f docker-compose.yml --env-file .env.merged up -d --build authservice ambulanceservice
```

### Frontend (from `client/`)

```shell
npm install
npm start        # dev server
npm run build    # production build
npm test         # Jest tests
```

## Architecture

### Microservices

Six independent Spring Boot services, each in its own top-level directory with a `pom.xml`, `Dockerfile`, and `src/`:

| Service | Port | API Prefix | DB | Purpose |
|---|---|---|---|---|
| BotswanaEMRAuthentication | 8081 | `/auths/` | `authentication` | User registration, login (Keycloak), email verification, password reset, patient linking to OpenMRS |
| BotswanaEMRAmbulance | 7002 | `/ambulance/` | `bitri_ambulance` | Ambulance incident management |
| BotswanaEMRAppointments | 8082 | `/appointments/` | — | Proxies to OpenMRS appointment scheduling APIs |
| BotswanaEMRFacility | 7000 | `/facility/` | `facility` | Health facility information |
| BotswanaEMRFileStorage | 7003 | `/filestorage/` | `file_storage` | File upload/download |
| BotswanaEMRNotifications | 7001 | `/notification/` | `bitri_notifications` | In-app and email notifications |

### Cross-Service Communication

- Services call each other via internal URLs configured through env vars (`AUTHENTICATION_SERVICE_URL`, `NOTIFICATION_SERVICE_URL`).
- In Docker, services resolve by container name on the `docker_emr_network` bridge network (e.g. `http://botswanaauth:8081`).

### Common Code Patterns (per service)

Each backend service follows the same internal structure:

- **Java layer**: `controller/` → `service/` (interface + `*Impl`) → `repository/` → `entity/` — standard Spring MVC + JPA pattern.
- **Kotlin layer**: Shared utility classes that appear in every service:
  - `FormatterClass.kt` — response formatting, email sending, date conversion, Keycloak user extraction.
  - `NetworkCall.kt` — Retrofit-based HTTP client for calling OpenMRS REST/FHIR APIs. Uses Kotlin coroutines (`CoroutineScope(Dispatchers.IO)`).
  - `DataClass.kt` / `Data.kt` — Kotlin data classes for request/response DTOs and OpenMRS API models.
  - `AppProperties.kt` — `@Value`-injected Spring properties for inter-service URLs.
  - `NetworkRequestInterface.kt` — Retrofit interface defining OpenMRS API endpoints.
  - `RetrofitConfig.kt` — Configures Retrofit with basic auth from `OpenMrsProperties`.

### OpenMRS Integration

- **REST API** (`/rest/v1/`): patient search, allergies, drugs, vitals, visits, conditions.
- **FHIR R4** (`/fhir2/R4/`): condition resources (newer endpoint).
- Authentication to OpenMRS uses HTTP Basic Auth, configured via `OPENMRS_USERNAME` / `OPENMRS_PASSWORD` / `OPENMRS_URL` env vars, wired through `RetrofitConfig`.

### Security

- All services use **Keycloak** Spring Boot adapter (`KeycloakWebSecurityConfigurerAdapter`).
- Public endpoints (login, register, password reset, Swagger) are explicitly whitelisted in each service's `SecurityConfig`.
- Protected endpoints require a valid Keycloak bearer token.

### Infrastructure

- **Nginx** reverse proxy routes by URL path prefix to the appropriate backend service.
- **PgBouncer** (optional) connection pooling on port 5433 in `transaction` mode. Config in `pg_bouncer/`.
- **CircleCI** builds all services on the `development-backend` branch, pushes Docker images to Docker Hub.

## Environment Configuration

- `.env.general` — shared vars (OpenMRS, Keycloak, mail, inter-service URLs).
- `.env.<ServiceName>` — per-service overrides (DB URL, port, JPA settings).
- `deploy.sh` merges these into `.env.merged`, which `docker-compose.yml` consumes.
- Spring profiles: `dev` (local), `server` (deployed). Set via `SPRING_PROFILES_ACTIVE`.
- Profile-specific DB config lives in `application-dev.properties` / `application-server.properties` inside each service.

## Frontend

React 17 app in `client/` using Redux for state management, React Router v5, Carbon Design System components, and Axios for API calls. Views include Login, Register, Dashboard, Profile, Notifications, and PasswordReset.
