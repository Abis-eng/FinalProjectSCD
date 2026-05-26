<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Patient Dashboard</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/fragments/navbar.jsp"/>
<jsp:include page="/WEB-INF/views/fragments/header.jsp"/>
<div class="container">
    <h2>Welcome, ${sessionScope.currentUser.fullName}</h2>
    <div class="grid-2">
        <div class="stat-card"><span>Today's Visits</span><strong>${stats.todayAppointments}</strong></div>
        <div class="stat-card"><span>Upcoming</span><strong>${stats.pendingAppointments}</strong></div>
        <div class="stat-card"><span>Unpaid Bills</span><strong>${stats.unpaidInvoices}</strong></div>
        <div class="stat-card"><span>Alerts</span><strong>${stats.unreadNotifications}</strong></div>
    </div>
    <div class="grid-2">
        <div class="card">
            <h3>My Doctor</h3>
            <p><strong>${profile.assignedDoctorName != null ? profile.assignedDoctorName : 'Not assigned yet'}</strong></p>
            <a href="${pageContext.request.contextPath}/patient/profile">View profile</a>
        </div>
        <div class="card">
            <h3>Quick Actions</h3>
            <a class="btn" href="${pageContext.request.contextPath}/appointments?action=new">Book Appointment</a>
            <a class="btn btn-secondary" href="${pageContext.request.contextPath}/patient/billing">Pay Bills</a>
            <a class="btn btn-secondary" href="${pageContext.request.contextPath}/patient/labs">Lab Results</a>
        </div>
    </div>
    <div class="card">
        <h3>My Appointments</h3>
        <table>
            <tr><th>Provider</th><th>Visit</th><th>Room</th><th>Date</th><th>Status</th><th></th></tr>
            <c:forEach var="a" items="${appointments}">
                <tr>
                    <td>${a.providerName}</td><td>${a.appointmentType}</td><td>${a.roomNumber}</td>
                    <td>${a.appointmentDate} ${a.timeSlot}</td><td>${a.status}</td>
                    <td><a href="${pageContext.request.contextPath}/appointments?action=view&id=${a.id}">Details</a></td>
                </tr>
            </c:forEach>
        </table>
    </div>
</div>
</body>
</html>
