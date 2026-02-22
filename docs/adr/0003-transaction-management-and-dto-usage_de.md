# ADR 0003: Strategie zur Transaktionssteuerung und DTO-Nutzung

## Status
Akzeptiert

## Kontext
In modernen Spring-Unternehmensanwendungen ist es entscheidend, eine klare Strategie für das Transaktionsmanagement und das Datenmapping über die Schichten hinweg zu haben. Eine inkonsistente Platzierung der Transaktionsgrenzen kann zu Datenbankverbindungs-Leaks, Lazy-Loading-Exceptions oder inkonsistenten Datenzuständen führen. Darüber hinaus führt das direkte Exponieren von internen Entitäten (JPA/Hibernate) über Controller zu einer engen Kopplung zwischen dem Datenbankschema und der öffentlichen API, was es erschwert, beide ohne gegenseitige Beeinträchtigung zu ändern.

## Entscheidung
Wir werden uns an die folgenden Architekturregeln halten:

1.  **Transaktionsgrenzen in der Serviceschicht**: Transaktionen dürfen nur auf der Ebene der Geschäftsservices (z. B. mit `@Transactional`) geöffnet werden. Dies stellt sicher, dass eine einzelne Geschäftsoperation atomar ist und die Ressourcen effizient verwaltet werden. Controller dürfen niemals Transaktionen öffnen oder verwalten.
2.  **DTOs für einfache Anwendungsfälle**: Bei einfachen CRUD-ähnlichen Operationen oder einfacher Geschäftslogik sollten die Services direkt die durch den OpenAPI-Kontrakt generierten DTOs akzeptieren und zurückgeben. Dies reduziert den Mapping-Aufwand und vereinfacht den Entwicklungsfluss.
3.  **Business Records für komplexe Anwendungsfälle**: Wenn die Geschäftslogik komplexer wird (z. B. wenn mehrere Domänenentitäten oder externe Systeme involviert sind), sollten die Services zur Verwendung interner **Java Records** als Geschäftsmodelle übergehen. Dies entkoppelt die Kern-Domänenlogik von der öffentlichen API-Struktur.
4.  **Kein Durchsickern von Entitäten**: Interne Datenbankentitäten (z. B. `@Entity`-Klassen) dürfen niemals an Controller zurückgegeben oder von diesen akzeptiert werden. Sie müssen innerhalb der Serviceschicht auf DTOs (oder Business Records) gemappt werden, bevor sie an den Controller zurückgegeben werden.
5.  **Evolutionäre Architektur**: Wir behandeln die Verwendung von API-DTOs in der Serviceschicht als einen evolutionären Ansatz. Wir beginnen einfach und führen das Mapping auf interne Records nur dann ein, wenn die Komplexität des Anwendungsfalls die zusätzliche Abstraktionsschicht rechtfertigt.

## Begründung
1.  **Atomarität und Integrität**: Die Platzierung von Transaktionen in der Serviceschicht stellt sicher, dass alle Datenbankoperationen innerhalb eines Geschäftsanwendungsfalls gemeinsam erfolgreich sind oder gemeinsam scheitern.
2.  **Entkopplung**: Durch die Verhinderung des Durchsickerns von Entitäten wird sichergestellt, dass der API-Kontrakt stabil bleibt, auch wenn sich das Datenbankschema ändert.
3.  **Wartbarkeit**: Java Records bieten eine leichtgewichtige, unveränderliche Möglichkeit, Geschäftsdaten darzustellen, ohne den Overhead von JPA-Entitäten oder die Kopplung von API-DTOs in komplexen Szenarien.
4.  **Pragmatismus**: Die Erlaubnis von API-DTOs in der Serviceschicht für einfache Fälle verhindert "Over-Engineering" und reduziert Boilerplate-Code, während der Pfad zu einem robusteren Domänenmodell offen bleibt, wenn sich die Anwendung weiterentwickelt.

## Konsequenzen
- **Positiv**: Weniger Boilerplate-Code für einfache Funktionen, klare Trennung der Belange und robustes Transaktionsmanagement.
- **Negativ**: Erfordert sorgfältiges Mapping in komplexen Szenarien, um sicherzustellen, dass Entitäten nicht versehentlich nach außen dringen.
