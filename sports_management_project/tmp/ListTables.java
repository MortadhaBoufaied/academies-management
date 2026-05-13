import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class ListTables {
    public static void main(String[] args) throws Exception {
        String db = args.length > 0 ? args[0] : "football_academy";
        String url = "jdbc:mysql://localhost:3306/" + db + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true";
        try (Connection c = DriverManager.getConnection(url, "root", "");
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SHOW TABLES")) {
            int i = 0;
            while (rs.next()) {
                i++;
                System.out.println(rs.getString(1));
            }
            System.out.println("DB=" + db);
            System.out.println("TOTAL_TABLES=" + i);
        }
    }
}
