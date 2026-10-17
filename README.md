# Smart Expense Tracker

A full-stack expense tracking application for creating, updating, filtering and summarizing daily expenses. Built with a Spring Boot REST API, a React frontend, and PostgreSQL persistence.

**Backend:** Java 17 · Spring Boot 4 · Spring Data JPA · PostgreSQL · Bean Validation · OpenAPI/Swagger · JUnit + MockMvc
**Frontend:** React 19 · Vite · Axios
**Infra:** Docker · Docker Compose · GitHub Actions CI

---

## Features

- Create, view, update and delete expenses (full CRUD)
- Filter expenses by category
- Paginated expense list (page + size)
- Overall and per-category total summaries
- Request validation with structured, field-level error responses
- Input validation surfaced directly in the UI
- Interactive API documentation via Swagger UI
- Automated backend test suite (12 tests)

---

## Quick Start (Docker — recommended)

The fastest way to run the full stack (database + backend + frontend):

```bash
git clone https://github.com/ArpitaKadtane/expense-tracker.git
cd expense-tracker
docker compose up --build
```

Then open:

- **Frontend:** http://localhost:5173
- **API:** http://localhost:8080/api/expenses
- **Swagger UI:** http://localhost:8080/swagger-ui.html

Stop everything:

```bash
docker compose down
```

Data is stored in a named Docker volume (`pgdata`), so it survives restarts. To also delete the data, run `docker compose down -v`.

---

## Quick Start (Local, without Docker)

### Prerequisites

- Java 17+
- Node.js 18+
- A running PostgreSQL instance (or run only the database with `docker compose up -d db`)

### 1. Backend

Configure the database connection via environment variables (defaults shown in `.env.example`):

```bash
DB_URL=jdbc:postgresql://localhost:5432/expense_tracker
DB_USER=expense
DB_PASSWORD=expense
```

Then start the API:

**Windows**
```powershell
.\mvnw.cmd spring-boot:run
```

**macOS/Linux**
```bash
./mvnw spring-boot:run
```

The API runs at `http://localhost:8080`.

### 2. Frontend

In a second terminal:

```bash
cd frontend
npm install
npm run dev
```

The frontend runs at `http://localhost:5173`. The Vite dev server proxies `/api` requests to `http://localhost:8080`, so no CORS configuration is needed locally.

---

## API Endpoints

| Method | Path | Description |
| --- | --- | --- |
| `POST` | `/api/expenses` | Create an expense |
| `GET` | `/api/expenses?page=0&size=10&category=Food` | List expenses (paginated, optional category filter) |
| `GET` | `/api/expenses/categories` | List distinct categories |
| `GET` | `/api/expenses/summary?category=Food` | Total expenses (overall or by category) |
| `PUT` | `/api/expenses/{id}` | Update an expense |
| `DELETE` | `/api/expenses/{id}` | Delete an expense |

### Example request

```http
POST /api/expenses
Content-Type: application/json

{
  "title": "Grocery Shopping",
  "amount": 1250.50,
  "category": "Food",
  "date": "2026-07-31"
}
```

### Paginated response

```json
{
  "content": [ { "id": 1, "title": "Grocery Shopping", "amount": 1250.50, "category": "Food", "date": "2026-07-31" } ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

---

## Testing

The test suite uses an in-memory H2 database, so no PostgreSQL instance is required:

**Windows**
```powershell
.\mvnw.cmd test
```

**macOS/Linux**
```bash
./mvnw test
```

```
Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
```

---

## Continuous Integration

Every push and pull request to `main` runs the GitHub Actions workflow in
[`.github/workflows/ci.yml`](.github/workflows/ci.yml), which builds and tests the
backend and builds the frontend.

---

## Configuration

The backend reads configuration from environment variables (with sensible local defaults):

| Variable | Default | Description |
| --- | --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/expense_tracker` | PostgreSQL JDBC URL |
| `DB_USER` | `expense` | Database user |
| `DB_PASSWORD` | `expense` | Database password |
| `PORT` | `8080` | HTTP port |
| `CORS_ALLOWED_ORIGINS` | `*` | Comma-separated allowed frontend origins |
