package demo;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/departments")
public class DepartmentsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/html; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        out.println("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Departments</title></head><body>");
        out.println("<h1>Departments</h1><ul>");

        final String sql =
            "SELECT d.id, d.name, COUNT(e.id) AS n " +
            "FROM dept d LEFT JOIN employee e ON e.dept_id = d.id " +
            "GROUP BY d.id, d.name ORDER BY d.name";

        try (Connection conn = demo.Db.get();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int n = rs.getInt("n");
                out.printf("<li><a href='%s/employees?dept=%d'>%s</a> (%d)</li>%n",
                           req.getContextPath(), id, html(name), n);
            }
        } catch (Exception e) {
            out.println("<pre style='color:red'>"); e.printStackTrace(out); out.println("</pre>");
        }

        // Simple add form (POST to /departments/add)
        out.printf("</ul><hr><h3>Add Department</h3>"
                 + "<form method='post' action='%s/departments/add'>"
                 + "<input name='name' required maxlength='100' placeholder='Dept name'> "
                 + "<button type='submit'>Add</button></form>",
                 req.getContextPath());

        out.printf("<p><a href='%s/'>Home</a></p>", req.getContextPath());
        out.println("</body></html>");
    }

    private static String html(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;")
                .replace(">","&gt;").replace("\"","&quot;");
    }
}
