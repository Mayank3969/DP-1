# SKILL: Build Travel Booking Design-Pattern Project

## Goal
Build a Java console project for 4 students, each covering exactly one design pattern:
1. Singleton
2. Factory
3. Chain of Responsibility
4. Observer

Beginner-level Java (5th sem student). Comments must be SHORT, human-style, one line max per method/class — not exam-style explanations. Example good comment: `// only one db instance allowed`. Example bad comment: `// This class implements the Singleton design pattern which ensures...`

## Hard constraints
- Exactly 4 patterns, no more, no less.
- No cancel-booking feature.
- No separate browse-package screen (merge into search/select).
- No real Agent class — hardcode agent values directly in Main.java.
- Console-based (System.out.println), no UI, no DB engine — use in-memory ArrayLists.
- Keep each class under ~40 lines where possible.

## Build order (do NOT reorder)

### Step 1 — Core data class
File: `Booking.java`
- Fields: `bookingId`, `userName`, `packageBooked` (Package type), `status` (String).
- Holds `List<BookingObserver> observers`.
- Method `attach(BookingObserver o)`.
- Method `setStatus(String s)` → updates status, then calls `notifyObservers()`.
- Method `notifyObservers()` → loops observers, calls `update(status)`.
- No pattern-heavy comment, just: `// tells everyone watching that status changed`

### Step 2 — Observer pattern files
Files: `BookingObserver.java` (interface, one method `update(String status)`), `EmailNotifier.java`, `SMSNotifier.java`
- Each notifier just prints a line like `Email sent: Booking CONFIRMED`.

### Step 3 — Factory pattern files
Files: `Package.java` (abstract class: name, price, duration + `showDetails()`), `BeachPackage.java`, `AdventurePackage.java`, `PackageFactory.java`
- `PackageFactory.createPackage(String type)` — if/else or switch, returns correct subclass.
- Comment style: `// decides which package object to build`

### Step 4 — Chain of Responsibility files
Files: `BookingHandler.java` (abstract: `nextHandler` field, `setNext()`, abstract `handle(Booking b)`), `AvailabilityCheckHandler.java`, `PaymentValidationHandler.java`, `BookingConfirmationHandler.java`
- Each concrete handler: checks one condition (use a hardcoded boolean flag passed in constructor to simulate pass/fail), prints result, calls `next.handle(b)` if passed, else stops and prints failure reason.
- Comment style: `// checks seat availability first`

### Step 5 — Singleton file
File: `DatabaseManager.java`
- Private static instance, private constructor, `getInstance()`.
- Holds `List<Booking> bookings`.
- Method `saveBooking(Booking b)`, `printAllBookings()`.
- Comment style: `// only one db manager exists app-wide`

### Step 6 — Main.java (wiring)
- Hardcode: agent name, user name, package type string.
- Flow:
  1. Get `DatabaseManager` instance.
  2. Create package via `PackageFactory`.
  3. Create `Booking`, attach `EmailNotifier` + `SMSNotifier`.
  4. Build chain: availability → payment → confirmation.
  5. Run chain on booking.
  6. If chain reaches end, `booking.setStatus("CONFIRMED")`.
  7. Save booking via singleton.
  8. Print all bookings from DB.
- Add 2-3 `System.out.println("--- step name ---")` markers so console output reads like a story, not a dump.

## After code is generated
1. Create `README.md` — project overview, how to run, which pattern is where, why chosen (already drafted separately — reuse content, don't regenerate from scratch).
2. Create `FLOW.md` — data flow + system flow explanation (already drafted separately — reuse, don't regenerate).
3. Do NOT add extra design patterns, extra features, or extra files beyond what's listed above, even if it seems "cleaner."
4. Do NOT write javadoc-style block comments. One-line inline comments only, and only on non-obvious lines.

## Definition of done
- 4 patterns present, one clearly owned per file group.
- Compiles and runs with `javac *.java && java Main`.
- Console output shows: package created → booking created → chain checks pass/fail → notifications fire → DB saves → DB prints all bookings.
- README.md and FLOW.md exist alongside code.
