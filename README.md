# Spring Boot Vision: Modern Architectural Blueprint

**Spring Boot Vision** is a reference architecture for modern, production-ready backend applications. It demonstrates a
clean, multi-module structure leveraging the latest features of **Spring Boot 4.0.2** and **Java 25**, with a primary
focus on explicit exception handling and native observability.

---

## 🏗 Architectural Goals

The goal of this project is to showcase a robust and scalable architecture that adheres to the following principles:

### 1. Explicit Exception Handling (RFC 7807)

A clear distinction between business-related errors and technical system failures, using the standardized *
*ProblemDetail** format for consistent API responses.

- **Business Exceptions**: Represent functional errors (e.g., `FraudException`, `UnprocessableContentException`). These
  are typically handled with `4xx` HTTP status codes and can be marked as `Alertable` if they require immediate
  attention.
- **Technical Exceptions**: Represent infrastructure or system-level failures (e.g., database connection issues,
  external service timeouts). These are mapped to `5xx` HTTP status codes.
- **ProblemDetail (RFC 7807)**: Every error response includes a standardized JSON body containing:
  - Status code and title.
  - Application-specific `error_code`.
  - `trace.id` and `correlation.id` for cross-system debugging.
  - Dynamic `parameters` for contextual error information.

### 2. Native Observability with Micrometer Observation

Built-in tracing and monitoring using the **Micrometer Observation API**, ensuring that observability is a first-class
citizen rather than an afterthought.

- **Unified Observation**: A single API to handle both metrics and tracing.
- **Context Propagation**: Automatic propagation of trace contexts across different protocols:
  - **HTTP**: Standard REST endpoint observation.
  - **JMS (Artemis)**: Tracing for asynchronous message production and consumption.
  - **Kafka**: Observation-enabled event-driven communication.
- **Custom Metadata**: Automated extraction and propagation of `Correlation-ID`, `Client-ID`, and `Auth` metadata
  through custom filters and MDC bridging.

---

## 📂 Project Structure

The project follows a modular design to promote reusability and separation of concerns:

- **`app`**: The core application module containing business domains (Accident, Rental, Vehicle). It implements the
  actual business logic and API controllers.
- **`shared-exception`**: A reusable library defining the base exception hierarchy (`AbstractException`,
  `BusinessException`, `TechnicalException`) and the `Alertable` contract.
- **`shared-tracing`**: Infrastructure module that configures Micrometer Observation, MDC handlers, and context filters
  for unified tracing across the system.

---

## 🛠 Technology Stack

- **Runtime**: Java 25
- **Framework**: Spring Boot 4.0.2
- **Observability**: Micrometer Observation, Brave (Tracing), SLF4J (MDC)
- **Messaging**: Apache ActiveMQ Artemis (JMS), Kafka
- **Build Tool**: Gradle (Kotlin DSL)

---

Developed as a showcase for modern observability and architectural patterns in Spring Boot.
