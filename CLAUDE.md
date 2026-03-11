# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What is Lighter

Lighter is an open-source web UI and REST API for running Apache Spark batch jobs and interactive sessions on Kubernetes or Apache Hadoop YARN (inspired by Apache Livy).

## Commands

### Backend (server/)

```bash
cd server
./gradlew build              # Build JAR and run tests
./gradlew build -x test      # Build without tests
./gradlew test               # Run tests only
./gradlew test --tests "com.exacaster.lighter.SomeTest"  # Run single test
./gradlew build -PSPARK_VERSION=3.5.8  # Build with specific Spark version
```

### Frontend (frontend/)

```bash
cd frontend
yarn install    # Install dependencies
yarn start      # Dev server on port 3000 (proxies /lighter/api to localhost:8080)
yarn build      # Production build → dist/
yarn lint       # ESLint
yarn format     # Prettier
```

### Docker

```bash
docker build -t lighter .    # Multi-stage build from project root
cd dev && docker-compose up  # Local dev environment
```

## Architecture

### Backend (Java 17 / Micronaut 4)

Entry point: `server/src/main/java/com/exacaster/lighter/Application.java`

- **`rest/`** — REST controllers for batches, sessions, and configuration
- **`application/`** — Core domain models (`Application`, `SubmitParams`) and business logic
  - `batch/` — Batch job service: submit → track → sync state
  - `sessions/` — Interactive PySpark sessions via Py4J Gateway (port 25333)
- **`backend/`** — Pluggable cluster backends
  - `kubernetes/` — Fabric8 Kubernetes client (default)
  - `yarn/` — Apache Hadoop YARN client
  - `local/` — Local execution for testing
- **`storage/`** — JDBI-based persistence with Flyway migrations (`resources/db/migration/V1–V8`)
- **`concurrency/`** — Scheduled tasks with ShedLock for distributed locking

Configuration: `resources/application.yml` (main), `application-local.yml` (local overrides)

### Frontend (React 19 / TypeScript / Vite)

- **`client/client.ts`** — Axios HTTP client wrapping all REST endpoints
- **`pages/`** — Batches and Sessions pages
- **`components/`** — Reusable UI: AppStatus, AppLogs, Statements, etc.
- **`hooks/`** — TanStack Query hooks for server state
- Chakra UI 3 for components, React Router 7 for routing

### API

Base path: `/lighter/api`

- `GET|POST /batches`, `GET|DELETE /batches/{id}`, `GET /batches/{id}/log`
- `GET|POST|DELETE /sessions`, `GET /sessions/{id}/log`
- `POST|GET /sessions/{id}/statements`, `POST /sessions/{id}/statements/{id}/cancel`
- `GET /configuration`

### Database

Default: H2 in-memory (PostgreSQL in production). Configured via environment variables:
- `LIGHTER_STORAGE_JDBC_URL`, `LIGHTER_STORAGE_JDBC_USERNAME`, `LIGHTER_STORAGE_JDBC_PASSWORD`, `LIGHTER_STORAGE_JDBC_DRIVER_CLASS_NAME`

### Docker / Kubernetes

Three-stage Dockerfile: build server (JDK 17) → build frontend (Node LTS) → runtime (JRE 17). Exposes ports 8080 (HTTP) and 25333 (Py4J). Kubernetes pod templates for Spark driver/executor are in `k8s/`.