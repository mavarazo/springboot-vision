# ADR 0001: Standardizing Observability with Micrometer Observation

## Status

Accepted

## Kontext

Moderne Unternehmensanwendungen, insbesondere solche, die auf Microservices oder verteilten Architekturen basieren,
erfordern eine hohe Sichtbarkeit ihres Laufzeitverhaltens. Herkömmliche Observability trennt Metriken, Logging und
Tracing oft in verschiedene APIs und Bibliotheken, was zu inkonsistenten Metadaten und erhöhter Komplexität bei der
Wartung führt.

Im Spring-Ökosystem (Spring Boot 3.x/4.x) wurde **Micrometer Observation** als einheitliche API für die Handhabung von
Metriken und Tracing eingeführt. Dies ersetzt das veraltete Spring Cloud Sleuth und bietet eine stärker integrierte
Möglichkeit zur Instrumentierung von Anwendungen.

---

## Entscheidung

Wir werden **Micrometer Observation** mit Tracing (über Brave oder OpenTelemetry) als primären Observability-Standard
für die Anwendung verwenden. Die gesamte Kommunikation zwischen Diensten (HTTP, JMS, Kafka) sowie kritische
Geschäftslogik wird mithilfe der Observation-API instrumentiert.

---

## Begründung

1. **Einheitliche API**: Micrometer Observation ermöglicht es, Code einmal zu instrumentieren und gleichzeitig Metriken
   und Traces zu erzeugen.
2. **Native Integration**: Es ist das Standard-Observability-Tool für Spring Boot 3+ und 4+, was langfristigen Support
   und eine nahtlose Integration in Spring-Kernkomponenten (RestTemplate, WebClient, Kafka usw.) gewährleistet.
3. **Kontext-Propagierung**: Es übernimmt die Komplexität der Weitergabe von Trace-Kontexten über verschiedene
   Protokolle (HTTP, JMS, Kafka) hinweg.
4. **Anbieterneutralität**: Micrometer bietet eine Brücke zu verschiedenen Backends (Zipkin, Jaeger, Prometheus, OTLP)
   und verhindert so eine Anbieterbindung (Vendor Lock-in).
5. **MDC-Anreicherung**: Es ermöglicht die automatische Synchronisierung von Trace-IDs in den SLF4J MDC, wodurch
   sichergestellt wird, dass Protokolle immer mit Traces korreliert werden.

---

## Konsequenzen

- **Positiv**: Konsistente Observability über alle Module hinweg, verbessertes Debugging in verteilten Umgebungen und
  weniger Boilerplate-Code für das Tracing.
- **Negativ**: Geringfügige Lernkurve für Entwickler, um die Observation-API im Vergleich zu älteren
  Tracing-Bibliotheken zu verstehen.
