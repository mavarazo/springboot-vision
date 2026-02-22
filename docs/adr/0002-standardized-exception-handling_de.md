# ADR 0002: Standardisierte Ausnahme- und Fehlerbehandlung

## Status
Akzeptiert

## Kontext
In komplexen Unternehmensanwendungen ist die Fehlerbehandlung oft inkonsistent. Dies erschwert es sowohl Clients als auch Betriebsteams, zwischen erwarteten Fehlern in der Geschäftslogik und unerwarteten technischen Systemausfällen zu unterscheiden. Ohne eine klare Trennung erzeugen Überwachungssysteme oft unnötige ("laute") Alarme für unkritische Geschäftsereignisse, während kritische technische Ausfälle übersehen werden könnten.

Zudem geben interne Fehlerdarstellungen oft Implementierungsdetails preis oder liefern API-Konsumenten inkonsistente Strukturen, was die clientseitige Fehlerbehandlung erschwert.

## Entscheidung
Wir implementieren eine standardisierte Strategie zur Ausnahmebehandlung basierend auf folgenden Prinzipien:

1.  **Klare Kategorisierung**: Alle benutzerdefinierten Ausnahmen müssen entweder `BusinessException` oder `TechnicalException` erweitern.
    -   **BusinessException**: Repräsentiert eine Verletzung von Geschäftsregeln (z. B. "Ungültige Eingabe", "Unzureichendes Guthaben"). Dies sind erwartete Verhaltensweisen der Geschäftsdomäne.
    -   **TechnicalException**: Repräsentiert einen Fehler in der Infrastruktur oder im zugrunde liegenden System (z. B. "Datenbank-Timeout", "Downstream-Service nicht verfügbar").
2.  **Standardisiertes Antwortformat**: Alle API-Fehler werden unter Verwendung von **Problem Details for HTTP APIs** (RFC 9457 / Spring `ProblemDetail`) zurückgegeben. Dies gewährleistet eine konsistente Struktur (`type`, `title`, `status`, `detail`, `instance`) über alle Dienste hinweg.
3.  **Selektive Alarmierung**:
    -   Alle **TechnicalException**-Instanzen (und unbehandelte Runtime-Exceptions) müssen einen Alarm auslösen, da sie auf einen Systemfehler hinweisen.
    -   **BusinessException**-Instanzen lösen standardmäßig **keine** Alarme aus.
    -   Das Marker-Interface **`Alertable`** wird verwendet, um spezifische `BusinessException`-Typen zu kennzeichnen, die sofortige Aufmerksamkeit der Entwickler oder des Betriebs erfordern (z. B. Erkennung von potenziellem Betrug).
4.  **Einheitliches Error-Mapping**: Ein globaler Exception-Handler (z. B. `@RestControllerAdvice`) welcher von `ResponseEntityExceptionHandler` ableitet, ist dafür verantwortlich, diese Ausnahmen in das Standardformat `ProblemDetail` zu überführen.

---

## Begründung
1.  **Effizienz im Betrieb**: Durch die Trennung von fachlichen und technischen Fehlern reduzieren wir die Alarm-Müdigkeit (Alert Fatigue) im Entwicklungsteam. Alarme werden nur für behebbare Systemfehler ausgelöst.
2.  **Interoperabilität**: Die Verwendung des RFC 9457-Standards (`ProblemDetail`) ermöglicht es Clients, Standardbibliotheken für die Fehlerbehandlung zu verwenden, und bietet eine vorhersehbare Schnittstelle für API-Konsumenten.
3.  **Sicherheit**: Die Standardisierung von Fehlerantworten verhindert das versehentliche Durchsickern von Stack-Traces oder internen Implementierungsdetails an externe Benutzer.
4.  **Flexibilität**: Das `Alertable`-Interface bietet eine saubere Möglichkeit, spezifische Geschäftsszenarien zu kritischen Alarmen zu "erheben", ohne die Basis-Hierarchie der `BusinessException` zu verunreinigen.

---

## Konsequenzen
- **Positiv**: Konsistente Fehlermeldungen über die gesamte Plattform hinweg, verbessertes Signal-Rausch-Verhältnis beim Monitoring und eine bessere Developer Experience beim Debugging.
- **Negativ**: Erfordert Disziplin von den Entwicklern, neue Ausnahmen innerhalb der etablierten Hierarchie korrekt zu kategorisieren und zu implementieren.
