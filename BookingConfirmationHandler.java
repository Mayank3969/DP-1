// confirms booking last, updates status (which fires Observer)
public class BookingConfirmationHandler extends BookingHandler {

    @Override
    public void handle(Booking b) {
        System.out.println("  [Confirmation] All checks passed — confirming booking.");
        b.setStatus("CONFIRMED"); // this triggers EmailNotifier + SMSNotifier
    }
}
