<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Notifications</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/fragments/navbar.jsp"/>
<div class="container">
    <div class="card">
        <div class="toolbar">
            <h2>Notifications <c:if test="${unreadCount > 0}"><span class="badge-count">${unreadCount} unread</span></c:if></h2>
            <form method="post"><input type="hidden" name="action" value="readAll"><button class="btn btn-secondary btn-sm">Mark all read</button></form>
        </div>
        <c:forEach var="n" items="${notifications}">
            <div class="notification-item ${n.read ? 'read' : 'unread'}">
                <strong>${n.title}</strong>
                <p>${n.message}</p>
                <small class="muted">${n.createdAt}</small>
                <c:if test="${!n.read}">
                    <form method="post" class="inline-form"><input type="hidden" name="id" value="${n.id}"><button class="btn btn-sm">Mark read</button></form>
                </c:if>
            </div>
        </c:forEach>
        <c:if test="${empty notifications}"><p class="muted">No notifications.</p></c:if>
    </div>
</div>
</body>
</html>
