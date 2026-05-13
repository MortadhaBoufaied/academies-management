import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class ShowCreate {
    public static void main(String[] args) throws Exception {
        String db = args.length > 0 ? args[0] : "seeded_football_academy_db";
        String table = args.length > 1 ? args[1] : "users";
        String url = "jdbc:mysql://localhost:3306/" + db + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        try (Connection c = DriverManager.getConnection(url, "root", "");
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SHOW CREATE TABLE " + table)) {
            if (rs.next()) {
                System.out.println(rs.getString(2));
            }
        }
    }
}
