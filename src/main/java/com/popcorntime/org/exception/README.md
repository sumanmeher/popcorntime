# ⚠️ Global Exception Handling

This package is responsible for catching and handling errors gracefully across the entire application.

## 📌 Key Concepts

1. **`@ControllerAdvice` Pattern**: Instead of wrapping every single controller method in a `try/catch` block, Spring Boot allows us to use `@ControllerAdvice`. This creates a global interceptor. If any controller throws an exception, it is caught here.
2. **Custom Exceptions**: We define specific domain exceptions (e.g., `SeatAlreadyBookedException`, `UserNotFoundException`). This makes the business logic in the Service layer much cleaner.
3. **Standardized API Responses**: When an error occurs, the global handler ensures that the client ALWAYS receives a predictable, structured JSON error response (containing the timestamp, error message, and HTTP status code), rather than a messy HTML stack trace.

## 📂 Key Classes

- `GlobalExceptionHandler`: The centralized class that intercepts exceptions and formats the HTTP response.
- Specific Custom Exception classes extending `RuntimeException`.
