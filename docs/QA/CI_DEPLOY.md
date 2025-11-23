# CI & Deployment (QA guide)

This document explains how CI is configured, how it prevents deployment when tests fail, and how to run tests locally.

## What we added
- GitHub Actions workflows:
  - `.github/workflows/ci.yml` — runs frontend and backend tests on pull requests and on pushes to `develop`.
  - `.github/workflows/deploy.yml` — runs frontend and backend tests on push to `main`; deployment only runs when tests pass.

These workflows run the frontend Vitest suite and backend Maven tests. The `deploy.yml` uses a two-job flow where `deploy` depends on `tests` (`needs: tests`). If `tests` fails, `deploy` will not run.

## Branch protection (recommended settings)
1. Go to the repository settings → Branches → Add rule for `main`.
2. Select:
   - Require status checks to pass before merging.
   - Select the CI workflow check(s) (they appear after the workflow has run at least once). For example: `CI / Frontend tests`, `CI / Backend tests`.
   - Optionally enable "Require branches to be up to date before merging" to ensure merges include latest passing tests.
3. Repeat for `develop` if you want PRs targeting `develop` to pass checks before merging.

With these rules, merges to `main` won't be allowed unless the CI checks pass. Additionally, the `deploy.yml` workflow runs tests again on push to `main` and blocks the deploy job if tests fail.

## How to run tests locally
### Frontend (Vitest / React Testing Library)
From repository root:

```bash
cd frontend
npm ci
npm run test        # run tests once
npm run test:ui     # interactive UI runner
```

Notes:
- If you see failures due to localization or text differences, tests query DOM text/labels — update tests or add `data-testid` attributes to components for more stable selectors.

### Backend (Maven / JUnit / Spring Boot)
From repository root:

```bash
cd backend
# If you have system Maven installed
mvn -B test

# OR if you prefer wrapper and the wrapper is present:
./mvnw test
```

If `./mvnw` fails with `.mvn/wrapper/maven-wrapper.properties: No such file or directory`, either run system `mvn` or regenerate the wrapper:

```bash
# requires system maven
mvn -N io.takari:maven:wrapper:wrapper
# then commit .mvn and mvnw into the repo
```

Troubleshooting common backend test issues:
- "Username already taken" failures: tests that create users should use unique usernames (e.g., include `System.nanoTime()` or `UUID`). Alternatively, configure tests to use an in-memory DB (H2) or mark them `@Transactional` so changes roll back after each test.
- Mockito inline-agent warnings: add `org.mockito:mockito-inline` test dependency to avoid dynamic agent self-attachment on newer JDKs.

## How the CI gate prevents deployment
- The `deploy.yml` workflow runs a `tests` job that executes frontend and backend tests.
- The `deploy` job has `needs: tests`, so GitHub Actions will not start `deploy` if `tests` failed.
- Additionally, use branch protection to prevent merging to `main` if CI checks are failing; this prevents triggering deploys from broken code.

## Further QA recommendations
- Add `data-testid` attributes to any elements that tests rely on (forms, buttons) for stability.
- Run backend tests in an isolated environment: add a `test` Spring profile using H2 and activate it during CI (or use Testcontainers with a real Postgres image).
- Add end-to-end tests (Cypress / Playwright) for critical user flows if you want higher confidence before deploy.

### Recent updates (backend tests)

- The repository now includes controller slice tests for the backend which run without a real database (uses Spring's @WebMvcTest + mocked services/repositories). These cover critical controller behaviour for:
  - EventsController: create, join, leave flows (controller responses and error mappings).
  - ConfigurarPerfilController: validation for empty sports, successful save, and not-found handling.
  - AuthController: controller slice tests that mock AuthService to avoid DB/state dependencies.

These WebMvcTest slices are faster and deterministic and are executed by the current CI workflows. If you prefer full integration tests (real DB), consider one of:

- Add a `test` Spring profile with an H2 in-memory datasource (application-test.properties) and run integration tests under that profile in CI.
- Or use Testcontainers in the integration test suite to run a Postgres container during CI.

Recommendation: keep fast controller slices for PR gating and add a small set of integration tests (Testcontainers) for a nightly build or pre-release pipeline.

## How to inspect CI runs
- Open the repository on GitHub → Actions tab → select a workflow run.
- Click a job (e.g., `frontend`) to see logs. Expand steps to see `npm run test` or `mvn test` outputs.

---

