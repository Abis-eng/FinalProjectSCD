<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page import="com.elcinic.model.Role" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Lab Tests</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/fragments/navbar.jsp"/>
<div class="container">
    <div class="card">
        <div class="toolbar">
            <h2>Laboratory Tests</h2>
            <% if (((com.elcinic.model.User)session.getAttribute("currentUser")).getRole() == Role.DOCTOR) { %>
                <a class="btn" href="${pageContext.request.contextPath}/labs?action=new">Order Test</a>
            <% } %>
        </div>
        <table>
            <tr><th>Patient</th><th>Test</th><th>Status</th><th>Result</th><th>Ordered</th><th></th></tr>
            <c:forEach var="t" items="${labTests}">
                <tr>
                    <td>${t.patientName}</td>
                    <td>${t.testName}</td>
                    <td><span class="badge badge-${t.status.name().toLowerCase()}">${t.status}</span></td>
                    <td>${t.resultValue} ${t.resultUnit}</td>
                    <td>${t.orderedDate}</td>
                    <td>
                        <% if (((com.elcinic.model.User)session.getAttribute("currentUser")).getRole() == Role.DOCTOR) { %>
                        <a class="btn btn-sm" href="${pageContext.request.contextPath}/labs?action=edit&id=${t.id}">Update</a>
                        <% } %>
                    </td>
                </tr>
            </c:forEach>
        </table>
    </div>
</div>
</body>
</html>
