// checks payment second
public class PaymentValidationHandler extends BookingHandler {

    private boolean paymentValid;

    public PaymentValidationHandler(boolean paymentValid) {
        this.paymentValid = paymentValid;
    }

    @Override
    public void handle(Booking b) {
        System.out.println("  [Payment]      Validating payment...");
        if (!paymentValid) {
            System.out.println("  [Payment]      FAILED — payment invalid. Booking stopped.");
            return;
        }
        System.out.println("  [Payment]      PASSED — payment verified.");
        if (nextHandler != null) nextHandler.handle(b);
    }
}
