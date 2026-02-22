# Vision

**vision** is a Proof of Concept (PoC) demonstrating advanced distributed tracing and structured logging with **Spring Boot 4.0.2** and **Java 25**. This project showcases how to seamlessly propagate trace contexts across different communication protocols, including **HTTP**, **JMS**, and **Kafka**, using **Micrometer Tracing** and **Brave**.

## 🚀 Features

- **Spring Boot 4.0.2 & Java 25**: Leverages the latest Spring Framework features and Java's modern capabilities.
- **Unified Distributed Tracing**:
  - **HTTP**: Standard observation for REST endpoints.
  - **JMS**: Tracing for message production and consumption via Spring JMS (Artemis).
  - **Kafka**: Observation-enabled Kafka production and consumption.
- **Custom Context Propagation**:
  - **Context Filters**: Extracts custom headers (Auth, Client, Correlation ID) from HTTP requests and attaches them to the Micrometer Observation context.
  - **MDC Bridge**: Automatically synchronizes Micrometer Observation tags into the SLF4J MDC for consistent logging across the entire request lifecycle.
- **Structured Logging**: Configured with Logstash-formatted console logging for better observability and log analysis in ELK/Grafana Loki stacks.
- **Embedded JMS**: Uses an embedded Apache ActiveMQ Artemis server for easy demonstration.

## 🛠 Prerequisites

- **Java 25** (Oracle OpenJDK or GraalVM recommended)
- **Gradle** (included via `./gradlew`)
- **Kafka**: A local Kafka broker running at `localhost:9092` (required for Kafka features).

## 🏃 How to Run

### 1. Start Kafka (Optional)
Ensure you have a Kafka broker running if you intend to test Kafka tracing.

### 2. Run the Application
```bash
./gradlew bootRun
```
The application will start on port `8001`.

## 🧪 Testing Tracing

### HTTP to JMS Propagation
Send a POST request to create a user via JMS.
```bash
curl -X POST http://localhost:8001/v1/users/jms 
     -H "Content-Type: application/json" 
     -H "x-correlation-id: custom-corr-123" 
     -d '{"name": "John Doe", "email": "john@example.com"}'
```

Check the logs to see how the `x-correlation-id` and `trace.id` are propagated from the HTTP controller to the JMS
consumer.

### HTTP to Kafka Propagation
Send a POST request to update a user via Kafka.
```bash
curl -X POST http://localhost:8001/v1/users/kafka 
     -H "Content-Type: application/json" 
     -H "X-Client-ID: mobile-app" 
     -d '{"id": "1", "name": "Jane Doe"}'
```
Observe the Kafka consumer logs showing the same trace identifiers and client metadata.

## 🏗 Architecture Details

### Observation Configuration
The `ObservationConfig` customizes the `CurrentTraceContext` to ensure `trace.id` and `span.id` are correctly mapped to MDC keys.

### MDC Observation Handler
The `MdcObservationHandler` is a custom `ObservationHandler` that iterates through all key-values in an observation context and puts them into the MDC when a scope is opened, ensuring that all custom metadata (like correlation IDs) is present in every log line.

### Context Filters
- `AuthContextFilter`: Extracts `Authorization` header metadata.
- `ClientContextFilter`: Extracts `X-Client-ID` and `X-Client-Version`.
- `CorrelationIdFilter`: Extracts `x-correlation-id` or generates a new one.

These filters interact with `ServerHttpObservationFilter` to enrich the observation context before it propagates further.

## 📊 Monitoring
Actuator endpoints are enabled at `/actuator`:
- `health`: `http://localhost:8001/actuator/health`
- `tracing`: `http://localhost:8001/actuator/tracing` (Displays current trace propagation details)

---
Developed as a PoC for modern observability patterns in Spring Boot applications.
