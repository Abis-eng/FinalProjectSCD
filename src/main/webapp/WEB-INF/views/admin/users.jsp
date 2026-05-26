<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Manage Users</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/fragments/navbar.jsp"/>
<jsp:include page="/WEB-INF/views/fragments/header.jsp"/>
<div class="container">
    <div class="card">
        <h2>Users</h2>
        <form class="toolbar" method="get">
            <input name="q" placeholder="Search" value="${param.q}">
            <select name="role">
                <option value="">All roles</option>
                <option>ADMIN</option><option>DOCTOR</option><option>NURSE</option><option>PATIENT</option>
            </select>
            <button class="btn btn-secondary" type="submit">Search</button>
        </form>
        <table>
            <tr><th>Name</th><th>Username</th><th>Email</th><th>Role</th><th>Phone</th><th>Action</th></tr>
            <c:forEach var="u" items="${users}">
                <tr>
                    <td>${u.fullName}</td><td>${u.username}</td><td>${u.email}</td>
                    <td>${u.role}</td><td>${u.phone}</td>
                    <td>
                        <c:if test="${u.role != 'ADMIN'}">
                        <form class="inline-form" method="post">
                            <input type="hidden" name="action" value="deactivate">
                            <input type="hidden" name="id" value="${u.id}">
                            <button class="btn btn-sm btn-danger" type="submit">Deactivate</button>
                        </form>
                        </c:if>
                    </td>
                </tr>
            </c:forEach>
        </table>
    </div>
    <div class="card">
        <h3>Add Doctor / Nurse</h3>
        <form method="post">
            <input type="hidden" name="action" value="create">
            <div class="grid-2">
                <div class="form-group"><label>Role</label>
                    <select name="role" required><option>DOCTOR</option><option>NURSE</option></select>
                </div>
                <div class="form-group"><label>Username</label><input name="username" required></div>
                <div class="form-group"><label>Password</label><input type="password" name="password" required></div>
                <div class="form-group"><label>Full Name</label><input name="fullName" required></div>
                <div class="form-group"><label>Email</label><input type="email" name="email" required></div>
                <div class="form-group"><label>Phone</label><input name="phone"></div>
                <div class="form-group"><label>Specialization / Department</label><input name="specializationOrDept" required></div>
                <div class="form-group"><label>License (doctors only)</label><input name="license"></div>
            </div>
            <button class="btn" type="submit">Create Staff</button>
        </form>
    </div>
</div>
</body>
</html>
