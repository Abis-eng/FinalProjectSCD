<%@ page contentType="text/html;charset=UTF-8" %>
<%
    if (session.getAttribute("currentUser") != null) {
        response.sendRedirect(request.getContextPath() + "/login");
    } else {
        response.sendRedirect(request.getContextPath() + "/login");
    }
%>
