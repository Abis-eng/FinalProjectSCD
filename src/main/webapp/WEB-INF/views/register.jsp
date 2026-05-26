<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Create Account - E-Clinic</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="auth-page">
<div class="auth-card" style="max-width:560px">
    <h1>Create Account</h1>
    <p style="text-align:center;color:#6b7280;margin-bottom:1rem">Choose your role. Doctors and nurses require admin approval before login.</p>
    <% if (request.getAttribute("error") != null) { %>
        <p class="error-msg"><%= request.getAttribute("error") %></p>
    <% } %>

    <div class="role-tabs" style="display:flex;gap:.5rem;margin-bottom:1.25rem;flex-wrap:wrap">
        <a class="btn btn-outline ${selectedRole == 'PATIENT' ? 'active' : ''}"
           href="${pageContext.request.contextPath}/register?role=PATIENT">Patient</a>
        <a class="btn btn-outline ${selectedRole == 'DOCTOR' ? 'active' : ''}"
           href="${pageContext.request.contextPath}/register?role=DOCTOR">Doctor</a>
        <a class="btn btn-outline ${selectedRole == 'NURSE' ? 'active' : ''}"
           href="${pageContext.request.contextPath}/register?role=NURSE">Nurse</a>
    </div>

    <form method="post" action="${pageContext.request.contextPath}/register">
        <input type="hidden" name="role" value="${selectedRole}">

        <div class="form-group"><label>Full Name</label><input name="fullName" required></div>
        <div class="form-group"><label>Username</label><input name="username" required pattern="[A-Za-z0-9_]{3,50}"></div>
        <div class="form-group"><label>Email</label><input type="email" name="email" required></div>
        <div class="form-group"><label>Phone</label><input name="phone"></div>
        <div class="form-group"><label>Password</label><input type="password" name="password" required minlength="6"></div>

        <c:if test="${selectedRole == 'PATIENT'}">
            <div class="form-group"><label>Date of Birth</label><input type="date" name="dateOfBirth"></div>
            <div class="form-group"><label>Blood Type</label>
                <select name="bloodType">
                    <option value="">-- Select --</option>
                    <option>A+</option><option>A-</option><option>B+</option><option>B-</option>
                    <option>O+</option><option>O-</option><option>AB+</option><option>AB-</option>
                </select>
            </div>
            <div class="form-group"><label>Preferred Doctor (optional)</label>
                <select name="assignedDoctorId">
                    <option value="">-- Assign later --</option>
                    <c:forEach var="d" items="${doctors}">
                        <option value="${d.userId}">${d.fullName} - ${d.specialization}</option>
                    </c:forEach>
                </select>
            </div>
        </c:if>

        <c:if test="${selectedRole == 'DOCTOR'}">
            <div class="form-group"><label>Specialization</label><input name="specialization" required></div>
            <div class="form-group"><label>Medical License Number</label><input name="licenseNumber" required></div>
            <p class="hint" style="font-size:.85rem;color:#6b7280">You cannot log in until an administrator verifies your credentials.</p>
        </c:if>

        <c:if test="${selectedRole == 'NURSE'}">
            <div class="form-group"><label>Department</label><input name="department" required placeholder="e.g. Outpatient Care"></div>
            <p class="hint" style="font-size:.85rem;color:#6b7280">You cannot log in until an administrator approves your registration.</p>
        </c:if>

        <button type="submit" class="btn" style="width:100%;margin-top:.5rem">Register</button>
    </form>
    <p style="text-align:center;margin-top:1rem"><a href="${pageContext.request.contextPath}/login">Back to login</a></p>
</div>
</body>
</html>
