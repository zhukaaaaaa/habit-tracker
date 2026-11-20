# Habit Tracker

A habit-tracking API built with Java 21 and Spring Boot. It stores habits and daily completions in PostgreSQL, tracks streaks, and uses JWT authentication. Liquibase manages the database schema.

## Run with Docker

Start with an empty PostgreSQL database.

Copy `.env.example` to `.env` in the project root. Set a random `DB_PASSWORD` and use `openssl rand -base64 32` to generate `JWT_SECRET`. On Windows, `.\scripts\create-env.ps1` can generate both values and create `.env`.

```sh
docker compose up --build -d
```

Open [Swagger UI](http://localhost:8080/swagger-ui/index.html). Run `docker compose down` to stop the containers and keep the data in the PostgreSQL volume.

Set `APP_TZ` in `.env` to choose the time zone for daily check-ins in Docker. The default is `UTC`.

## Run locally

Start PostgreSQL and create an empty database. Use JDK 21 and set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` in `.env` to match your database. Set `JWT_SECRET` as described above. The sample URL uses `localhost:5433`.

Run `./mvnw spring-boot:run` on Linux or macOS, or `.\mvnw.cmd spring-boot:run` on Windows. Liquibase creates the tables on startup. Then open [Swagger UI](http://localhost:8080/swagger-ui/index.html).

## Authentication

In Swagger UI, send a `POST /api/auth/register` request with this body:

```json
{
  "username": "demo_user",
  "password": "demo-password-123",
  "email": "demo@example.test"
}
```

Then send a `POST /api/auth/login` request:

```json
{
  "username": "demo_user",
  "password": "demo-password-123"
}
```

Paste the returned `token` into **Authorize** in Swagger UI. For other clients, send the `Authorization: Bearer <token>` header.

## Endpoints

| Method | Path | Action |
| --- | --- | --- |
| POST | `/api/auth/register` | Register an account |
| POST | `/api/auth/login` | Get a JWT |
| POST | `/api/habits` | Create a habit |
| GET | `/api/habits` | List habits |
| GET | `/api/habits/{id}` | Get a habit |
| PUT | `/api/habits/{id}` | Update a habit |
| DELETE | `/api/habits/{id}` | Delete a habit |
| POST | `/api/habits/{id}/records` | Mark a habit complete for today |
| GET | `/api/habits/{id}/records` | List completion records |
| GET | `/api/habits/{id}/stats` | Get streaks and completion rate |
| GET | `/api/stats/daily` | Get today's summary |
| GET | `/api/stats/week` | Get the last seven days' summary |

To create or update a habit, send `name`, `description`, and `target`:

```json
{
  "name": "Read",
  "description": "20 minutes a day",
  "target": 7
}
```

## Tests

With JDK 21, run `./mvnw test` on Linux or macOS, or `.\mvnw.cmd test` on Windows. The tests use H2.

## Author

Zhuldyz
