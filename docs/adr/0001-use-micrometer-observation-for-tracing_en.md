# ADR 0001: Standardizing Observability with Micrometer Observation

## Status
Accepted

## Context
Modern enterprise applications, especially those built on microservices or distributed architectures, require high visibility into their runtime behavior. Traditional observability often separates metrics, logging, and tracing into different APIs and libraries, leading to inconsistent metadata and increased complexity in maintenance.

In the Spring ecosystem (Spring Boot 3.x/4.x), **Micrometer Observation** was introduced as a unified API to handle both metrics and tracing. This replaces the legacy Spring Cloud Sleuth and provides a more integrated way to instrument applications.

---

## Decision
We will use **Micrometer Observation** with tracing (via Brave or OpenTelemetry) as the primary observability standard for the application. All inter-service communication (HTTP, JMS, Kafka) and critical business logic will be instrumented using the Observation API.

---

## Rationale
1.  **Unified API**: Observation allows us to instrument code once and produce both metrics and traces simultaneously.
2.  **Native Integration**: It is the default observability tool for Spring Boot 3+ and 4+, ensuring long-term support and seamless integration with Spring's core components (RestTemplate, WebClient, Kafka, etc.).
3.  **Context Propagation**: It handles the complexity of propagating trace contexts across different protocols (HTTP, JMS, Kafka) out-of-the-box.
4.  **Vendor Neutrality**: Micrometer provides a bridge to various backends (Zipkin, Jaeger, Prometheus, OTLP), preventing vendor lock-in.
5.  **MDC Enrichment**: It enables automatic synchronization of trace IDs into the SLF4J MDC, ensuring that logs are always correlated with traces.

---

## Consequences
- **Positive**: Consistent observability across all modules, improved debugging in distributed environments, and reduced boilerplate code for tracing.
- **Negative**: Slight learning curve for developers to understand the Observation API versus legacy tracing libraries.
