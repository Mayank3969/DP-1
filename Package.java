// blueprint for any travel package
public abstract class Package {
    protected String name;
    protected double price;
    protected int duration; // in days

    public abstract void showDetails();

    public String getName()    { return name; }
    public double getPrice()   { return price; }
    public int getDuration()   { return duration; }
}
