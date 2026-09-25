# 💳 Payment (Strategy Pattern)

This package manages integrations with third-party payment gateways (like Razorpay, Stripe, GPay).

## 📌 Key Concepts

1. **Strategy Design Pattern**: Payment gateways change frequently. To prevent rewriting core checkout logic every time we switch gateways, we use the Strategy Pattern (a core SOLID principle: Open/Closed).
2. **How it works**:
   - `PaymentProviderService` is an Interface defining a common contract (`createPayment()`, `verifyPayment()`).
   - `RazorpayProvider` and `StripeProvider` implement this interface.
   - The application dynamically injects the correct implementation based on properties, meaning we can switch payment gateways without modifying a single line of the main checkout logic!
3. **Idempotency**: Payment processing requires strict idempotency. We use unique UUIDs as `Idempotency-Keys` when communicating with Razorpay to ensure that if a network timeout occurs and we retry the request, the user is NOT charged twice.

## 📂 Key Classes

- `PaymentProviderService`: The Strategy interface.
- `RazorpayPaymentProvider`: The concrete implementation for Razorpay APIs.
