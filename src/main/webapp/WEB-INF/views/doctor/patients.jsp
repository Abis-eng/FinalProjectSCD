<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>My Patients</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/fragments/navbar.jsp"/>
<div class="container page-enter">
    <div class="card">
        <h2>My Patients</h2>
        <form method="get" class="toolbar"><input name="q" placeholder="Search by name"><button class="btn btn-secondary">Search</button></form>
        <table>
            <tr><th>Name</th><th>Email</th><th>Phone</th><th>DOB</th><th>Blood</th></tr>
            <c:forEach var="p" items="${patients}">
                <tr>
                    <td><strong>${p.fullName}</strong></td>
                    <td>${p.email}</td>
                    <td>${p.phone != null ? p.phone : '—'}</td>
                    <td>${p.dateOfBirth != null ? p.dateOfBirth : '—'}</td>
                    <td>${p.bloodType != null ? p.bloodType : '—'}</td>
                </tr>
            </c:forEach>
        </table>
        <c:if test="${empty patients}"><p class="muted">No patients assigned to you yet.</p></c:if>
    </div>
</div>
</body>
</html>
