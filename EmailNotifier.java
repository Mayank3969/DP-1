// prints a fake email when notified
public class EmailNotifier implements BookingObserver {
    @Override
    public void update(String status) {
        System.out.println("  [Email] Booking status updated to: " + status);
    }
}
