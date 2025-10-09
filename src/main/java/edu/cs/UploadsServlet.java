package edu.cs;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import demo.Db;   // your DB helper

@WebServlet("/uploads")
public class UploadsServlet extends HttpServlet {

  private static String esc(String s) {
    return (s == null) ? "" : s.replace("&","&amp;")
                               .replace("<","&lt;")
                               .replace(">","&gt;");
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    resp.setContentType("text/html; charset=UTF-8");

    String q = req.getParameter("q");
    if (q != null) q = q.trim();
    boolean hasQuery = (q != null && !q.isEmpty());

    int page = 1;
    try { page = Integer.parseInt(req.getParameter("page")); } catch (Exception ignore) {}
    if (page < 1) page = 1;

    final int LIMIT = 10;
    int offset = (page - 1) * LIMIT;

    try (PrintWriter out = resp.getWriter()) {
      out.println("<!doctype html><html><head><meta charset='utf-8'><title>Recent uploads</title></head><body>");
      out.println("<h1>Recent uploads</h1>");

      // Search box
      out.println("<form method='get' action='uploads'>");
      out.println("<input type='text' name='q' value='" + esc(q == null ? "" : q) + "' />");
      out.println("<button type='submit'>Search</button> ");
      if (hasQuery) out.println("<a href='uploads'>Clear</a>");
      out.println("</form>");

      // SQL with optional filter + pagination (ask LIMIT+1 to detect 'next' page)
      String sql =
          "SELECT id, filename, LEFT(content,80) AS preview, uploaded_at " +
          "FROM uploads " +
          (hasQuery ? "WHERE filename LIKE ? OR content LIKE ? " : "") +
          "ORDER BY id DESC " +
          "LIMIT ? OFFSET ?";

      try (Connection c = Db.getConnection();
           PreparedStatement ps = c.prepareStatement(sql)) {

        int idx = 1;
        if (hasQuery) {
          String like = "%" + q + "%";
          ps.setString(idx++, like);
          ps.setString(idx++, like);
        }
        ps.setInt(idx++, LIMIT + 1); // request one extra to know if 'Next' exists
        ps.setInt(idx++, offset);

        try (ResultSet rs = ps.executeQuery()) {
          int printed = 0;
          boolean hasMore = false;

          out.println("<ul>");
          while (rs.next()) {
            if (printed >= LIMIT) {    // we fetched one extra row => there is a next page
              hasMore = true;
              break;
            }
            int id = rs.getInt("id");
            String filename = rs.getString("filename");
            String preview  = rs.getString("preview");
            Timestamp ts    = rs.getTimestamp("uploaded_at");

            out.println("<li>#"+ id +" <a href='upload/view?id="+ id +"'>"
                + esc(filename) + "</a> — " + esc(preview)
                + " <em>(" + ts + ")</em></li>");
            printed++;
          }
          out.println("</ul>");

          if (printed == 0) {
            out.println("<p>No results.</p>");
          }

          // Prev / Next links (preserve q if present)
          String base = "uploads";
          String qs = hasQuery ? ("q=" + URLEncoder.encode(q, StandardCharsets.UTF_8) + "&") : "";
          out.print("<p>");
          if (page > 1) {
            out.print("<a href='" + base + "?" + qs + "page=" + (page - 1) + "'>Prev</a>");
            if (hasMore) out.print(" | ");
          }
          if (hasMore) {
            out.print("<a href='" + base + "?" + qs + "page=" + (page + 1) + "'>Next</a>");
          }
          out.println("</p>");
        }
      } catch (SQLException e) {
        out.println("<p><b>DB error:</b> " + esc(e.getMessage()) + "</p>");
      }

      out.println("<p><a href='index.html'>Home</a></p>");
      out.println("</body></html>");
    }
  }
}
