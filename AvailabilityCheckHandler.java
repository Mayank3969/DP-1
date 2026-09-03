// checks seat availability first
public class AvailabilityCheckHandler extends BookingHandler {

    private boolean seatsAvailable;

    public AvailabilityCheckHandler(boolean seatsAvailable) {
        this.seatsAvailable = seatsAvailable;
    }

    @Override
    public void handle(Booking b) {
        System.out.println("  [Availability] Checking seats...");
        if (!seatsAvailable) {
            System.out.println("  [Availability] FAILED — no seats available. Booking stopped.");
            return;
        }
        System.out.println("  [Availability] PASSED — seats confirmed.");
        if (nextHandler != null) nextHandler.handle(b);
    }
}
