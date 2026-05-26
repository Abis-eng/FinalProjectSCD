<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="m" tagdir="/WEB-INF/tags" %>
<%@ page import="com.elcinic.model.Role" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Appointments - E-Clinic</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/fragments/navbar.jsp"/>
<jsp:include page="/WEB-INF/views/fragments/header.jsp"/>
<%
    com.elcinic.model.User u = (com.elcinic.model.User) session.getAttribute("currentUser");
    String bookUrl = request.getContextPath() + "/appointments?action=new";
%>
<div class="container">
    <div class="card">
        <h2>Appointments</h2>
        <div class="toolbar">
            <% if (u.getRole() == Role.PATIENT || u.getRole() == Role.ADMIN) { %>
                <a class="btn" href="<%= bookUrl %>">Book Appointment</a>
            <% } %>
            <% if (u.getRole() == Role.ADMIN) { %>
            <form method="get" style="display:flex;gap:.5rem;flex-wrap:wrap">
                <input type="text" name="q" placeholder="Search..." value="${param.q}">
                <select name="status">
                    <option value="">All statuses</option>
                    <option>PENDING</option><option>CONFIRMED</option>
                    <option>COMPLETED</option><option>CANCELLED</option>
                </select>
                <button class="btn btn-secondary" type="submit">Filter</button>
            </form>
            <% } %>
        </div>
        <table>
            <thead>
            <tr>
                <th>ID</th><th>Patient</th><th>Provider</th><th>Visit</th><th>Room</th>
                <th>Date</th><th>Time</th><th>Status</th><th>Fee</th><th>Actions</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="a" items="${appointments}">
                <tr>
                    <td>${a.id}</td>
                    <td>${a.patientName}</td>
                    <td>${a.providerName}</td>
                    <td>${a.appointmentType} · ${a.priority}</td>
                    <td>${a.roomNumber}</td>
                    <td>${a.appointmentDate}</td>
                    <td>${a.timeSlot}</td>
                    <td><span class="badge badge-${a.status.name().toLowerCase()}">${a.status}</span></td>
                    <td><m:formatMoney amount="${a.feeAmount}"/></td>
                    <td>
                        <a class="btn btn-sm" href="${pageContext.request.contextPath}/appointments?action=view&id=${a.id}">View</a>
                        <% if (u.getRole() != Role.PATIENT) { %>
                        <form class="inline-form" method="post" action="${pageContext.request.contextPath}/appointments">
                            <input type="hidden" name="action" value="status">
                            <input type="hidden" name="id" value="${a.id}">
                            <select name="status" onchange="this.form.submit()">
                                <option value="PENDING" ${a.status.name()=='PENDING'?'selected':''}>PENDING</option>
                                <option value="CONFIRMED" ${a.status.name()=='CONFIRMED'?'selected':''}>CONFIRMED</option>
                                <option value="COMPLETED" ${a.status.name()=='COMPLETED'?'selected':''}>COMPLETED</option>
                                <option value="CANCELLED" ${a.status.name()=='CANCELLED'?'selected':''}>CANCELLED</option>
                            </select>
                        </form>
                        <% } else { %>
                        <form class="inline-form" method="post" action="${pageContext.request.contextPath}/appointments">
                            <input type="hidden" name="action" value="status">
                            <input type="hidden" name="id" value="${a.id}">
                            <input type="hidden" name="status" value="CANCELLED">
                            <button class="btn btn-sm btn-danger" type="submit">Cancel</button>
                        </form>
                        <% } %>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
        <c:if test="${empty appointments}"><p>No appointments found.</p></c:if>
    </div>
</div>
</body>
</html>
