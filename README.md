# Travel Booking System — Design Patterns Project

Console-based Java project simulating a travel package booking flow. Built to demonstrate 4 design patterns, one per team member.

## Patterns used

| Pattern | Where | Why |
|---|---|---|
| Singleton | `DatabaseManager.java` | Only one shared storage instance across the whole app — no duplicate DB connections |
| Factory | `PackageFactory.java` + `Package`, `BeachPackage`, `AdventurePackage` | Creates different package types without exposing `new BeachPackage()` everywhere |
| Chain of Responsibility | `BookingHandler.java` + 3 handlers | Booking request passes through availability → payment → confirmation checks, one handler per stage |
| Observer | `Booking.java` + `BookingObserver`, `EmailNotifier`, `SMSNotifier` | When booking status changes, all listeners get notified automatically |

## Team split

- **Person A** — Singleton — `DatabaseManager.java`
- **Person B** — Factory — `Package.java`, `BeachPackage.java`, `AdventurePackage.java`, `PackageFactory.java`
- **Person C** — Chain of Responsibility — `BookingHandler.java`, `AvailabilityCheckHandler.java`, `PaymentValidationHandler.java`, `BookingConfirmationHandler.java`
- **Person D** — Observer — `Booking.java`, `BookingObserver.java`, `EmailNotifier.java`, `SMSNotifier.java`
- **Main.java** — wiring, shared by whoever integrates

## Scope cuts (intentional)

- No cancel-booking feature.
- No separate browse-package step (merged into package selection).
- No real Agent class — agent name hardcoded in `Main.java`.
- No real database — in-memory `ArrayList` inside `DatabaseManager`.

## How to run

```
javac *.java
java Main
```

Expected: console prints package creation, booking creation, each chain check passing (or failing), notifications firing, and final DB state.

## File map

See `FLOW.md` for detailed data flow and system flow.
