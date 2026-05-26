<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Nurse Dashboard</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/fragments/navbar.jsp"/>
<jsp:include page="/WEB-INF/views/fragments/header.jsp"/>
<div class="container">
    <h2>Nurse Dashboard</h2>
    <div class="card">
        <h3>My Appointments</h3>
        <a class="btn" href="${pageContext.request.contextPath}/appointments?action=new">View booking page</a>
        <table style="margin-top:1rem">
            <tr><th>Patient</th><th>Date</th><th>Time</th><th>Status</th></tr>
            <c:forEach var="a" items="${appointments}">
                <tr>
                    <td>${a.patientName}</td><td>${a.appointmentDate}</td>
                    <td>${a.timeSlot}</td><td>${a.status}</td>
                </tr>
            </c:forEach>
        </table>
    </div>
</div>
</body>
</html>
