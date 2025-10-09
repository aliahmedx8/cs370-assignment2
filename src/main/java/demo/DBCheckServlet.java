package demo;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/dbcheck")
public class DBCheckServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/plain; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        PrintWriter out = resp.getWriter();

        try (
            Connection conn = demo.Db.get();                           // uses your central Db.java
            Statement  st   = conn.createStatement();
            ResultSet  rs   = st.executeQuery(
                "SELECT NOW() AS server_time, DATABASE() AS db, @@hostname AS host")
        ) {
            if (rs.next()) {
                out.println("Connected \u2714"); // ✔
                out.println("DB: " + rs.getString("db"));
                out.println("Host: " + rs.getString("host"));          // should show Iqbal's host
                out.println("MySQL time: " + rs.getString("server_time"));
            }
        } catch (Exception e) {
            resp.setStatus(500);
            e.printStackTrace(out);
        }
    }
}
