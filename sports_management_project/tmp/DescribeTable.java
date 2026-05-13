import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DescribeTable {
    public static void main(String[] args) throws Exception {
        String db = args.length > 0 ? args[0] : "seeded_football_academy_db";
        String table = args.length > 1 ? args[1] : "user_roles";
        String url = "jdbc:mysql://localhost:3306/" + db + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        try (Connection c = DriverManager.getConnection(url, "root", "")) {
            System.out.println("DB=" + db + " TABLE=" + table);
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT column_name FROM information_schema.columns WHERE table_schema=? AND table_name=? ORDER BY ordinal_position")) {
                ps.setString(1, db);
                ps.setString(2, table);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        System.out.println(table + "." + rs.getString(1));
                    }
                }
            }
        }
    }
}
