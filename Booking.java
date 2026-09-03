import java.util.ArrayList;
import java.util.List;

// holds booking info and tells everyone watching that status changed
public class Booking {

    private static int idCounter = 1;

    private int bookingId;
    private String userName;
    private Package packageBooked;
    private String status;
    private List<BookingObserver> observers = new ArrayList<>();

    public Booking(String userName, Package packageBooked) {
        this.bookingId = idCounter++;
        this.userName = userName;
        this.packageBooked = packageBooked;
        this.status = "PENDING";
    }

    // add a listener
    public void attach(BookingObserver o) {
        observers.add(o);
    }

    // update status, then alert all listeners
    public void setStatus(String s) {
        this.status = s;
        notifyObservers();
    }

    // loop through and tell each observer
    private void notifyObservers() {
        for (BookingObserver o : observers) {
            o.update(status);
        }
    }

    public int getBookingId()         { return bookingId; }
    public String getUserName()        { return userName; }
    public Package getPackageBooked()  { return packageBooked; }
    public String getStatus()          { return status; }
}
