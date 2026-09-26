# 📦 Data Transfer Objects (DTO)

This package contains the **Data Transfer Objects (DTOs)** used to send and receive data across the application's network boundary (the Controller layer).

## 📌 Key Concepts

1. **Decoupling from Entities**: We NEVER expose our internal Database Entities (`@Entity`) directly to the outside world. If a database schema changes, the API response shouldn't magically change and break mobile apps. DTOs act as a stable contract between the client and the server.
2. **Security**: Entities might contain sensitive data (like a User's hashed password). DTOs ensure we only serialize and send the exact data the client is allowed to see.
3. **Data Validation**: Incoming JSON requests are mapped to Request DTOs. These DTOs use Jakarta Validation annotations (like `@NotBlank`, `@Email`, `@Min`) to automatically validate data before it ever reaches the business logic.

## 📂 Key Types

- **Request DTOs**: (e.g., `LoginRequest`, `BookTicketRequest`) Used to structure data coming *in* from the client.
- **Response DTOs**: (e.g., `AuthResponse`, `TicketDetailsResponse`) Used to structure data going *out* to the client.
