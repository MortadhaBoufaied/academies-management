import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DescribeUsers {
    public static void main(String[] args) throws Exception {
        String db = args.length > 0 ? args[0] : "seeded_football_academy_db";
        String url = "jdbc:mysql://localhost:3306/" + db + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        try (Connection c = DriverManager.getConnection(url, "root", "")) {
            System.out.println("DB=" + db);
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT column_name FROM information_schema.columns WHERE table_schema=? AND table_name='users' ORDER BY ordinal_position")) {
                ps.setString(1, db);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        System.out.println("users." + rs.getString(1));
                    }
                }
            }
        }
    }
}
