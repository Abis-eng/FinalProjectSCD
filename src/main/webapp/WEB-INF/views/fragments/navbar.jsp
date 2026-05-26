<%@ page import="com.elcinic.model.Role" %>
<%
    com.elcinic.model.User u = (com.elcinic.model.User) session.getAttribute("currentUser");
    String ctx = request.getContextPath();
    int unread = 0;
    if (u != null) {
        try {
            unread = com.elcinic.service.ServiceFactory.notificationService().unreadCount(u.getId());
        } catch (Exception ignored) {}
    }
%>
<header class="topbar">
    <h1><a href="<%= ctx %>/login" style="color:#fff;text-decoration:none">E-Clinic Pro</a></h1>
    <nav>
        <% if (u != null) { %>
            <% if (u.getRole() == Role.ADMIN) { %>
                <a href="<%= ctx %>/admin/dashboard">Dashboard</a>
                <a href="<%= ctx %>/admin/users">Users</a>
                <a href="<%= ctx %>/admin/verify">Verify Staff</a>
                <a href="<%= ctx %>/admin/patients">Patients</a>
                <a href="<%= ctx %>/admin/appointments">Appointments</a>
                <a href="<%= ctx %>/admin/records">Records</a>
                <a href="<%= ctx %>/admin/labs">Labs</a>
                <a href="<%= ctx %>/admin/billing">Billing</a>
                <a href="<%= ctx %>/admin/reports">Reports</a>
            <% } else if (u.getRole() == Role.DOCTOR) { %>
                <a href="<%= ctx %>/doctor/dashboard">Dashboard</a>
                <a href="<%= ctx %>/doctor/appointments">Appointments</a>
                <a href="<%= ctx %>/doctor/patients">Patients</a>
                <a href="<%= ctx %>/doctor/records">Records</a>
                <a href="<%= ctx %>/doctor/labs">Labs</a>
            <% } else if (u.getRole() == Role.NURSE) { %>
                <a href="<%= ctx %>/nurse/dashboard">Dashboard</a>
                <a href="<%= ctx %>/nurse/appointments">Appointments</a>
            <% } else if (u.getRole() == Role.PATIENT) { %>
                <a href="<%= ctx %>/patient/dashboard">Dashboard</a>
                <a href="<%= ctx %>/patient/appointments">Appointments</a>
                <a href="<%= ctx %>/patient/records">Records</a>
                <a href="<%= ctx %>/patient/labs">Lab Results</a>
                <a href="<%= ctx %>/patient/billing">Billing</a>
                <a href="<%= ctx %>/patient/profile">My Doctor</a>
            <% } %>
            <a href="<%= ctx %>/notifications">Alerts<% if (unread > 0) { %> <span class="badge-count"><%= unread %></span><% } %></a>
            <a href="<%= ctx %>/profile">Settings</a>
            <span class="user-pill"><%= u.getFullName() %> · <%= u.getRole() %></span>
            <a href="<%= ctx %>/logout">Logout</a>
        <% } %>
    </nav>
</header>
