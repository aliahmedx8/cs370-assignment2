package demo;

import java.io.IOException;
import java.sql.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/departments/add")
public class AddDepartmentServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String name = req.getParameter("name");
        if (name != null) name = name.trim();

        if (name == null || name.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/departments");
            return;
        }

        String url  = "jdbc:mysql://127.0.0.1:3306/SE?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        String user = "SE";
        String pass = "SE2020";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(url, user, pass);
                 PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO dept(name) VALUES (?)")) {
                ps.setString(1, name);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            // For a real app you'd log an error; for class demo we ignore and redirect
        }

        resp.sendRedirect(req.getContextPath() + "/departments");
    }
}
