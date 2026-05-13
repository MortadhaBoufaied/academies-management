import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class ListDatabases {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        try (Connection c = DriverManager.getConnection(url, "root", "");
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SHOW DATABASES")) {
            while (rs.next()) {
                System.out.println(rs.getString(1));
            }
        }
    }
}
