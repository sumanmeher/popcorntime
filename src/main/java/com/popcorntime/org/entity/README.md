# 🗄️ Entity Layer (Domain Models)

This package contains the **JPA Entities** that represent the core domain models and map directly to tables in the PostgreSQL database.

## 📌 Key Concepts

1. **Object-Relational Mapping (ORM)**: Using Hibernate (via Spring Data JPA), these Java classes are automatically mapped to SQL tables. Annotations like `@Entity`, `@Table`, and `@Id` define how the mapping occurs.
2. **Relationships**: Real-world data is deeply connected. We model the cinema hierarchy using relational annotations:
   - `@ManyToOne`: e.g., A `Screen` belongs to a single `Theatre`.
   - `@OneToMany`: e.g., A `Theatre` has multiple `Screen`s.
3. **Encapsulation**: Entities represent the absolute source of truth for the application's data state.

## 📂 Core Hierarchy

- `City` -> `Theatre` -> `Screen` -> `Seat`
- `Movie` -> `MovieShow`
- `AppUser` -> `Ticket`
