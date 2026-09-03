// one check in the chain, passes to next if ok
public abstract class BookingHandler {

    protected BookingHandler nextHandler;

    // link the next handler in the chain
    public void setNext(BookingHandler next) {
        this.nextHandler = next;
    }

    public abstract void handle(Booking b);
}
