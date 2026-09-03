// decides which package object to build
public class PackageFactory {

    public static Package createPackage(String type) {
        switch (type.toLowerCase()) {
            case "beach":
                return new BeachPackage();
            case "adventure":
                return new AdventurePackage();
            default:
                throw new IllegalArgumentException("Unknown package type: " + type);
        }
    }
}
