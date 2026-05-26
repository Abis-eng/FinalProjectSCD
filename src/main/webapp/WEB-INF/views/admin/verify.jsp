<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Staff Verification - E-Clinic</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/fragments/navbar.jsp"/>
<jsp:include page="/WEB-INF/views/fragments/header.jsp"/>
<div class="container">
    <h2>Pending Staff Registrations</h2>
    <p>Review doctor and nurse sign-ups. Approved accounts can log in immediately; declined accounts are notified with your reason.</p>

    <c:if test="${empty pending}">
        <div class="card" style="margin-top:1rem">
            <p>No pending registrations.</p>
        </div>
    </c:if>

    <c:forEach var="p" items="${pending}">
        <div class="card" style="margin-top:1rem">
            <h3>${p.fullName} <span class="badge">${p.role}</span></h3>
            <table class="detail-table">
                <tr><th>Username</th><td>${p.username}</td></tr>
                <tr><th>Email</th><td>${p.email}</td></tr>
                <tr><th>Phone</th><td>${p.phone}</td></tr>
                <tr><th>Registered</th><td>${p.registeredAt}</td></tr>
                <c:if test="${p.role == 'DOCTOR'}">
                    <tr><th>Specialization</th><td>${p.specialization}</td></tr>
                    <tr><th>License</th><td>${p.licenseNumber}</td></tr>
                </c:if>
                <c:if test="${p.role == 'NURSE'}">
                    <tr><th>Department</th><td>${p.department}</td></tr>
                </c:if>
            </table>
            <div style="display:flex;gap:1rem;margin-top:1rem;flex-wrap:wrap;align-items:flex-end">
                <form method="post" action="${pageContext.request.contextPath}/admin/verify" style="display:inline">
                    <input type="hidden" name="userId" value="${p.userId}">
                    <input type="hidden" name="action" value="approve">
                    <button type="submit" class="btn">Approve</button>
                </form>
                <form method="post" action="${pageContext.request.contextPath}/admin/verify" class="reject-form" style="flex:1;min-width:240px">
                    <input type="hidden" name="userId" value="${p.userId}">
                    <input type="hidden" name="action" value="reject">
                    <div class="form-group" style="margin:0">
                        <label>Decline reason</label>
                        <input name="reason" required placeholder="e.g. Invalid license number">
                    </div>
                    <button type="submit" class="btn btn-danger" style="margin-top:.5rem">Decline</button>
                </form>
            </div>
        </div>
    </c:forEach>
</div>
</body>
</html>
