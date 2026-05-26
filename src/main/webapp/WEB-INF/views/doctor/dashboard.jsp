<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Doctor Dashboard</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/fragments/navbar.jsp"/>
<jsp:include page="/WEB-INF/views/fragments/header.jsp"/>
<div class="container">
    <h2>Doctor Workspace</h2>
    <div class="grid-2">
        <div class="stat-card"><span>Today</span><strong>${stats.todayAppointments}</strong></div>
        <div class="stat-card"><span>Queue</span><strong>${stats.pendingAppointments}</strong></div>
    </div>
    <div class="card">
        <h3>Upcoming Appointments</h3>
        <table>
            <tr><th>Patient</th><th>Symptoms</th><th>Room</th><th>Time</th><th>Priority</th><th>Status</th><th></th></tr>
            <c:forEach var="a" items="${appointments}">
                <tr>
                    <td>${a.patientName}</td><td>${a.symptoms}</td><td>${a.roomNumber}</td>
                    <td>${a.appointmentDate} ${a.timeSlot}</td>
                    <td><span class="badge badge-${a.priority.name().toLowerCase()}">${a.priority}</span></td>
                    <td>${a.status}</td>
                    <td><a href="${pageContext.request.contextPath}/appointments?action=view&id=${a.id}">Open</a></td>
                </tr>
            </c:forEach>
        </table>
    </div>
</div>
</body>
</html>
