// beach-specific package details
public class BeachPackage extends Package {

    public BeachPackage() {
        this.name = "Beach Getaway";
        this.price = 15000.00;
        this.duration = 5;
    }

    @Override
    public void showDetails() {
        System.out.println("  Package : " + name);
        System.out.println("  Price   : Rs. " + price);
        System.out.println("  Duration: " + duration + " days");
        System.out.println("  Type    : Beach");
    }
}
