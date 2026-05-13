import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class VerifySuperAdmin {
    public static void main(String[] args) throws Exception {
        String db = args.length > 0 ? args[0] : "seeded_football_academy_db";
        String url = "jdbc:mysql://localhost:3306/" + db + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        try (Connection c = DriverManager.getConnection(url, "root", "")) {
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT u.id, u.email, u.main_role, r.name AS role_name " +
                    "FROM users u " +
                    "LEFT JOIN user_roles ur ON ur.user_id = u.id " +
                    "LEFT JOIN roles r ON r.id = ur.role_id " +
                    "WHERE u.email = ?")) {
                ps.setString(1, "boufaiedmortadha7@gmail.com");
                try (ResultSet rs = ps.executeQuery()) {
                    System.out.println("DB=" + db);
                    while (rs.next()) {
                        System.out.println(
                                "id=" + rs.getLong("id")
                                + ", email=" + rs.getString("email")
                                + ", main_role=" + rs.getString("main_role")
                                + ", role=" + rs.getString("role_name"));
                    }
                }
            }
        }
    }
}
