import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class CreateSuperAdmin {
    public static void main(String[] args) throws Exception {
        String db = args.length > 0 ? args[0] : "football_academy";
        String url = "jdbc:mysql://localhost:3306/" + db + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true";
        String user = "root";
        String pass = "";
        String email = "boufaiedmortadha7@gmail.com";
        String plainPassword = "admin123";
        String name = "Boufaied Mortadha";

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashed = encoder.encode(plainPassword);

        try (Connection c = DriverManager.getConnection(url, user, pass)) {
            c.setAutoCommit(false);

            boolean hasUsers = false;
            boolean hasRoles = false;
            boolean hasUserRoles = false;
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT table_name FROM information_schema.tables WHERE table_schema = ? AND table_name IN ('users','roles','user_roles')")) {
                ps.setString(1, db);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String t = rs.getString(1);
                        if ("users".equalsIgnoreCase(t)) hasUsers = true;
                        if ("roles".equalsIgnoreCase(t)) hasRoles = true;
                        if ("user_roles".equalsIgnoreCase(t)) hasUserRoles = true;
                    }
                }
            }
            if (!hasUsers || !hasRoles || !hasUserRoles) {
                System.out.println("SKIPPED DB " + db + " (missing users/roles/user_roles tables)");
                return;
            }

            // Ensure enum contains SUPER_ADMIN for legacy schemas.
            try (Statement st = c.createStatement()) {
                st.execute("ALTER TABLE users MODIFY COLUMN main_role ENUM('SUPER_ADMIN','ADMIN','PLAYER','PARENT','TRAINER','SCOUTER') NOT NULL");
            } catch (Exception ignored) {
                // If the column type is not enum or already compatible, keep going.
            }

            Set<String> userCols = new HashSet<>();
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT column_name FROM information_schema.columns WHERE table_schema=? AND table_name='users'")) {
                ps.setString(1, db);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        userCols.add(rs.getString(1).toLowerCase());
                    }
                }
            }

            Long roleId = null;
            try (PreparedStatement ps = c.prepareStatement("SELECT id FROM roles WHERE UPPER(name)=UPPER('SUPER_ADMIN') LIMIT 1");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) roleId = rs.getLong(1);
            }
            if (roleId == null) {
                try (PreparedStatement ps = c.prepareStatement("INSERT INTO roles(name) VALUES ('SUPER_ADMIN')", PreparedStatement.RETURN_GENERATED_KEYS)) {
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) roleId = rs.getLong(1);
                    }
                }
            }

            Long userId = null;
            try (PreparedStatement ps = c.prepareStatement("SELECT id FROM users WHERE email=? LIMIT 1")) {
                ps.setString(1, email);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) userId = rs.getLong(1);
                }
            }

            if (userId == null) {
                StringBuilder cols = new StringBuilder("nom,email,mdp,main_role");
                StringBuilder vals = new StringBuilder("?,?,?,'SUPER_ADMIN'");
                if (userCols.contains("active")) {
                    cols.append(",active");
                    vals.append(",1");
                }
                if (userCols.contains("registration_date")) {
                    cols.append(",registration_date");
                    vals.append(",NOW()");
                }
                if (userCols.contains("login_count")) {
                    cols.append(",login_count");
                    vals.append(",0");
                }
                if (userCols.contains("created_at")) {
                    cols.append(",created_at");
                    vals.append(",CURDATE()");
                }

                String sql = "INSERT INTO users(" + cols + ") VALUES (" + vals + ")";
                try (PreparedStatement ps = c.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, name);
                    ps.setString(2, email);
                    ps.setString(3, hashed);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) userId = rs.getLong(1);
                    }
                }
                System.out.println("Created user: " + email);
            } else {
                StringBuilder sql = new StringBuilder("UPDATE users SET nom=?, mdp=?, main_role='SUPER_ADMIN'");
                if (userCols.contains("active")) {
                    sql.append(", active=1");
                }
                if (userCols.contains("login_count")) {
                    sql.append(", login_count=COALESCE(login_count,0)");
                }
                if (userCols.contains("registration_date")) {
                    sql.append(", registration_date=COALESCE(registration_date,NOW())");
                }
                if (userCols.contains("created_at")) {
                    sql.append(", created_at=COALESCE(created_at,CURDATE())");
                }
                sql.append(" WHERE id=?");
                try (PreparedStatement ps = c.prepareStatement(sql.toString())) {
                    ps.setString(1, name);
                    ps.setString(2, hashed);
                    ps.setLong(3, userId);
                    ps.executeUpdate();
                }
                System.out.println("Updated existing user: " + email);
            }

            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT IGNORE INTO user_roles(user_id, role_id) VALUES (?, ?)")) {
                ps.setLong(1, userId);
                ps.setLong(2, roleId);
                ps.executeUpdate();
            }

            c.commit();
            System.out.println("SUPER_ADMIN ready for " + email + " in DB " + db);
        }
    }
}
