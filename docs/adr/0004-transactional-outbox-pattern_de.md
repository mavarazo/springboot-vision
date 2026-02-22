# ADR 0004: Transactional Outbox Pattern für zuverlässige Event-Publikation

## Status
Akzeptiert

## Kontext
In unserer aktuellen Architektur verwenden wir Apache Kafka für die eventgesteuerte Kommunikation. Dabei stehen wir vor zwei kritischen Herausforderungen:
1.  **Dual-Write-Problem**: Bei der gleichzeitigen Aktualisierung der Datenbank und der Veröffentlichung eines Events in Kafka in derselben Operation können wir keine Atomarität garantieren. Wenn die Datenbankaktualisierung erfolgreich ist, aber die Kafka-Veröffentlichung fehlschlägt (oder umgekehrt), gerät das System in einen inkonsistenten Zustand.
2.  **Erschöpfung des Connection-Pools**: Wir nutzen derzeit das Event-Publikationsregister von Spring Modulith, um Ereignisse zu persistieren und Wiederholungsversuche zu verwalten. Unter hoher Last führt die große Anzahl asynchroner Prozesse, die versuchen, das Ereignisregister in der Datenbank zu aktualisieren, zu einer Sättigung des Connection-Pools und zu Datenbanksperren, was effektiv zu einem Selbstblockieren des Systems führt.

## Entscheidung
Wir werden das **Transactional Outbox Pattern** für alle ausgehenden Ereignisse an Kafka implementieren.

1.  **Atomarität**: Anstatt direkt an Kafka zu publizieren, schreibt der Business-Service das Event in eine dedizierte `outbox`-Tabelle innerhalb derselben Datenbanktransaktion wie die Geschäftsdatenaktualisierung.
2.  **Trennung der Zuständigkeiten**: Ein separater, dedizierter Prozess (der „Message Relay“ oder „Outbox Processor“) wird asynchron die `outbox`-Tabelle abfragen und die Ereignisse an Kafka veröffentlichen.
3.  **Kontrollierter Durchsatz**: Der Outbox-Prozessor verwendet einen dedizierten, begrenzten Connection-Pool und eine Batch-Verarbeitung, um sicherzustellen, dass er die Datenbank nicht überfordert oder anderen Geschäftsprozessen die Verbindungen entzieht.
4.  **At-Least-Once-Zustellung**: Ereignisse werden erst nach erfolgreicher Bestätigung durch Kafka als verarbeitet markiert oder aus der Outbox-Tabelle gelöscht.

---

## Begründung
1.  **Konsistenz**: Durch die Verwendung der Outbox-Tabelle stellen wir sicher, dass das Ereignis nur gespeichert wird, wenn die Geschäftstransaktion erfolgreich ist, wodurch das Dual-Write-Problem gelöst wird.
2.  **Skalierbarkeit**: Die Verlagerung der Kafka-Publikationslogik aus der Haupttransaktion verkürzt die Haltedauer von Datenbankverbindungen und eliminiert das Risiko von verteilten Sperrproblemen durch hoch-konkurrente Ereignisregistrierung.
3.  **Ressourcenschutz**: Der Outbox-Prozessor kann unabhängig von den Business-Services eingestellt werden, was es uns ermöglicht, die Event-Veröffentlichung während Spitzenlasten zu drosseln, um den Datenbank-Connection-Pool zu schützen.
4.  **Operative Stabilität**: Das Ersetzen des schwergewichtigen „Missbrauchs“ des internen Eventregisters von Spring Modulith durch einen schlanken, zweckgebundenen Outbox-Mechanismus bietet mehr Transparenz und Kontrolle über den Event-Lebenszyklus.

---

## Konsequenzen
- **Positiv**: Garantierte Eventual Consistency zwischen Datenbank und Kafka, verbesserte Systemstabilität unter hoher Last und besseres Management der Datenbankressourcen.
- **Negativ**: Erhöhte Komplexität in der Infrastruktur durch die Notwendigkeit eines Outbox-Prozessors und einer zusätzlichen Datenbanktabelle. Es gibt eine geringfügige Verzögerung (Latenz) zwischen der Datenbanktransaktion und dem Erscheinen des Ereignisses in Kafka.
