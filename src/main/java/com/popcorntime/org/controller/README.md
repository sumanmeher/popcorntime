# 🎮 Controller Layer

This package contains the **REST Controllers** for the PopcornTime application. 

The Controller layer is the outermost boundary of the application. It acts as the entry point for all incoming HTTP requests from clients (browsers, mobile apps, Postman, etc.).

## 📌 Key Concepts

1. **RESTful Routing**: Controllers use annotations like `@RestController`, `@RequestMapping`, `@GetMapping`, and `@PostMapping` to map URL endpoints to specific Java methods.
2. **Separation of Concerns**: Controllers **do not** contain business logic. Their sole responsibility is to:
   - Accept the HTTP Request (and map JSON to DTOs).
   - Validate incoming data (using `@Valid`).
   - Call the appropriate method in the `Service` layer.
   - Return an HTTP Response (usually wrapped in a `ResponseEntity` with the correct HTTP Status Code).

## 📂 Key Classes

- `UserController`: Handles user registration and authentication endpoints.
- `MovieController`: Handles fetching movie and show details.
- `SeatController`: Handles the concurrent booking and checkout process.
