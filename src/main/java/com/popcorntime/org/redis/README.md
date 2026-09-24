# 🔴 Redis (Caching & Distributed Locks)

This package contains the configuration and logic for integrating Redis, an incredibly fast, in-memory data store.

## 📌 Key Concepts

1. **Distributed Locks for Concurrency**: In a movie ticketing app, thousands of users might try to book the exact same seat at the exact same millisecond. Traditional PostgreSQL database locks are too slow and can cause deadlocks under high load.
2. **The `SETNX` Command**: We use Redis `SETNX` (Set if Not eXists). When a user selects a seat, we try to write a key (`showId-seatId`) to Redis. Because Redis is single-threaded, it guarantees absolute atomicity. Only one user will successfully write the key (acquiring the lock).
3. **Time-To-Live (TTL)**: The lock is given a TTL (e.g., 6 minutes). This guarantees that if the user closes their browser or their payment fails, the lock automatically expires and the seat is released back to the public without any manual intervention.

## 📂 Key Classes

- `RedisConfig`: Configures the connection factory and `RedisTemplate`.
- `RedisLockService`: The utility class that handles acquiring and releasing distributed locks.
