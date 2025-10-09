package demo;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

@WebServlet("/tables")
public class TablesServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        out.println("<!DOCTYPE html><html><head><title>Tables</title></head><body>");
        out.println("<h2>Tables in SE</h2><ul>");

        String url  = "jdbc:mysql://127.0.0.1:3306/SE?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        String user = "SE";
        String pass = "SE2020";

        try {
            // IMPORTANT: load MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            try (Connection conn = DriverManager.getConnection(url, user, pass);
                 Statement st   = conn.createStatement();
                 ResultSet rs   = st.executeQuery("SHOW TABLES")) {

                while (rs.next()) out.println("<li>" + rs.getString(1) + "</li>");
            }
        } catch (Exception e) {
            out.println("<pre style='color:red'>");
            e.printStackTrace(out);
            out.println("</pre>");
        }

        out.println("</ul><p><a href='" + req.getContextPath() + "/'>Home</a></p>");
        out.println("</body></html>");
    }
}
