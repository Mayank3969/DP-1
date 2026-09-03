import java.sql.Connection;
import java.sql.Statement;

// wipes the bookings table and loads dummy data — run only when you need a clean demo slate
public class CreateData {

    public static void main(String[] args) {
        // reuse the singleton — never opens a second connection
        DatabaseManager db = DatabaseManager.getInstance();
        Connection conn = db.getConnection();

        try (Statement st = conn.createStatement()) {

            // wipe existing data
            st.execute("DROP TABLE IF EXISTS bookings");
            st.execute("CREATE TABLE bookings ("
                     + "id INTEGER PRIMARY KEY, "
                     + "user_name TEXT NOT NULL, "
                     + "package_name TEXT NOT NULL, "
                     + "price REAL NOT NULL, "
                     + "status TEXT NOT NULL)");

            // insert dummy rows
            st.execute("INSERT INTO bookings VALUES (1, 'Alice Roy',   'Beach Getaway',  15000.00, 'CONFIRMED')");
            st.execute("INSERT INTO bookings VALUES (2, 'Bob Singh',   'Adventure Trek', 22000.00, 'CONFIRMED')");
            st.execute("INSERT INTO bookings VALUES (3, 'Carol Nair',  'Beach Getaway',  15000.00, 'PENDING')");

            System.out.println("SeedData: table wiped and 3 dummy bookings inserted.");
        } catch (Exception e) {
            System.out.println("SeedData error: " + e.getMessage());
        }

        // show what's in the DB now
        System.out.println("\n--- DB State after seed ---");
        db.printAllBookings();
        System.out.println();
    }
}
