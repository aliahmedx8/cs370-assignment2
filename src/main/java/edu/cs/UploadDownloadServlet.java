package edu.cs;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import demo.Db;

@WebServlet("/upload/download")
public class UploadDownloadServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    int id = 0;
    try { id = Integer.parseInt(req.getParameter("id")); } catch (Exception ignore) {}
    if (id <= 0) {
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      resp.setContentType("text/html; charset=UTF-8");
      try (PrintWriter out = resp.getWriter()) {
        out.println("<p>Missing or bad id.</p>");
      }
      return;
    }

    try (Connection c = Db.getConnection();
         PreparedStatement ps = c.prepareStatement(
           "SELECT filename, content FROM uploads WHERE id=?")) {
      ps.setInt(1, id);

      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) {
          resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
          resp.setContentType("text/html; charset=UTF-8");
          try (PrintWriter out = resp.getWriter()) {
            out.println("<p>Not found.</p>");
          }
          return;
        }

        String filename = rs.getString("filename");
        if (filename == null || filename.isBlank()) filename = "download.txt";
        // basic filename safety
        filename = filename.replaceAll("[\\\\/\\r\\n\\t\"]", "_");

        String content = rs.getString("content");
        if (content == null) content = "";

        // Stream as a downloadable text file
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/plain; charset=UTF-8");
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8.name())
                                   .replace("+", "%20");
        resp.setHeader(
          "Content-Disposition",
          "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encoded
        );

        resp.getOutputStream().write(content.getBytes(StandardCharsets.UTF_8));
      }
    } catch (SQLException e) {
      resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      resp.setContentType("text/html; charset=UTF-8");
      try (PrintWriter out = resp.getWriter()) {
        out.println("<p><b>DB error:</b> " + e.getMessage() + "</p>");
      }
    }
  }
}
