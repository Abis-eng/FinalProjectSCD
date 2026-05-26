<%@ page contentType="text/html;charset=UTF-8" isErrorPage="true" %>
<%@ page import="com.elcinic.utility.ErrorMessages" %>
<%
    String displayError = (String) request.getAttribute("error");
    if (displayError == null || displayError.isBlank()) {
        Throwable ex = (Throwable) request.getAttribute("jakarta.servlet.error.exception");
        if (ex == null) {
            ex = (Throwable) request.getAttribute("javax.servlet.error.exception");
        }
        if (ex != null) {
            displayError = ErrorMessages.from(ex);
            request.getServletContext().log("Error page", ex);
        }
    }
    if (displayError == null || displayError.isBlank()) {
        displayError = "Please try again or contact support.";
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Error - E-Clinic</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="auth-page">
<div class="auth-card">
    <h1>Something went wrong</h1>
    <p class="error-msg"><%= displayError %></p>
    <a class="btn" href="${pageContext.request.contextPath}/login">Back to Login</a>
</div>
</body>
</html>
