# 📨 Messaging (RabbitMQ)

This package handles **Asynchronous Messaging** using RabbitMQ to decouple heavy background tasks from the main thread.

## 📌 Key Concepts

1. **Decoupling for Performance**: Sending an email or generating a PDF receipt can take 2-3 seconds. If we do this synchronously during the checkout flow, the user has to wait. By pushing a "Send Email" message to RabbitMQ, the API can return an instant "Success" response to the user, while the email is sent in the background.
2. **Producers and Consumers**: 
   - The **Producer** (in the Service layer) publishes a message to a RabbitMQ Exchange.
   - The **Consumer** (in this package) constantly listens to a specific Queue and processes messages as they arrive.
3. **Eventual Consistency**: RabbitMQ is also used as a fallback mechanism for Distributed Transactions. If a database save fails after a payment succeeds, the payload is pushed to a Dead Letter/Retry Queue to be processed later, guaranteeing data consistency.

## 📂 Key Classes

- `RabbitMQConfig`: Configures the Exchanges, Queues, and Binding keys.
- `EmailWorker` / `FallbackWorker`: The `@RabbitListener` components that consume messages and execute the background tasks.
