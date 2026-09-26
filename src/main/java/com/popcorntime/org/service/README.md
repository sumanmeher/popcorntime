# ⚙️ Service Layer (Business Logic)

This package contains the absolute core of the application: the **Business Logic**.

## 📌 Key Concepts

1. **The Brain of the App**: Controllers pass data here, and Repositories are called from here. The Service layer is where the actual "work" gets done (calculating prices, validating seat availability, initiating payments).
2. **Transaction Management (`@Transactional`)**: Many operations require modifying multiple database tables. (e.g., Booking a ticket requires updating the `Seat` table AND inserting into the `Ticket` table). 
   - By annotating service methods with `@Transactional`, Spring ensures the ACID properties of the database.
   - If anything fails during the process, the entire transaction is rolled back, preventing partial or corrupted data.
3. **Dependency Injection**: Services depend on Repositories and other Services. Spring Boot's IoC (Inversion of Control) container automatically injects these dependencies via constructor injection.

## 📂 Key Classes

- `UserService`: Logic for registering and authenticating users.
- `MovieService` & `TheatreService`: Logic for catalog browsing.
- `SeatService`: The most complex service, handling the critical section of acquiring Redis locks, validating availability, and executing the checkout flow.
