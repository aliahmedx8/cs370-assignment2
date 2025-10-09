<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <title>DBTest_Demo</title>
  </head>
  <body>
    <h2>It works 🎉</h2>
    <p>Server time: <%= new java.util.Date() %></p>

    <hr />
    <ul>
      <li><a href="${pageContext.request.contextPath}/dbcheck">Run DB check</a></li>
      <li><a href="${pageContext.request.contextPath}/tables">List tables</a></li>
      <li><a href="${pageContext.request.contextPath}/employees">List employees</a></li>
    </ul>
  </body>
</html>
