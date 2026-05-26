<%
    String flashType = (String) session.getAttribute("flashType");
    String flashMessage = (String) session.getAttribute("flashMessage");
    if (flashMessage != null) {
        session.removeAttribute("flashType");
        session.removeAttribute("flashMessage");
    }
%>
<% if (flashMessage != null) { %>
<div class="container">
    <div class="flash <%= "success".equals(flashType) ? "flash-success" : "flash-error" %>"><%= flashMessage %></div>
</div>
<% } %>
