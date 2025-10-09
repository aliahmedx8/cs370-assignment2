package demo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DBLocalTest {
    public static void main(String[] args) {
        // Change these to match your MySQL setup
        String url  = "jdbc:mysql://127.0.0.1:3306/SE?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        String user = "SE";
        String pass = "SE2020";

        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement st = conn.createStatement()) {
            System.out.println("✅ Connected to MySQL.");
            ResultSet rs = st.executeQuery("SELECT NOW() AS server_time, DATABASE() AS db");
            if (rs.next()) {
                System.out.println("DB: " + rs.getString("db"));
                System.out.println("MySQL time: " + rs.getString("server_time"));
            }
            rs.close();
        } catch (Exception e) {
            System.out.println("❌ Connection failed.");
            e.printStackTrace();
        }
    }
}
