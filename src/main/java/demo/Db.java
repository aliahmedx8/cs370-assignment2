package demo;

public final class Db {
  static {
    try { Class.forName("com.mysql.cj.jdbc.Driver"); } catch (Exception ignore) {}
  }

  // ✅ Level 3: Laptop → Server EC2 (Public IP, No SSL)
  private static final String URL =
      "jdbc:mysql://18.189.21.239:3306/SE"
    + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";
  private static final String USER = "SE";
  private static final String PASS = "SE2020";

  public static java.sql.Connection getConnection() throws java.sql.SQLException {
    return java.sql.DriverManager.getConnection(URL, USER, PASS);
  }

  // keep compatibility if other code calls Db.get()
  public static java.sql.Connection get() throws java.sql.SQLException {
    return getConnection();
  }
}
