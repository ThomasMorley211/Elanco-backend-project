# Tick Tracker by Thomas Morley (@ThomasMorley211@outlook.com)

Backend for the Elanco Placement Program technical task. It pulls UK tick sighting data from the Elanco API, validates and stores it, and exposes REST endpoints to search sightings, view reports and spot trends.

Author: Thomas Morley


Claude AI on sonnet 5.5 was used for fixing errors towards the end of the process and troubleshooting with swaggerUI and maven
## Outline

- Downloads about 1000 sightings from `https://dev-task.elancoapps.com/data/tick-sightings`.
- Validates each record (missing or unreadable fields, future dates, over-long values). Rejected records are stored with the reason and can be inspected.
- Stores valid sightings in an H2 database. Re-running the ingest never creates duplicates.
- Provides search, reporting and insight endpoints with Swagger documentation.

The dataset only contains cities, so "region" in the reports means the sighting's location.

## Endpoints

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/admin/ingest` | Download, validate and store sightings. Returns fetched / inserted / duplicatesSkipped / rejected counts |
| GET | `/api/admin/rejected` | Records that failed validation, with the reason |
| GET | `/api/sightings` | Search by `from`, `to`, `location`, `species`; paged with `page` and `size` |
| GET | `/api/reports/sightings-by-region` | Sighting counts per location, optional `from` and `to` |
| GET | `/api/reports/trends` | Sightings over time. `granularity=weekly\|monthly`, optional `from`, `to`, `region` |
| GET | `/api/insights/region-trends` | Locations with rising activity. `days` (1-90, default 7) and `minSightings` (default 5) |

Dates use ISO format, for example `2025-06-30`. Errors are returned as RFC 7807 problem responses.

Swagger UI: `http://localhost:8080/swagger-ui.html`

## Running it

Requires Java 21.

```
./mvnw spring-boot:run
```

On Windows PowerShell use `.\mvnw.cmd spring-boot:run`.

Then call `POST /api/admin/ingest` (easiest from Swagger UI) to load the data. The database is a file in `./data`, so the data survives restarts.

With Docker:

```
docker compose up --build
```

Run the tests with `./mvnw test`.

## Architecture choices

- **Spring Boot 3 and Java 21** with the layers split into `client`, `ingest`, `domain`, `repository`, `service` and `web`.
- **RestClient with retries** in `ElancoApiClient`. The API rate limits (HTTP 429) and sometimes returns 500s, so the client waits and retries, then fails with a clear upstream error.
- **Validation before storage.** Bad records go to a separate table instead of being silently dropped. Locations are tidied into title case so "reading" and "Reading" group together.
- **Idempotent ingest.** Records are keyed on the API's id, so running the ingest again skips what is already stored. A guard prevents two ingests running at once (HTTP 409).
- **Spring Data JPA with Specifications** for the optional search filters.
- **H2 file database** so the project runs with no setup. It can be swapped for Postgres by changing the datasource settings.
- **Central error handling** with `@RestControllerAdvice` returning `ProblemDetail`.
- **Tests** use JUnit 5, Mockito and standalone MockMvc.

## What I would do with more time

- Run the ingest on a schedule and on startup, and fetch only new records.
- Move to PostgreSQL with Flyway migrations.
- Add integration tests against the full application with a stubbed Elanco API.
- Add authentication to the admin endpoints.
- Add caching for the report endpoints.
- Add a map-based region grouping (county level) using the city names.
