<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="m" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Account Settings</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/fragments/navbar.jsp"/>
<jsp:include page="/WEB-INF/views/fragments/header.jsp"/>
<div class="container page-enter">
    <div class="grid-2">
        <div class="card">
            <h2>Profile</h2>
            <p><strong>Name:</strong> ${profileUser.fullName}</p>
            <p><strong>Username:</strong> ${profileUser.username}</p>
            <p><strong>Email:</strong> ${profileUser.email}</p>
            <p><strong>Phone:</strong> ${profileUser.phone != null ? profileUser.phone : '—'}</p>
            <p><strong>Role:</strong> ${profileUser.role}</p>
        </div>
        <div class="card">
            <h2>Change Password</h2>
            <form method="post">
                <input type="hidden" name="action" value="password">
                <div class="form-group"><label>Current Password</label><input type="password" name="currentPassword" required></div>
                <div class="form-group"><label>New Password</label><input type="password" name="newPassword" required minlength="6"></div>
                <button class="btn" type="submit">Update Password</button>
            </form>
        </div>
    </div>

    <c:if test="${not empty doctorProfile}">
        <div class="card" style="margin-top:1.5rem">
            <h2>Consultation Fee (PKR)</h2>
            <p class="muted">Set your base consultation fee. Follow-up, emergency, and vaccination visits use multipliers on this amount.</p>
            <p>Current fee: <strong><m:formatMoney amount="${doctorProfile.consultationFee}"/></strong></p>
            <form method="post" class="toolbar" style="align-items:flex-end">
                <input type="hidden" name="action" value="fee">
                <div class="form-group" style="margin:0">
                    <label>New fee (Rs.)</label>
                    <input type="number" name="consultationFee" min="500" max="500000" step="100"
                           value="${doctorProfile.consultationFee}" required>
                </div>
                <button class="btn" type="submit">Save Fee</button>
            </form>
        </div>
    </c:if>
</div>
</body>
</html>
