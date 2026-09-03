# System & Data Flow

## System flow (what runs, in order)

```
Main.java
  |
  1. DatabaseManager.getInstance()          -> Singleton returns the ONE shared instance
  |
  2. PackageFactory.createPackage("beach")  -> Factory returns a BeachPackage object
  |
  3. new Booking(user, package)              -> Booking object created, status = "PENDING"
       booking.attach(new EmailNotifier())
       booking.attach(new SMSNotifier())
  |
  4. Build chain:
       AvailabilityCheckHandler -> PaymentValidationHandler -> BookingConfirmationHandler
  |
  5. chain.handle(booking)
       - AvailabilityCheckHandler checks seat flag
           - fail -> print reason, STOP here
           - pass -> call next.handle(booking)
       - PaymentValidationHandler checks payment flag
           - fail -> print reason, STOP here
           - pass -> call next.handle(booking)
       - BookingConfirmationHandler
           - booking.setStatus("CONFIRMED")
  |
  6. booking.setStatus(...) triggers notifyObservers()
       - EmailNotifier.update("CONFIRMED")  -> prints email line
       - SMSNotifier.update("CONFIRMED")    -> prints sms line
  |
  7. DatabaseManager.saveBooking(booking)   -> stored in shared list
  |
  8. DatabaseManager.printAllBookings()     -> shows final state
```

## Data flow (what object holds what)

```
Package (abstract)
  name, price, duration
  |
  ├── BeachPackage
  └── AdventurePackage

Booking
  bookingId, userName, status
  packageBooked  --------> holds a Package object (from factory)
  observers []   --------> holds BookingObserver objects (Email, SMS)

BookingHandler (abstract)
  nextHandler ------------> points to next handler in chain
  |
  ├── AvailabilityCheckHandler
  ├── PaymentValidationHandler
  └── BookingConfirmationHandler
        (this one calls booking.setStatus(...) which triggers Observer)

DatabaseManager (singleton)
  bookings [] ------------> holds all Booking objects ever saved
```

## Why the 4 patterns connect this way

- **Factory** builds the `Package` that goes inside `Booking` — Booking never calls `new BeachPackage()` directly.
- **Chain of Responsibility** validates the `Booking` step by step — no single method does all checks.
- Last handler in the chain changes `Booking.status`, which fires **Observer** — decouples "status changed" from "who needs to know."
- **Singleton** stores the final `Booking` — every class asks the same `DatabaseManager.getInstance()`, never creates its own storage.

## One-line summary per file (use as comment style reference)

- `Package.java` — `// blueprint for any travel package`
- `PackageFactory.java` — `// decides which package object to build`
- `Booking.java` — `// holds booking info and tells observers when status changes`
- `BookingObserver.java` — `// anything that wants to know about status changes`
- `EmailNotifier.java` — `// prints a fake email when notified`
- `SMSNotifier.java` — `// prints a fake sms when notified`
- `BookingHandler.java` — `// one check in the chain, passes to next if ok`
- `AvailabilityCheckHandler.java` — `// checks seat availability first`
- `PaymentValidationHandler.java` — `// checks payment second`
- `BookingConfirmationHandler.java` — `// confirms booking last, updates status`
- `DatabaseManager.java` — `// only one db manager exists app-wide`
- `Main.java` — `// wires everything together and runs the flow`
