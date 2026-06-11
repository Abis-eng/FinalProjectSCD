<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>My Profile</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/fragments/navbar.jsp"/>
<div class="container">
    <div class="card">
        <h2>My Profile & Assigned Doctor</h2>
        <p><strong>Date of Birth:</strong> ${profile.dateOfBirth}</p>
        <p><strong>Blood Type:</strong> ${profile.bloodType}</p>
        <p><strong>Assigned Doctor:</strong> ${profile.assignedDoctorName != null ? profile.assignedDoctorName : 'None'}</p>
        <p><strong>Requested Doctor:</strong> ${profile.requestedDoctorName != null ? profile.requestedDoctorName : 'No pending request'}</p>
        <h3>Available Doctors</h3>
        <table>
            <tr><th>Name</th><th>Specialization</th><th>License</th><th>Action</th></tr>
            <c:forEach var="d" items="${doctors}">
                <tr>
                    <td>${d.fullName}</td><td>${d.specialization}</td><td>${d.licenseNumber}</td>
                    <td>
                        <form method="post" class="inline-form" action="${pageContext.request.contextPath}/patient/profile">
                            <input type="hidden" name="action" value="requestDoctor">
                            <input type="hidden" name="doctorId" value="${d.userId}">
                            <button class="btn btn-sm" type="submit">Request</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
        </table>
        <p style="margin-top:1rem;color:#6b7280">Send request from here. Admin will review and assign your doctor.</p>
    </div>
</div>
</body>
</html>
