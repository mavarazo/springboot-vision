# ADR 0004: Transactional Outbox Pattern for Reliable Event Publishing

## Status
Accepted

## Context
In our current architecture, we use Apache Kafka for event-driven communication. However, we face two critical challenges:
1.  **Dual-Write Issue**: When updating the database and publishing an event to Kafka in the same operation, we cannot guarantee atomicity. If the database update succeeds but the Kafka publish fails (or vice versa), the system enters an inconsistent state.
2.  **Connection Pool Exhaustion**: We are currently using Spring Modulith's event publication registry to persist events and handle retries. Under high load, the large number of asynchronous processes attempting to update the event registry in the database leads to connection pool saturation and database locking, effectively self-denying service.

## Decision
We will implement the **Transactional Outbox Pattern** for all outgoing events to Kafka.

1.  **Atomicity**: Instead of publishing directly to Kafka, the business service will write the event to a dedicated `outbox` table within the same database transaction as the business data update.
2.  **Separation of Concerns**: A separate, dedicated process (the "Message Relay" or "Outbox Processor") will asynchronously poll the `outbox` table and publish the events to Kafka.
3.  **Controlled Throughput**: The Outbox Processor will use a dedicated, limited connection pool and batch processing to ensure it does not overwhelm the database or starve other business processes of connections.
4.  **At-Least-Once Delivery**: Events will only be marked as processed or deleted from the outbox table after successful acknowledgment from Kafka.

---

## Rationale
1.  **Consistency**: By using the outbox table, we ensure that the event is only stored if the business transaction succeeds, solving the dual-write problem.
2.  **Scalability**: Moving the Kafka publishing logic out of the main business transaction reduces the hold time on database connections and eliminates the risk of distributed locking issues caused by high-concurrency event registration.
3.  **Resource Protection**: The Outbox Processor can be tuned independently of the business services, allowing us to throttle event publishing during peak loads to protect the database connection pool.
4.  **Operational Stability**: Replacing the heavy-weight "misuse" of Spring Modulith's internal event registry with a lean, purpose-built outbox mechanism provides more transparency and control over the event lifecycle.

---

## Consequences
- **Positive**: Guaranteed eventual consistency between database and Kafka, improved system resilience under high load, and better management of database resources.
- **Negative**: Increased complexity in the infrastructure due to the need for an outbox processor and an additional database table. There will be a slight delay (latency) between the database transaction and the event appearing in Kafka.
