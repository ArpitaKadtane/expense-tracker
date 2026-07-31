# AI Notes

## AI Tools Used

I used AI tools, including ChatGPT, as a development assistant during this assignment.

AI was primarily used for:

* Understanding the Spring Boot project structure
* Planning the REST API architecture
* Generating initial implementation ideas and code drafts
* Helping troubleshoot compilation and dependency issues
* Creating test cases
* Reviewing edge cases and validation behavior
* Improving documentation

I treated the AI output as suggestions rather than blindly accepting generated code.

## AI-Generated vs. Developer-Written Work

### AI-assisted work

AI assistance was used for initial drafts and guidance for:

* Expense model structure
* Service-layer implementation approach
* REST controller structure
* Validation annotations
* Exception handling approach
* Controller test structure
* README documentation
* OpenAPI/Swagger setup

### Developer work and decisions

I personally:

* Created and configured the project in VS Code
* Set up the Java 17 / Spring Boot project
* Reviewed and integrated the generated code
* Ran the project locally
* Tested the API endpoints using Postman
* Verified request validation behavior
* Tested category filtering
* Tested overall and category totals
* Tested deletion and not-found behavior
* Ran the automated test suite using Maven
* Investigated and fixed compilation issues
* Verified that all automated tests pass
* Verified the Swagger UI manually
* Reviewed the final project structure before submission

## Validation and Testing

I did not rely solely on AI-generated code.

The implementation was validated through both manual API testing and automated tests.

### Manual testing

The following functionality was tested through Postman:

1. Creating expenses
2. Retrieving all expenses
3. Filtering expenses by category
4. Calculating overall expense totals
5. Calculating totals for a specific category
6. Deleting an expense
7. Attempting to delete a non-existent expense
8. Sending invalid expense data
9. Verifying validation error responses

Swagger UI was also opened and verified locally.

### Automated testing

The project was tested using the Maven Wrapper:

```powershell
.\mvnw.cmd clean test
```

The final test execution completed successfully:

```text
Tests run: 8
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

The controller tests verify the API behavior through Spring MVC's `MockMvc`.

## AI Suggestions That Were Changed or Not Used

### Test dependency and Spring Boot version

Some initial testing guidance assumed the traditional Spring Boot testing dependency/package structure.

The project uses Spring Boot 4.1.0, whose testing modules and package names differ from older Spring Boot versions.

I verified the Maven compilation errors and adjusted the test setup to use the Spring Boot 4-compatible testing configuration rather than adding unnecessary dependencies.

### Testcontainers

Testcontainers was initially considered/added during project setup, but the assignment does not require a database or external container.

I removed the unnecessary Testcontainers dependencies because the application uses in-memory storage and does not need containerized infrastructure.

### Database

A database was not added.

The assignment explicitly permits in-memory storage or a local JSON file and states that no database is required.

I chose in-memory storage to keep the implementation simple, focused, and within the expected scope of the four-hour assignment.

### Additional Features

I intentionally did not add multiple optional features.

The assignment states that at most one optional bonus should be selected.

I selected OpenAPI/Swagger documentation because it provides useful API documentation without adding unnecessary application complexity.

## Engineering Reasoning

The implementation uses a simple layered structure:

```text
Controller
    ↓
Service
    ↓
In-memory storage
```

The controller is responsible for HTTP requests and responses.

The service contains the expense-related business logic.

`ConcurrentHashMap` is used for in-memory storage, and `AtomicLong` is used for generating expense IDs.

`BigDecimal` is used for monetary values to avoid using floating-point arithmetic for financial amounts.

Jakarta Bean Validation is used to reject invalid requests before they reach the business logic.

## Final Verification

Before submission, I verified that:

* The application starts successfully.
* Required REST endpoints work.
* Invalid requests are rejected correctly.
* Postman testing succeeds.
* Swagger UI opens successfully.
* Automated tests pass.
* The project can be built using the Maven Wrapper.
