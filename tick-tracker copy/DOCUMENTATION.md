# Tick Tracker: how it works

This document walks through the code in the order a request travels through it. The README covers setup and the endpoint list.

## Package layout

All code lives under `com.example.ticktracker`.

| Package | Responsibility |
|---|---|
| `client` | Talks to the Elanco API (`ElancoApiClient`, `SightingDto`, `UpstreamApiException`) |
| `ingest` | Turns raw API records into stored data (`IngestionService`, `SightingValidator`, `ValidationResult`, `IngestionResult`) |
| `domain` | Database entities and queries (`TickSighting`, `RejectedRecord`, `SightingRepository`, `SightingSpecifications`) |
| `repository` | Simple Spring Data repositories used by ingestion (`TickSightingRepository`, `RejectedRecordRepository`) |
| `service` | Read-side logic: search, reports, insights |
| `web` | REST controllers, response DTOs and the global error handler |
| `config` | OpenAPI/Swagger settings |

## 1. Ingestion (`POST /api/admin/ingest`)

`IngestController` calls `IngestionService.ingest()`, which does the following.

1. **Guard.** An `AtomicBoolean` stops two ingests running at once. A second call gets `IngestionInProgressException`, which becomes HTTP 409.
2. **Download.** `ElancoApiClient.fetchAllSightings()` calls `GET /tick-sightings` with Spring's `RestClient`. The API rate limits, so a 429 makes the client wait about 61 seconds and retry. Server errors and connection problems are retried with a growing delay. After the maximum number of attempts (5) it throws `UpstreamApiException`. Other client errors, or data in an unexpected shape, fail straight away.
3. **Validate.** `SightingValidator` checks each record: id, date, location, species and Latin name must be present, lengths are limited, the date must be readable and not in the future. Whitespace is cleaned and the location is converted to title case. The result is a `ValidationResult` holding either a `TickSighting` or a rejection reason.
4. **Separate good from bad.** Valid records are collected in a map keyed by id, which removes repeats inside the download. Invalid ones become `RejectedRecord` rows holding the raw JSON and the reason.
5. **Skip what is stored.** The service looks up the ids already in the database and inserts only the new ones, so running the ingest twice is safe.
6. **Save and report.** Rejected records are replaced with the latest set, and an `IngestionResult` (fetched, inserted, duplicatesSkipped, rejected) is returned.

`GET /api/admin/rejected` returns the stored rejected records so you can see what was dropped and why.

## 2. Search (`GET /api/sightings`)

`SightingController` reads the optional `from`, `to`, `location`, `species`, `page` and `size` parameters. Page size is limited to 1-100 by validation annotations.

`SightingSearchService` builds a JPA `Specification` from four small pieces in `SightingSpecifications`. Each piece returns nothing when its filter is empty, so only the filters supplied apply. Text filters are case-insensitive "contains" matches, and special characters (`%`, `_`) are escaped. Results are sorted newest first and returned as a `PagedResponse`.

## 3. Reports (`/api/reports`)

- **`sightings-by-region`** counts sightings per location in an optional date range, sorted highest first. The database does the grouping.
- **`trends`** returns counts over time, grouped by week (starting Monday) or month, with an optional location filter. Empty periods between the first and last bucket are filled with zero so a chart has no gaps. An unknown `granularity` gives a 400 error.

## 4. Insights (`/api/insights/region-trends`)

The service compares two windows of equal length: the most recent `days` days (default 7) and the `days` before that. If no end date is given, it uses the date of the latest sighting in the data, because the dataset is historical.

For each location it works out the change and the percentage change, then assigns a status:

- **SPIKE**: at least `minSightings` in the current window and either none before or at least double the previous window
- **RISING** or **FALLING**: the count went up or down
- **STABLE**: no change

Results are sorted by biggest increase first.

## 5. Errors

`GlobalExceptionHandler` extends `ResponseEntityExceptionHandler` and returns RFC 7807 `ProblemDetail` responses with a title, detail, type and timestamp.

| Situation | Status |
|---|---|
| Bad parameter, bad date, invalid range | 400 |
| Ingest already running | 409 |
| Elanco API failed or returned an error | 502 |
| Elanco API unreachable | 503 |
| Anything unexpected | 500 (details logged, not exposed) |

## 6. Data model

`TickSighting` is keyed by the API's own id (a string), with date, location, species and Latin name. `RejectedRecord` has a generated id, the raw payload, the reason and the time it was rejected. The H2 database is stored in `./data`, and Hibernate updates the schema automatically.

## 7. Tests

Unit tests with JUnit 5 and Mockito cover the report and insight services. Controller tests use standalone MockMvc with the real `GlobalExceptionHandler` to check status codes and error bodies. Run them with `./mvnw test`.
