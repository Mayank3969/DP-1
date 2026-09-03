// anything that wants to know when booking status changes
public interface BookingObserver {
    void update(String status);
}
