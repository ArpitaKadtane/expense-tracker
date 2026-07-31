# Smart Expense Tracker API

The API supports creating, viewing, filtering, summarizing, and deleting expenses. Data is stored in memory, so no external database is required.

## Tech Stack

* Java 17
* Spring Boot 4.1.0
* Spring MVC
* Maven Wrapper
* Jakarta Bean Validation
* JUnit
* Spring MVC Test / MockMvc
* OpenAPI / Swagger UI
* In-memory storage using `ConcurrentHashMap`

## Features

* Create an expense
* View all expenses
* Filter expenses by category
* Calculate total expenses
* Calculate total expenses for a specific category
* Delete an expense
* Input validation
* Consistent validation error responses
* Automated controller tests
* Interactive OpenAPI / Swagger documentation

## Project Structure

```text
expense-tracker/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/diligent/expense/
│   │   │       ├── controller/
│   │   │       ├── exception/
│   │   │       ├── model/
│   │   │       ├── service/
│   │   │       └── ExpenseTrackerApplication.java
│   │   └── resources/
│   │
│   └── test/
│       └── java/
│           └── com/diligent/expense/
│               ├── ExpenseControllerTest.java
│               
│
├── pom.xml
├── mvnw
├── mvnw.cmd
├── README.md
└── AI_NOTES.md
```

## Prerequisites

* Java 17 or later
* No separate Maven installation is required because the project includes the Maven Wrapper.

Verify Java:

```powershell
java -version
```

## Installation

Clone the repository and enter the project directory.

Windows:

```powershell
.\mvnw.cmd clean install
```

The Maven Wrapper downloads the required Maven dependencies automatically.

## Run the Application

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

The server starts on:

```text
http://localhost:8080
```

## Run Tests

Windows:

```powershell
.\mvnw.cmd clean test
```

The test suite covers:

* Creating an expense
* Retrieving all expenses
* Filtering by category
* Calculating overall totals
* Calculating category totals
* Deleting an expense
* Handling deletion of a non-existent expense
* Rejecting invalid expense data

## API Endpoints

### Create Expense

```http
POST /api/expenses
```

Example request:

```json
{
  "title": "Grocery Shopping",
  "amount": 1250.50,
  "category": "Food",
  "date": "2026-07-31"
}
```

Returns `201 Created`.

---

### Get All Expenses

```http
GET /api/expenses
```

Returns `200 OK`.

---

### Filter Expenses by Category

```http
GET /api/expenses?category=Food
```

Returns only expenses belonging to the requested category.

---

### Get Overall Expense Summary

```http
GET /api/expenses/summary
```

Example response:

```json
{
  "totalExpenses": 2100.50
}
```

---

### Get Category Expense Summary

```http
GET /api/expenses/summary?category=Food
```

Example response:

```json
{
  "category": "Food",
  "totalExpenses": 1750.50
}
```

---

### Delete Expense

```http
DELETE /api/expenses/{id}
```

Returns `204 No Content` when the expense is deleted.

Returns `404 Not Found` when the requested expense does not exist.

## Validation

The API validates incoming expense data.

The following fields are required:

* `title`
* `amount`
* `category`
* `date`

The amount must be greater than zero.

Invalid requests return:

```text
400 Bad Request
```

with a structured validation error response.

## API Documentation

Swagger UI is available while the application is running:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI specification:

```text
http://localhost:8080/v3/api-docs
```

Swagger UI can be used to explore and execute the API endpoints interactively.

## Data Storage

The assignment does not require a database, so expenses are stored in memory using a thread-safe `ConcurrentHashMap`.

Because storage is in memory, all data is cleared when the application restarts.

## Testing

The project uses JUnit and Spring MVC Test with `MockMvc` for API-level controller testing.

The complete test suite currently passes successfully.

```text
Tests run: 8
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

## Optional Bonus

The project implements the **OpenAPI/Swagger documentation** bonus.

## AI Usage

AI tools were used during development as a coding and learning assistant. Details about generated code, validation performed, modifications made, and rejected suggestions are documented in `AI_NOTES.md`.
