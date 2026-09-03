// adventure-specific package details
public class AdventurePackage extends Package {

    public AdventurePackage() {
        this.name = "Adventure Trek";
        this.price = 22000.00;
        this.duration = 7;
    }

    @Override
    public void showDetails() {
        System.out.println("  Package : " + name);
        System.out.println("  Price   : Rs. " + price);
        System.out.println("  Duration: " + duration + " days");
        System.out.println("  Type    : Adventure");
    }
}
