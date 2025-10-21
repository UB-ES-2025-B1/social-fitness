# Social Fitness

Monorepo for the Social Fitness application. It contains a Spring Boot backend and a React (Vite) frontend.

## Overview

- Backend: Java 17, Spring Boot (security, validation, JPA). Maven wrapper is included.
- Frontend: React + Vite, tested with Vitest and Testing Library.
- Docs: QA notes and reports under `docs/`.

## Repository structure

```
social-fitness/
├─ backend/                # Spring Boot API
│  ├─ src/main/java/...    # application sources
│  ├─ src/test/java/...    # tests
│  ├─ src/main/resources/  # configuration (application.properties)
│  └─ mvnw, pom.xml        # Maven wrapper and build
├─ frontend/               # React client (Vite)
│  ├─ src/                 # UI components and services
│  ├─ public/              # static assets
│  └─ package.json         # scripts and dependencies
└─ docs/
   └─ QA/                  # QA reports
```

## Prerequisites

- Java 17+ (for backend)
- Node.js 18+ and npm (for frontend)
- PostgreSQL 14+ (default backend database)

On macOS you can install with Homebrew, for example:

```bash
# Java, Node, and PostgreSQL (optional example)
brew install openjdk@17 node postgresql
```

> See `backend/README.md` for a Windows-oriented PostgreSQL setup and more detailed steps.

## Configuration

- Backend config: `backend/src/main/resources/application.properties`
  - Ensure the PostgreSQL URL, username, and password are correct for your local setup.
- Frontend config: `frontend/.env`
  - Set the API base URL so the UI can call the backend:

```env
# frontend/.env
VITE_API_BASE=http://localhost:8080
```

## Run locally

### 1) Start the backend (port 8080 by default)

```bash
cd backend
./mvnw spring-boot:run
```

### 2) Start the frontend (port 5173 by default)

```bash
cd frontend
npm install
npm run dev
```

Open http://localhost:5173 and ensure the frontend can reach the backend at `VITE_API_BASE`.

## Tests

- Backend:

```bash
cd backend
./mvnw test
```

- Frontend:

```bash
cd frontend
npm test
```

## Useful endpoints (backend)

- Auth: `POST /auth/login`, `POST /auth/register`
- Profile: `POST /profile`
- Events: `GET /events`, `GET /events/:id`, `POST /events/:id/join`

See `frontend/API.md` for the request/response shapes expected by the UI.

## Troubleshooting

- CORS/auth issues when using cookies: enable credentials and allow the frontend origin on the backend.
- Database connection errors: verify PostgreSQL is running and credentials in `application.properties` match your instance.

## Documentation

- QA reports: `docs/QA/`
- Frontend API contract: `frontend/API.md`

## License

If a license applies to this project, place it in `LICENSE`. Otherwise, consult your course or organization guidelines.
