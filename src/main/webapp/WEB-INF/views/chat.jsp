<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Chat</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/fragments/navbar.jsp"/>
<jsp:include page="/WEB-INF/views/fragments/header.jsp"/>
<div class="container page-enter">
    <div class="grid-2">
        <div class="card">
            <h2>Start Chat</h2>
            <form method="get" action="${pageContext.request.contextPath}/chat">
                <div class="form-group">
                    <label>Chat with</label>
                    <select name="userId" required>
                        <option value="">-- Select user --</option>
                        <c:forEach var="u" items="${chatUsers}">
                            <option value="${u.id}" ${selectedUserId == u.id ? 'selected' : ''}>
                                ${u.fullName} (${u.role})
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <div class="form-group">
                    <label>Appointment ID (optional for appointment-specific chat)</label>
                    <input name="appointmentId" value="${selectedAppointmentId}">
                </div>
                <button type="submit" class="btn">Open Chat</button>
            </form>
        </div>
        <div class="card">
            <h2>
                <c:choose>
                    <c:when test="${not empty chatWithUser}">
                        Chat with ${chatWithUser.fullName}
                        <c:if test="${not empty selectedAppointmentId}"> (Appointment #${selectedAppointmentId})</c:if>
                    </c:when>
                    <c:otherwise>Select a user to begin</c:otherwise>
                </c:choose>
            </h2>

            <div style="max-height:380px;overflow-y:auto;border:1px solid #e5e7eb;border-radius:8px;padding:12px;margin-bottom:12px;background:#fafafa">
                <c:if test="${empty messages}">
                    <p class="muted">No messages yet.</p>
                </c:if>
                <c:forEach var="m" items="${messages}">
                    <div style="margin-bottom:10px">
                        <strong>${m.senderName}:</strong> ${m.content}
                        <br>
                        <small class="muted">${m.createdAt}</small>
                    </div>
                </c:forEach>
            </div>

            <c:if test="${not empty selectedUserId}">
                <form method="post" action="${pageContext.request.contextPath}/chat">
                    <input type="hidden" name="userId" value="${selectedUserId}">
                    <c:if test="${not empty selectedAppointmentId}">
                        <input type="hidden" name="appointmentId" value="${selectedAppointmentId}">
                    </c:if>
                    <div class="form-group">
                        <label>Message</label>
                        <textarea name="content" rows="3" required></textarea>
                    </div>
                    <button class="btn" type="submit">Send</button>
                </form>
            </c:if>
        </div>
    </div>
</div>
</body>
</html>
