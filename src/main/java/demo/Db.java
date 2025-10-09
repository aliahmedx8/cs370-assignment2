package demo;

public final class Db {
  static {
    try { Class.forName("com.mysql.cj.jdbc.Driver"); } catch (Exception ignore) {}
  }

  private static final String URL =
      "jdbc:mysql://192.168.1.183:3306/se"
    + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";
  private static final String USER = "se_app";
  private static final String PASS = "se_app_123";

  public static java.sql.Connection getConnection() throws java.sql.SQLException {
    return java.sql.DriverManager.getConnection(URL, USER, PASS);
  }

  // keep compatibility if other code calls Db.get()
  public static java.sql.Connection get() throws java.sql.SQLException {
    return getConnection();
  }
}
