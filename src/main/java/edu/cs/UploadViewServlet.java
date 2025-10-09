package edu.cs;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import demo.Db;  // uses your Db.getConnection()

@WebServlet("/upload/view")   // must match links on /uploads page
public class UploadViewServlet extends HttpServlet {
  private static String esc(String s){
    return s == null ? "" : s.replace("&","&amp;")
                             .replace("<","&lt;")
                             .replace(">","&gt;");
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    resp.setContentType("text/html; charset=UTF-8");

    int id = 0;
    try { id = Integer.parseInt(req.getParameter("id")); } catch (Exception ignore) {}

    try (PrintWriter out = resp.getWriter()) {
      out.println("<!doctype html><html><head><meta charset='utf-8'><title>View upload</title></head><body>");

      if (id <= 0) {
        out.println("<p>Missing or bad id.</p><p><a href='../../uploads'>Back</a></p>");
        out.println("</body></html>");
        return;
      }

      try (Connection c = Db.getConnection();
           PreparedStatement ps = c.prepareStatement(
               "SELECT filename, content, uploaded_at FROM uploads WHERE id=?")) {
        ps.setInt(1, id);
        try (ResultSet rs = ps.executeQuery()) {
          if (rs.next()) {
            out.println("<h1>" + esc(rs.getString("filename")) + "</h1>");
            out.println("<p><em>" + rs.getTimestamp("uploaded_at") + "</em></p>");
            // Download link for this upload (assumes /upload/download servlet exists)
            out.println("<p><a href='download?id=" + id + "'>Download</a></p>");
            out.println("<pre>" + esc(rs.getString("content")) + "</pre>");
          } else {
            out.println("<p>Not found.</p>");
          }
        }
      } catch (SQLException e) {
        out.println("<p><b>DB error:</b> " + esc(e.getMessage()) + "</p>");
      }

      out.println("<p><a href='../../uploads'>Back</a> | <a href='../../index.html'>Home</a></p>");
      out.println("</body></html>");
    }
  }
}
