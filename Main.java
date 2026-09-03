// wires everything together and runs the flow
public class Main {

    public static void main(String[] args) {

        String agentName = "Ravi Sharma";   // hardcoded agent
        String userName  = "Priya Mehta";   // hardcoded user
        String pkgType   = "beach";         // hardcoded package type

        // --- Step 1: Get the one shared DB instance ---
        DatabaseManager db = DatabaseManager.getInstance();

        // --- Creating Package ---
        System.out.println("\n--- Creating Package ---");
        Package travelPackage = PackageFactory.createPackage(pkgType);
        travelPackage.showDetails();

        // --- Creating Booking ---
        System.out.println("\n--- Creating Booking ---");
        Booking booking = new Booking(userName, travelPackage);
        booking.attach(new EmailNotifier());
        booking.attach(new SMSNotifier());
        System.out.println("  Agent   : " + agentName);
        System.out.println("  User    : " + booking.getUserName());
        System.out.println("  Booking#: " + booking.getBookingId());
        System.out.println("  Status  : " + booking.getStatus());

        // --- Running Booking Chain ---
        System.out.println("\n--- Running Booking Chain ---");
        // change the booleans to false to simulate a failure at any stage
        AvailabilityCheckHandler availability = new AvailabilityCheckHandler(true);
        PaymentValidationHandler  payment      = new PaymentValidationHandler(true);
        BookingConfirmationHandler confirmation = new BookingConfirmationHandler();

        availability.setNext(payment);
        payment.setNext(confirmation);

        availability.handle(booking); // kicks off the chain

        // --- Saving to Database ---
        System.out.println("\n--- Saving to Database ---");
        if ("CONFIRMED".equals(booking.getStatus())) {
            db.saveBooking(booking);
        } else {
            System.out.println("  Booking not confirmed — skipping save.");
        }

        // --- DB State ---
        System.out.println("\n--- DB State (all bookings) ---");
        db.printAllBookings();
        System.out.println();
    }
}
