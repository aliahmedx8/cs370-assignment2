package edu.cs;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import demo.Db; // Db.getConnection()

@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 10,   // 10 MB
    maxFileSize       = 1024 * 1024 * 50,   // 50 MB
    maxRequestSize    = 1024 * 1024 * 100   // 100 MB
)
public class FileUploadServlet extends HttpServlet {

  private static final long serialVersionUID = 205242440643911308L;

  // Save OUTSIDE the webapp so redeploys don't wipe files
  private static final String UPLOAD_DIR = "C:/workspace/uploads";

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    request.setCharacterEncoding("UTF-8");
    response.setContentType("text/html; charset=UTF-8");

    // Ensure save folder exists
    Path uploadDir = Paths.get(UPLOAD_DIR);
    Files.createDirectories(uploadDir);

    String fileName = "";
    Part filePart = null;

    for (Part p : request.getParts()) {
      String submitted = getFileName(p);
      if (submitted != null && !submitted.isEmpty()) {
        fileName = Paths.get(submitted).getFileName().toString();
        filePart = p;
        break;
      }
    }

    try (PrintWriter out = response.getWriter()) {
      out.println("<!doctype html><html><head><meta charset='utf-8'><title>Result</title></head><body>");
      out.println("<h1>Result</h1>");

      if (filePart == null || fileName.isEmpty()) {
        out.println("<p><b>File:</b> (no file selected)</p>");
        nav(out);
        out.println("</body></html>");
        return;
      }

      // Save the file
      Path target = uploadDir.resolve(fileName);
      filePart.write(target.toString());

      // Read back as text
      String content = "";
      if (Files.exists(target)) {
        byte[] bytes = Files.readAllBytes(target);
        content = new String(bytes, StandardCharsets.UTF_8);
      }

      out.println("<p><b>File:</b> " + esc(fileName) + "</p>");

      // --- Block empty uploads (0 bytes or only whitespace) ---
      if (content.trim().isEmpty()) {
        out.println("<p><b>DB:</b> Skipped — file is empty.</p>");
        nav(out);
        out.println("</body></html>");
        return; // stop here: no DB insert
      }

      // Save into DB
      try (Connection conn = Db.getConnection();
           PreparedStatement ps = conn.prepareStatement(
               "INSERT INTO uploads(filename, content) VALUES (?, ?)")) {
        ps.setString(1, fileName);
        ps.setString(2, content);
        ps.executeUpdate();
        out.println("<p><b>DB:</b> Saved.</p>");
      } catch (SQLException e) {
        out.println("<p><b>DB:</b> Insert failed: " + esc(e.getMessage()) + "</p>");
      }

      // Show preview
      out.println("<pre>" + esc(content) + "</pre>");
      nav(out);
      out.println("</body></html>");
    }
  }

  // Quick nav links shown on the result page
  private void nav(PrintWriter out) {
    out.println("<p><a href='uploads'>View recent uploads</a> | "
        + "<a href='upload.html'>Upload another</a> | "
        + "<a href='index.html'>Home</a></p>");
  }

  // Extract original filename from part header
  private String getFileName(Part part) {
    String cd = part.getHeader("content-disposition");
    if (cd == null) return "";
    for (String token : cd.split(";")) {
      token = token.trim();
      if (token.startsWith("filename")) {
        return token.substring(token.indexOf('=') + 1).trim().replace("\"", "");
      }
    }
    return "";
  }

  private String esc(String s) {
    if (s == null) return "";
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }
}
