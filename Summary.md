# Travel Booking System — Code Summary

> This document explains **every file, every class, and every method** in the project.
> Grouped by the 4 design patterns and the person who owns each one.
> If you know what a design pattern is, you will understand exactly what is happening
> in the code and *why* — without needing to open a single Java file.

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Shared Core — Booking.java](#2-shared-core--bookingjava)
3. [Person A — Singleton — DatabaseManager.java](#3-person-a--singleton--databasemanagerjava)
4. [Piyush — Factory — Package files](#4-piyush--factory--package-files)
5. [Mayank — Chain of Responsibility — Handler files](#5-mayank--chain-of-responsibility--handler-files)
6. [Person D — Observer — Notifier files](#6-person-d--observer--notifier-files)
7. [Wiring — Main.java](#7-wiring--mainjava)
8. [Utility — CreateData.java](#8-utility--createdatajava)
9. [How All 4 Patterns Connect](#9-how-all-4-patterns-connect)
10. [Full Execution Flow Step by Step](#10-full-execution-flow-step-by-step)
11. [How to Run](#11-how-to-run)

---

## 1. Project Overview

Console-based Java app simulating a travel package booking process.
Its real purpose: demonstrate 4 classic design patterns working together.

| Pattern | Owner | Problem it solves |
|---|---|---|
| Singleton | Person A | Only ONE database connection across the whole app |
| Factory | Piyush | Callers never need to know which Package subclass to create |
| Chain of Responsibility | Mayank | Booking passes independent checks one by one before being confirmed |
| Observer | Person D | Status change notifies multiple parties without the Booking knowing who they are |

---

## 2. Shared Core — `Booking.java`

**Role:** The central data object passed around by all 4 patterns.

### Fields

| Field | Type | What it holds |
|---|---|---|
| `bookingId` | int | In-memory counter starting at 1 each run (display only) |
| `userName` | String | Customer name |
| `packageBooked` | Package | The Package object built by the Factory |
| `status` | String | Current state: `"PENDING"` or `"CONFIRMED"` |
| `observers` | List\<BookingObserver\> | All registered listeners (email, SMS, etc.) |

### Constructor — `Booking(String userName, Package packageBooked)`

- Auto-assigns `bookingId` using `idCounter++` (static, resets each run).
- Sets `status = "PENDING"` — every booking starts unconfirmed.
- The real unique ID comes from SQLite AUTOINCREMENT in DatabaseManager.

### `attach(BookingObserver o)`

- Adds an observer to the internal list.
- Called in Main right after the booking is created.
- Why a list? So you can attach any number of notifiers (email, SMS, push, log…)
  without ever changing this class.

### `setStatus(String s)`

- Updates `this.status`.
- Then calls `notifyObservers()` immediately.
- This is the **bridge between Chain of Responsibility and Observer** —
  the last chain handler calls this, which auto-fires all observers.

### `notifyObservers()`

- Private. Loops through every observer and calls `o.update(status)`.
- `Booking` does NOT know what `EmailNotifier` or `SMSNotifier` do.
  It just calls `update()` and moves on. That is the Observer pattern.

### Getters

`getBookingId()`, `getUserName()`, `getPackageBooked()`, `getStatus()`
Used by `DatabaseManager.saveBooking()` to extract data for the SQL INSERT.

---

## 3. Person A — Singleton — `DatabaseManager.java`

**Pattern:** Singleton
**Why needed:** Only one database connection should ever exist. Every class that
needs the DB gets back the exact same object — same connection, same state.

### The Singleton mechanism

```java
private static DatabaseManager instance;   // the one and only object

private DatabaseManager() { ... }          // PRIVATE constructor
                                           // nobody outside can write: new DatabaseManager()

public static DatabaseManager getInstance() {
    if (instance == null) {                // first call: create it
        instance = new DatabaseManager();
    }
    return instance;                       // every future call: returns same object
}
```

**Three rules that make it a Singleton:**
1. Private constructor — forces all callers through `getInstance()`.
2. Static field — `instance` lives at class level, shared everywhere.
3. Lazy init — object is created only on the first call, not at class load time.

### Private Constructor flow

1. `Class.forName("org.sqlite.JDBC")` — loads the SQLite driver into memory.
2. `DriverManager.getConnection("jdbc:sqlite:travel.db")` — opens/creates `travel.db` file.
3. `initDB()` — creates the table if it does not exist yet.

### `initDB()` — Table creation (runs once per startup)

```sql
CREATE TABLE IF NOT EXISTS bookings (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    user_name    TEXT    NOT NULL,
    package_name TEXT    NOT NULL,
    price        REAL    NOT NULL,
    status       TEXT    NOT NULL
)
```

- `IF NOT EXISTS` — safe to call every run; skipped if table already exists.
- `AUTOINCREMENT` — SQLite assigns the next integer id automatically.
  We never pass an `id` in INSERT. Running the app 10 times gives IDs 1–10 with no conflicts.
- `travel.db` is created in the same folder as the `.java` files. It persists between runs.

### `saveBooking(Booking b)` — INSERT

```sql
INSERT INTO bookings (user_name, package_name, price, status)
VALUES (?, ?, ?, ?)
```

- `PreparedStatement` with `?` placeholders — values filled in safely via
  `ps.setString()`, `ps.setDouble()`. Prevents SQL injection.
- `id` column is intentionally absent — SQLite fills it with AUTOINCREMENT.
- After insert: `ps.getGeneratedKeys()` retrieves the auto-assigned id and prints it.

### `printAllBookings()` — SELECT

```sql
SELECT id, user_name, package_name, price, status FROM bookings
```

- Fetches every row (all runs, cumulative) and prints in a formatted table using `printf`.

### `getConnection()`

- Exposes the raw `Connection` so `CreateData.java` can reuse it.
- `CreateData` never opens its own connection — it calls
  `DatabaseManager.getInstance().getConnection()`, which correctly respects the Singleton.

---

## 4. Piyush — Factory — Package files

**Pattern:** Factory Method
**Files:** `Package.java`, `BeachPackage.java`, `AdventurePackage.java`, `PackageFactory.java`

### Problem without Factory

```java
// BAD — tightly coupled, must change this line everywhere to add a new type:
Package p = new BeachPackage();
```

With Factory, every caller writes:
```java
Package p = PackageFactory.createPackage("beach");
// caller never knows or cares it got a BeachPackage
```

### `Package.java` — Abstract base class

```java
public abstract class Package {
    protected String name;
    protected double price;
    protected int duration;            // in days

    public abstract void showDetails(); // every subclass must implement this

    // getters: getName(), getPrice(), getDuration()
}
```

- `abstract` — cannot do `new Package()`. It is a blueprint only.
- `abstract void showDetails()` — enforces that every package type knows how to print itself.
- Fields are `protected` so subclasses set them directly in their constructors.

### `BeachPackage.java` — Concrete subclass

```java
public BeachPackage() {
    this.name = "Beach Getaway";
    this.price = 15000.00;
    this.duration = 5;                 // 5 days
}
```

- Sets beach-specific values in constructor.
- Overrides `showDetails()` with beach output.

### `AdventurePackage.java` — Concrete subclass

- Same structure: "Adventure Trek", Rs. 22000, 7 days.
- To add a new package type later: create one new class, zero changes elsewhere.

### `PackageFactory.java` — The Factory

```java
public static Package createPackage(String type) {
    switch (type.toLowerCase()) {
        case "beach":     return new BeachPackage();
        case "adventure": return new AdventurePackage();
        default: throw new IllegalArgumentException("Unknown package type: " + type);
    }
}
```

- `static` — called directly as `PackageFactory.createPackage("beach")`. No object needed.
- Returns type `Package` (the abstract parent). The caller never sees `BeachPackage` —
  only the `Package` interface. This is **programming to an interface, not an implementation**.
- `default` throws an exception immediately for unknown types — fails loudly, no silent bugs.

---

## 5. Mayank — Chain of Responsibility — Handler files

**Pattern:** Chain of Responsibility
**Files:** `BookingHandler.java`, `AvailabilityCheckHandler.java`,
`PaymentValidationHandler.java`, `BookingConfirmationHandler.java`

### Problem without Chain

```java
// BAD — one giant method, grows forever, hard to test individual checks:
public void processBooking(Booking b) {
    if (!seatsAvailable) { ... return; }
    if (!paymentValid)   { ... return; }
    // confirm...
    // add more checks here forever...
}
```

With Chain, each check is its own class. You can add, remove, or reorder checks
without touching any other class.

### `BookingHandler.java` — Abstract handler (the chain backbone)

```java
public abstract class BookingHandler {
    protected BookingHandler nextHandler;      // pointer to next check in line

    public void setNext(BookingHandler next) {
        this.nextHandler = next;               // wire at runtime in Main
    }

    public abstract void handle(Booking b);    // each concrete handler defines this
}
```

- `nextHandler` is the key. Setting it wires two handlers together.
- Setting it to `null` (or not calling `setNext`) means "I am the last one."
- `handle()` is abstract — each subclass must implement its own logic.

### `AvailabilityCheckHandler.java` — Handler 1

```java
public AvailabilityCheckHandler(boolean seatsAvailable) {
    this.seatsAvailable = seatsAvailable;    // simulates real seat data
}

public void handle(Booking b) {
    if (!seatsAvailable) {
        System.out.println("FAILED — no seats. Booking stopped.");
        return;                              // chain STOPS here
    }
    System.out.println("PASSED — seats confirmed.");
    if (nextHandler != null) nextHandler.handle(b);  // pass to Handler 2
}
```

- `boolean` flag injected in constructor — in a real system this would be a DB query.
- Fail: prints reason, `return`. `PaymentValidationHandler` never runs.
- Pass: calls `nextHandler.handle(b)` → control moves to next handler.

### `PaymentValidationHandler.java` — Handler 2

- Same structure, checks `boolean paymentValid`.
- Fail: chain stops here. Confirmation never runs.
- Pass: calls `nextHandler.handle(b)` → moves to Handler 3.

### `BookingConfirmationHandler.java` — Handler 3 (final)

```java
public void handle(Booking b) {
    System.out.println("All checks passed — confirming booking.");
    b.setStatus("CONFIRMED");    // fires Observer pattern automatically
}
```

- No `nextHandler` call — this is the end.
- `b.setStatus("CONFIRMED")` is the bridge to Observer — calling this triggers
  `notifyObservers()` inside `Booking`, which fires `EmailNotifier` and `SMSNotifier`.
- No boolean flag needed — if this handler runs, all prior checks already passed.

### How the chain is assembled in Main.java

```java
AvailabilityCheckHandler availability = new AvailabilityCheckHandler(true);
PaymentValidationHandler  payment      = new PaymentValidationHandler(true);
BookingConfirmationHandler confirmation = new BookingConfirmationHandler();

availability.setNext(payment);       // availability → payment
payment.setNext(confirmation);       // payment → confirmation

availability.handle(booking);        // kick off from handler 1
```

**Simulate failures by flipping booleans to `false` in Main.java:**
```java
new AvailabilityCheckHandler(false);  // no seats — stops at handler 1
new PaymentValidationHandler(false);  // payment fails — stops at handler 2
```

---

## 6. Person D — Observer — Notifier files

**Pattern:** Observer (also known as Publish-Subscribe)
**Files:** `BookingObserver.java`, `EmailNotifier.java`, `SMSNotifier.java`
**Supporting code in:** `Booking.java` (holds the observer list, calls `notifyObservers()`)

### Problem without Observer

```java
// BAD — BookingConfirmationHandler must know every notifier:
sendEmail(booking);
sendSMS(booking);
// to add push notification: edit this file, add sendPush(booking)
```

With Observer, `Booking` knows nothing about email or SMS.
It just calls `notifyObservers()` and whoever is registered gets notified.

### `BookingObserver.java` — The interface (contract)

```java
public interface BookingObserver {
    void update(String status);
}
```

- One method only: `update(String status)`.
- Any class that wants to listen to status changes implements this interface.
- `Booking` only knows about `BookingObserver` — never about the concrete classes.

### `EmailNotifier.java` — Concrete observer

```java
public class EmailNotifier implements BookingObserver {
    @Override
    public void update(String status) {
        System.out.println("  [Email] Booking status updated to: " + status);
    }
}
```

- Implements `BookingObserver` — must provide `update()`.
- In this demo: prints a line. In a real system: calls an email API.

### `SMSNotifier.java` — Concrete observer

- Same structure, prints `[SMS]` prefix instead.
- Both classes are fully independent — neither knows the other exists.

### How Observer fires

In `Main.java` — registering:
```java
booking.attach(new EmailNotifier());   // observers list: [EmailNotifier]
booking.attach(new SMSNotifier());     // observers list: [EmailNotifier, SMSNotifier]
```

When `BookingConfirmationHandler` calls `booking.setStatus("CONFIRMED")`:
```java
// inside Booking.setStatus():
this.status = "CONFIRMED";
notifyObservers();         // auto-fires

// inside notifyObservers():
for (BookingObserver o : observers) {
    o.update(status);      // → EmailNotifier.update("CONFIRMED")
                           // → SMSNotifier.update("CONFIRMED")
}
```

**To add a third notifier (e.g. PushNotifier):**
1. Create `class PushNotifier implements BookingObserver { ... }`
2. Add `booking.attach(new PushNotifier())` in Main
3. Zero changes to `Booking`, `EmailNotifier`, or `SMSNotifier`

---

## 7. Wiring — `Main.java`

**Role:** Assembles all 4 patterns into a single runnable flow. Owns no pattern itself.

### Hardcoded values

```java
String agentName = "Ravi Sharma";   // no real Agent class per spec
String userName  = "Priya Mehta";   // customer name
String pkgType   = "beach";         // drives PackageFactory
```

### Step-by-step actions in Main

| Step | Code | Pattern triggered |
|---|---|---|
| 1 | `DatabaseManager.getInstance()` | Singleton |
| 2 | `PackageFactory.createPackage("beach")` | Factory |
| 3 | `new Booking(userName, pkg)` + `attach()` x2 | Observer (register) |
| 4 | Build chain with `setNext()` | Chain of Responsibility (setup) |
| 5 | `availability.handle(booking)` | Chain runs → last handler fires Observer |
| 6 | Check `"CONFIRMED".equals(booking.getStatus())` | Guard before saving |
| 7 | `db.saveBooking(booking)` | Singleton (INSERT) |
| 8 | `db.printAllBookings()` | Singleton (SELECT) |

---

## 8. Utility — `CreateData.java`

**Role:** Standalone reset tool. Not part of the booking flow.
Run this when you need a clean database before a demo.

### What it does

```java
DatabaseManager db   = DatabaseManager.getInstance();  // Singleton — same connection
Connection      conn = db.getConnection();

st.execute("DROP TABLE IF EXISTS bookings");    // wipe everything
st.execute("CREATE TABLE bookings (...)");      // recreate schema
st.execute("INSERT INTO bookings VALUES (1, 'Alice Roy',  'Beach Getaway',  15000.00, 'CONFIRMED')");
st.execute("INSERT INTO bookings VALUES (2, 'Bob Singh',  'Adventure Trek', 22000.00, 'CONFIRMED')");
st.execute("INSERT INTO bookings VALUES (3, 'Carol Nair', 'Beach Getaway',  15000.00, 'PENDING')");
```

- Uses `DatabaseManager.getInstance()` — never opens a second connection.
  Singleton is respected even in the seed script.
- `DROP TABLE` removes all existing rows and the table structure.
- Recreates the table and inserts 3 dummy rows with explicit IDs 1, 2, 3.

> **Run only when needed:**
> `java -cp ".;Database\lib\sql-JDBC.jar" CreateData`
>
> After this, running Main adds rows starting at ID 4 (AUTOINCREMENT continues).

---

## 9. How All 4 Patterns Connect

The patterns are not isolated islands — they form a pipeline.

```
Main.java
    │
    ├─► DatabaseManager.getInstance()               [SINGLETON]
    │       Opens one SQLite connection, creates table if needed
    │
    ├─► PackageFactory.createPackage("beach")       [FACTORY]
    │       Returns BeachPackage (typed as Package)
    │
    ├─► new Booking(user, package)
    │       Booking holds the Package from Factory ──────────────[Factory → Booking]
    │       booking.attach(EmailNotifier)            [OBSERVER: register listener]
    │       booking.attach(SMSNotifier)              [OBSERVER: register listener]
    │
    ├─► availability.handle(booking)                [CHAIN OF RESPONSIBILITY]
    │       AvailabilityCheckHandler  → PASS → calls next
    │       PaymentValidationHandler  → PASS → calls next
    │       BookingConfirmationHandler → calls booking.setStatus("CONFIRMED")
    │                                              │
    │                               [Chain → Observer bridge at setStatus()]
    │                                              │
    │                               notifyObservers() fires:
    │                                 EmailNotifier.update("CONFIRMED")  → [Email] line
    │                                 SMSNotifier.update("CONFIRMED")    → [SMS] line
    │
    └─► db.saveBooking(booking)                     [SINGLETON: INSERT]
    └─► db.printAllBookings()                       [SINGLETON: SELECT]
```

### The two critical bridges

**Bridge 1 — Factory → Booking**
`PackageFactory` creates the `Package`. That object is passed to `new Booking(...)`.
`Booking` never calls `new BeachPackage()` — it receives a ready `Package`
and does not care which subclass it is.

**Bridge 2 — Chain → Observer**
`BookingConfirmationHandler` calls `booking.setStatus("CONFIRMED")`.
That setter internally calls `notifyObservers()`.
So the Chain of Responsibility **triggers** the Observer — they are stitched
together at the `setStatus()` method inside `Booking`.

---

## 10. Full Execution Flow Step by Step

What happens exactly when you run `java Main`:

```
1.  DatabaseManager.getInstance()
      private constructor runs once:
        Class.forName("org.sqlite.JDBC")         — loads SQLite driver
        DriverManager.getConnection(travel.db)   — opens/creates travel.db
        initDB()                                 — CREATE TABLE IF NOT EXISTS
      instance stored in static field forever

2.  PackageFactory.createPackage("beach")
      switch matches "beach"
      returns new BeachPackage()
        name="Beach Getaway", price=15000, duration=5

3.  new Booking("Priya Mehta", travelPackage)
      bookingId = 1  (in-memory counter, display only)
      status    = "PENDING"
      packageBooked = the BeachPackage object

4.  booking.attach(new EmailNotifier())
      observers = [EmailNotifier]

5.  booking.attach(new SMSNotifier())
      observers = [EmailNotifier, SMSNotifier]

6.  availability.handle(booking)               — chain starts
      seatsAvailable=true → PASSED → calls payment.handle(booking)

7.  payment.handle(booking)
      paymentValid=true   → PASSED → calls confirmation.handle(booking)

8.  confirmation.handle(booking)
      prints "confirming booking"
      calls booking.setStatus("CONFIRMED")
        status = "CONFIRMED"
        notifyObservers():
          EmailNotifier.update("CONFIRMED")   → prints [Email] line
          SMSNotifier.update("CONFIRMED")     → prints [SMS] line

9.  "CONFIRMED".equals(booking.getStatus())   → true

10. db.saveBooking(booking)
      PreparedStatement INSERT (user_name, package_name, price, status)
      SQLite assigns next AUTOINCREMENT id automatically
      getGeneratedKeys() retrieves it → prints "Saved booking with DB id=X"

11. db.printAllBookings()
      SELECT * FROM bookings
      prints formatted table of ALL rows ever saved across all runs
```

---

## 11. How to Run

### Normal run

Double-click `run.bat`, or from terminal:

```
javac -cp ".;Database\lib\sql-JDBC.jar" *.java
java  -cp ".;Database\lib\sql-JDBC.jar" Main
```

### Simulate a booking failure

In `Main.java` change a boolean to `false` and recompile:

```java
// No seats — chain stops at handler 1, booking stays PENDING, not saved to DB:
new AvailabilityCheckHandler(false);

// Payment fails — chain stops at handler 2, booking stays PENDING, not saved:
new PaymentValidationHandler(false);
```

### Reset database with dummy data

```
java -cp ".;Database\lib\sql-JDBC.jar" CreateData
```

Wipes all rows, inserts Alice Roy, Bob Singh, Carol Nair. Use before a demo.

### View database visually

Open **DB Browser for SQLite** → Open Database:
```
C:\Users\Mayank\OneDrive\Desktop\Mayank\Projects\Vsem\DP\travel.db
```
Tab: Browse Data → Table: `bookings`

---

*Travel Booking System — Design Patterns Project*
*Patterns: Singleton (Person A) · Factory (Piyush) · Chain of Responsibility (Mayank) · Observer (Person D)*
