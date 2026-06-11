<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="m" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page import="com.elcinic.model.Role" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Appointment #${appointment.id}</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/fragments/navbar.jsp"/>
<div class="container">
    <div class="grid-2">
        <div class="card">
            <h2>Appointment Details</h2>
            <p><strong>Patient:</strong> ${appointment.patientName}</p>
            <p><strong>Provider:</strong> ${appointment.providerName} (${appointment.providerType})</p>
            <p><strong>Date:</strong> ${appointment.appointmentDate} · ${appointment.timeSlot}</p>
            <p><strong>Room:</strong> ${appointment.roomNumber}</p>
            <p><strong>Type:</strong> ${appointment.appointmentType} · <strong>Priority:</strong> ${appointment.priority}</p>
            <p><strong>Status:</strong> <span class="badge badge-${appointment.status.name().toLowerCase()}">${appointment.status}</span></p>
            <p><strong>Symptoms:</strong> ${appointment.symptoms}</p>
            <p><strong>Reason:</strong> ${appointment.reason}</p>
            <p><strong>Fee:</strong> <m:formatMoney amount="${appointment.feeAmount}"/></p>
            <c:if test="${not empty invoice}">
                <p><strong>Invoice:</strong> ${invoice.status} — <m:formatMoney amount="${invoice.amount}"/></p>
            </c:if>
        </div>
        <div class="card">
            <h3>Vital Signs</h3>
            <c:if test="${not empty vitals}">
                <p>BP: ${vitals.bloodPressure} · Pulse: ${vitals.pulse} · Temp: ${vitals.temperature}°C</p>
                <p>Weight: ${vitals.weightKg} kg · Height: ${vitals.heightCm} cm</p>
            </c:if>
            <% if (((com.elcinic.model.User)session.getAttribute("currentUser")).getRole() != Role.PATIENT) { %>
            <form method="post" action="${pageContext.request.contextPath}/appointments">
                <input type="hidden" name="action" value="vitals">
                <input type="hidden" name="appointmentId" value="${appointment.id}">
                <div class="form-group"><label>Blood Pressure</label><input name="bloodPressure" placeholder="120/80" value="${vitals.bloodPressure}"></div>
                <div class="grid-2">
                    <div class="form-group"><label>Pulse</label><input name="pulse" value="${vitals.pulse}"></div>
                    <div class="form-group"><label>Temp °C</label><input name="temperature" value="${vitals.temperature}"></div>
                </div>
                <div class="grid-2">
                    <div class="form-group"><label>Weight kg</label><input name="weightKg" value="${vitals.weightKg}"></div>
                    <div class="form-group"><label>Height cm</label><input name="heightCm" value="${vitals.heightCm}"></div>
                </div>
                <button class="btn" type="submit">Save Vitals</button>
            </form>
            <% } %>
            <div style="margin-top:12px">
                <a class="btn btn-secondary"
                   href="${pageContext.request.contextPath}/chat?userId=${sessionScope.currentUser.id == appointment.patientId ? appointment.providerId : appointment.patientId}&appointmentId=${appointment.id}">
                    Chat on this appointment
                </a>
            </div>
        </div>
    </div>
    <a class="btn btn-secondary" href="javascript:history.back()">Back</a>
</div>
</body>
</html>
