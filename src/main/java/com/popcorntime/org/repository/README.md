# 💾 Repository Layer (Data Access)

This package contains the **Spring Data JPA Repositories**, acting as the bridge between the Java application and the PostgreSQL database.

## 📌 Key Concepts

1. **Abstraction**: Instead of writing raw SQL queries, Spring Data JPA allows us to create simple interfaces that extend `JpaRepository`. Spring Boot automatically implements these interfaces at runtime!
2. **Derived Queries**: We can generate complex SQL queries simply by naming methods correctly. (e.g., `findByCityNameAndMovieTitle()` will automatically generate a `SELECT ... WHERE city = ? AND movie = ?` query).
3. **Pagination & Sorting**: Repositories easily support fetching large datasets in chunks (Pages) to prevent memory overload.

## 📂 Key Classes

- `UserRepository`, `MovieRepository`, `TheatreRepository`, `TicketRepository`, etc.
