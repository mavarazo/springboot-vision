# ADR 0002: Standardized Exception Handling and Error Responses

## Status
Accepted

## Context
In complex enterprise applications, error handling is often inconsistent, making it difficult for both clients and operations teams to distinguish between expected business logic failures and unexpected technical system failures. Without a clear separation, monitoring systems often produce "noisy" alerts for non-critical business events, while critical technical failures might be overlooked.

Additionally, internal error representations often leak implementation details or provide inconsistent structures to API consumers, complicating client-side error handling.

## Decision
We will implement a standardized exception handling strategy based on the following principles:

1.  **Clear Categorization**: All custom exceptions must extend either `BusinessException` or `TechnicalException`.
    -   **BusinessException**: Represents a violation of business rules (e.g., "Invalid input", "Insufficient balance"). These are expected behaviors of the business domain.
    -   **TechnicalException**: Represents a failure in the infrastructure or underlying system (e.g., "Database timeout", "Downstream service unavailable").
2.  **Standardized Response Format**: All API errors will be returned using the **Problem Details for HTTP APIs** (RFC 9457 / Spring `ProblemDetail`). This ensures a consistent structure (`type`, `title`, `status`, `detail`, `instance`) across all services.
3.  **Selective Alerting**:
    -   All **TechnicalException** instances (and unhandled runtime exceptions) must trigger an alert, as they indicate a system failure.
    -   **BusinessException** instances do **not** trigger alerts by default.
    -   The **`Alertable`** marker interface will be used to flag specific `BusinessException` types that require immediate developer or operational attention (e.g., potential fraud detection).
4.  **Unified Error Mapping**: A global exception handler (e.g., `@RestControllerAdvice`) which extends `ResponseEntityExceptionHandler` will be responsible for mapping these exceptions to the standard `ProblemDetail` format.

---

## Rationale
1.  **Operational Efficiency**: By separating business and technical errors, we reduce alert fatigue for the development team. Alerts are only triggered for actionable system failures.
2.  **Interoperability**: Using the RFC 9457 standard (`ProblemDetail`) allows clients to use standard libraries for error handling and provides a predictable interface for API consumers.
3.  **Security**: Standardizing error responses prevents the accidental leakage of stack traces or internal implementation details to external users.
4.  **Flexibility**: The `Alertable` interface provides a clean way to "elevate" specific business scenarios to critical alerts without polluting the base `BusinessException` hierarchy.

---

## Consequences
- **Positive**: Consistent error reporting across the platform, improved monitoring signal-to-noise ratio, and better developer experience when debugging.
- **Negative**: Requires discipline from developers to correctly categorize and implement new exceptions within the established hierarchy.
