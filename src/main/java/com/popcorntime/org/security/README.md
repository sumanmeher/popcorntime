# 🔐 Security (JWT & Authentication)

This package configures Spring Security to protect the API endpoints and manage user authentication.

## 📌 Key Concepts

1. **Stateless Authentication (JWT)**: We do not use server-side sessions. Instead, upon successful login, the server generates a JSON Web Token (JWT) cryptographically signed with a secret key.
2. **The Filter Chain**: Spring Security operates as a chain of filters intercepting HTTP requests.
   - We implemented a custom `JwtAuthenticationFilter`.
   - For every incoming request, this filter intercepts it, extracts the `Authorization: Bearer <token>` header, validates the signature, and sets the Security Context.
3. **Password Hashing**: User passwords are NEVER stored in plain text. We use `BCryptPasswordEncoder` to hash passwords securely before saving them to the database.

## 📂 Key Classes

- `SecurityConfig`: Configures which endpoints are public (like login/register) and which require authentication (like booking a ticket).
- `JwtService`: Handles the generation and cryptographic validation of JWTs.
- `JwtAuthenticationFilter`: The OncePerRequestFilter that intercepts API calls to verify identity.
