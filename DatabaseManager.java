import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

// only one db manager exists app-wide
public class DatabaseManager {

    private static DatabaseManager instance;
    private Connection conn;

    // DB file sits next to the compiled classes
    private static final String DB_URL = "jdbc:sqlite:travel.db";

    private DatabaseManager() {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(DB_URL);
            initDB();
        } catch (Exception e) {
            System.out.println("DB init error: " + e.getMessage());
        }
    }

    // create table if not already there (persistent between runs)
    private void initDB() throws Exception {
        // AUTOINCREMENT ensures ids never repeat even after deletes
        String sql = "CREATE TABLE IF NOT EXISTS bookings ("
                   + "id           INTEGER PRIMARY KEY AUTOINCREMENT, "
                   + "user_name    TEXT    NOT NULL, "
                   + "package_name TEXT    NOT NULL, "
                   + "price        REAL    NOT NULL, "
                   + "status       TEXT    NOT NULL"
                   + ")";
        try (Statement st = conn.createStatement()) {
            st.execute(sql);
        }
    }

    // only one instance ever
    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    // insert a booking row — id is auto-assigned by SQLite, never conflicts
    public void saveBooking(Booking b) {
        String sql = "INSERT INTO bookings (user_name, package_name, price, status) "
                   + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, b.getUserName());
            ps.setString(2, b.getPackageBooked().getName());
            ps.setDouble(3, b.getPackageBooked().getPrice());
            ps.setString(4, b.getStatus());
            ps.executeUpdate();
            // get the id SQLite actually assigned
            ResultSet rs = ps.getGeneratedKeys();
            int dbId = rs.next() ? rs.getInt(1) : -1;
            System.out.println("  Saved booking to database with DB id=" + dbId);
        } catch (Exception e) {
            System.out.println("Save error: " + e.getMessage());
        }
    }

    // print every row in the bookings table
    public void printAllBookings() {
        String sql = "SELECT id, user_name, package_name, price, status FROM bookings";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            System.out.printf("  %-6s %-15s %-20s %-10s %-12s%n",
                    "ID", "User", "Package", "Price", "Status");
            System.out.println("  " + "-".repeat(65));
            while (rs.next()) {
                System.out.printf("  %-6d %-15s %-20s %-10.2f %-12s%n",
                        rs.getInt("id"),
                        rs.getString("user_name"),
                        rs.getString("package_name"),
                        rs.getDouble("price"),
                        rs.getString("status"));
            }
        } catch (Exception e) {
            System.out.println("Read error: " + e.getMessage());
        }
    }

    // expose connection for SeedData to reuse the singleton's connection
    public Connection getConnection() {
        return conn;
    }
}
