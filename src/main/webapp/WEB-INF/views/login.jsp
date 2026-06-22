<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Login - E-Clinic</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="auth-page">
<div class="auth-card">
    <h1>E-Clinic</h1>
    <p style="text-align:center;color:#6b7280;margin-bottom:1.5rem">Patient, Doctor, Nurse & Admin Portal</p>
    <%
        String flashType = (String) session.getAttribute("flashType");
        String flashMessage = (String) session.getAttribute("flashMessage");
        if (flashMessage != null) {
            session.removeAttribute("flashType");
            session.removeAttribute("flashMessage");
        }
    %>
    <% if (flashMessage != null) { %>
        <p class="<%= "success".equals(flashType) ? "success-msg" : "error-msg" %>"><%= flashMessage %></p>
    <% } %>
    <% if (request.getAttribute("error") != null) { %>
        <p class="error-msg"><%= request.getAttribute("error") %></p>
    <% } %>
    <form method="post" action="${pageContext.request.contextPath}/login">
        <div class="form-group">
            <label>Username</label>
            <input type="text" name="username" required>
        </div>
        <div class="form-group">
            <label>Password</label>
            <input type="password" name="password" required>
        </div>
        <button type="submit" class="btn" style="width:100%">Login</button>
    </form>
    <p style="text-align:center;margin-top:1rem">
        <a href="${pageContext.request.contextPath}/register">Create an account</a> (patient, doctor, or nurse)
    </p>
</div>
</body>
</html>
