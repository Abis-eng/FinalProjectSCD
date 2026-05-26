<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Patients</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/fragments/navbar.jsp"/>
<jsp:include page="/WEB-INF/views/fragments/header.jsp"/>
<div class="container page-enter">
    <div class="card">
        <h2>All Patients</h2>
        <form class="toolbar" method="get"><input name="q" placeholder="Search by name"><button class="btn btn-secondary">Search</button></form>
        <table>
            <tr><th>Name</th><th>Email</th><th>Phone</th><th>DOB</th><th>Blood</th><th>Doctor</th><th>Assign</th></tr>
            <c:forEach var="p" items="${patients}">
                <tr>
                    <td><strong>${p.fullName}</strong></td>
                    <td>${p.email}</td>
                    <td>${p.phone != null ? p.phone : '—'}</td>
                    <td>${p.dateOfBirth != null ? p.dateOfBirth : '—'}</td>
                    <td>${p.bloodType != null ? p.bloodType : '—'}</td>
                    <td>${p.assignedDoctorName != null ? p.assignedDoctorName : '—'}</td>
                    <td>
                        <form method="post" class="inline-form" action="${pageContext.request.contextPath}/admin/patients">
                            <input type="hidden" name="action" value="assign">
                            <input type="hidden" name="patientId" value="${p.userId}">
                            <select name="doctorId" required>
                                <c:forEach var="d" items="${doctors}">
                                    <option value="${d.userId}">${d.fullName}</option>
                                </c:forEach>
                            </select>
                            <button class="btn btn-sm" type="submit">Assign</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
        </table>
        <c:if test="${empty patients}"><p class="muted">No patients registered yet.</p></c:if>
    </div>
</div>
</body>
</html>
