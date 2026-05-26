<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page import="com.elcinic.model.Role" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Medical Records - E-Clinic</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/fragments/navbar.jsp"/>
<jsp:include page="/WEB-INF/views/fragments/header.jsp"/>
<%
    com.elcinic.model.User u = (com.elcinic.model.User) session.getAttribute("currentUser");
%>
<div class="container">
    <div class="card">
        <h2>Medical Records</h2>
        <% if (u.getRole() == Role.DOCTOR || u.getRole() == Role.ADMIN) { %>
            <a class="btn" href="${pageContext.request.contextPath}/records?action=new">Add Record</a>
        <% } %>
        <table style="margin-top:1rem">
            <thead>
            <tr><th>Date</th><th>Patient</th><th>Doctor</th><th>Diagnosis</th><th>Prescription</th><th>Notes</th></tr>
            </thead>
            <tbody>
            <c:forEach var="r" items="${records}">
                <tr>
                    <td>${r.visitDate}</td>
                    <td>${r.patientName}</td>
                    <td>${r.doctorName}</td>
                    <td>${r.diagnosis}</td>
                    <td>${r.prescription}</td>
                    <td>${r.notes}</td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
        <c:if test="${empty records}"><p>No medical records.</p></c:if>
    </div>
</div>
</body>
</html>
