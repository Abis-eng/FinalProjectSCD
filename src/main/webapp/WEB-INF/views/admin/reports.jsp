<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="m" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Reports</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/fragments/navbar.jsp"/>
<div class="container page-enter">
    <h2>Clinic Analytics</h2>
    <div class="grid-2">
        <div class="stat-card animate-in"><span>Patients</span><strong>${stats.totalPatients}</strong></div>
        <div class="stat-card animate-in"><span>Doctors</span><strong>${stats.totalDoctors}</strong></div>
        <div class="stat-card animate-in"><span>Today's Appointments</span><strong>${stats.todayAppointments}</strong></div>
        <div class="stat-card animate-in"><span>Pending Queue</span><strong>${stats.pendingAppointments}</strong></div>
        <div class="stat-card animate-in"><span>Revenue Today</span><strong><m:formatMoney amount="${stats.revenueToday}"/></strong></div>
        <div class="stat-card animate-in"><span>Unpaid Invoices</span><strong>${stats.unpaidInvoices}</strong></div>
        <div class="stat-card animate-in"><span>Pending Labs</span><strong>${stats.pendingLabs}</strong></div>
    </div>
    <div class="card" style="margin-top:1.5rem">
        <h3>Recent Invoices</h3>
        <table>
            <tr><th>Patient</th><th>Amount (PKR)</th><th>Status</th></tr>
            <c:forEach var="inv" items="${invoices}" begin="0" end="9">
                <tr><td>${inv.patientName}</td><td><m:formatMoney amount="${inv.amount}"/></td><td>${inv.status}</td></tr>
            </c:forEach>
        </table>
    </div>
</div>
</body>
</html>
