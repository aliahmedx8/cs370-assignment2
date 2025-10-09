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

@WebServlet("/employees")
public class EmployeesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/html; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        String deptParam = req.getParameter("dept"); // may be null or ""
        Integer deptId = null;
        try {
            if (deptParam != null && !deptParam.isBlank()) {
                deptId = Integer.valueOf(deptParam);
            }
        } catch (NumberFormatException ignore) { /* treat as 'all' */ }

        // 1) Load departments for the dropdown
        final String deptSql = "SELECT id, name FROM dept ORDER BY name";
        // 2) Load employees (optionally filtered)
        final String empSqlAll =
            "SELECT e.name AS emp, d.name AS dept " +
            "FROM employee e JOIN dept d ON d.id = e.dept_id " +
            "ORDER BY d.name, e.name";
        final String empSqlByDep =
            "SELECT e.name AS emp, d.name AS dept " +
            "FROM employee e JOIN dept d ON d.id = e.dept_id " +
            "WHERE d.id = ? " +
            "ORDER BY e.name";

        try (Connection conn = demo.Db.get()) {

            out.println("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Employees</title></head><body>");
            out.println("<h1>Employees</h1>");

            // Dropdown filter form
            out.println("<form method='get' action='" + req.getContextPath() + "/employees'>");
            out.println("<label for='dept'>Department: </label>");
            out.println("<select name='dept' id='dept'>");
            out.println("<option value=''>All</option>");

            try (PreparedStatement ps = conn.prepareStatement(deptSql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    String selected = (deptId != null && deptId == id) ? " selected" : "";
                    out.println("<option value='" + id + "'" + selected + ">" + esc(name) + "</option>");
                }
            }

            out.println("</select>");
            out.println("<button type='submit'>Filter</button>");
            out.println("</form>");
            out.println("<hr/>");

            // Employee list
            out.println("<ul>");
            if (deptId == null) {
                try (PreparedStatement ps = conn.prepareStatement(empSqlAll);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        out.println("<li>" + esc(rs.getString("emp")) + " (dept: " + esc(rs.getString("dept")) + ")</li>");
                    }
                }
            } else {
                try (PreparedStatement ps = conn.prepareStatement(empSqlByDep)) {
                    ps.setInt(1, deptId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            out.println("<li>" + esc(rs.getString("emp")) + " (dept: " + esc(rs.getString("dept")) + ")</li>");
                        }
                    }
                }
            }
            out.println("</ul>");

            out.println("<p><a href='" + req.getContextPath() + "/'>Home</a></p>");
            out.println("</body></html>");

        } catch (Exception e) {
            resp.setStatus(500);
            out.println("<pre style='color:red'>");
            e.printStackTrace(out);
            out.println("</pre>");
        }
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;")
                .replace("<","&lt;")
                .replace(">","&gt;")
                .replace("\"","&quot;");
    }
}
