// prints a fake sms when notified
public class SMSNotifier implements BookingObserver {
    @Override
    public void update(String status) {
        System.out.println("  [SMS]   Booking status updated to: " + status);
    }
}
