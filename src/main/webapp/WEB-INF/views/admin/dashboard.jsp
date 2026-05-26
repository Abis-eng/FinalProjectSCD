<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="m" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Admin Dashboard</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"></script>
</head>
<body>
<jsp:include page="/WEB-INF/views/fragments/navbar.jsp"/>
<jsp:include page="/WEB-INF/views/fragments/header.jsp"/>
<div class="container page-enter">
    <h2>Clinic Command Center</h2>
    <div class="grid-2">
        <div class="stat-card animate-in"><span>Patients</span><strong>${stats.totalPatients}</strong></div>
        <div class="stat-card animate-in"><span>Doctors</span><strong>${stats.totalDoctors}</strong></div>
        <div class="stat-card animate-in"><span>Today</span><strong>${stats.todayAppointments}</strong></div>
        <div class="stat-card animate-in"><span>Queue</span><strong>${stats.pendingAppointments}</strong></div>
        <div class="stat-card highlight animate-in"><span>Revenue Today</span><strong><m:formatMoney amount="${stats.revenueToday}"/></strong></div>
        <div class="stat-card animate-in"><span>Unpaid Bills</span><strong>${stats.unpaidInvoices}</strong></div>
    </div>

    <div class="grid-2" style="margin-top:1.5rem">
        <div class="card chart-card">
            <h3>Appointments (7 days)</h3>
            <canvas id="weeklyChart" height="200"></canvas>
        </div>
        <div class="card chart-card">
            <h3>By status</h3>
            <canvas id="statusChart" height="200"></canvas>
        </div>
    </div>
    <div class="card chart-card" style="margin-top:1rem">
        <h3>Revenue (7 days) — PKR</h3>
        <canvas id="revenueChart" height="120"></canvas>
    </div>

    <div class="card" style="margin-top:1.5rem">
        <h3>Today's Appointments</h3>
        <table>
            <tr><th>Patient</th><th>Provider</th><th>Room</th><th>Time</th><th>Status</th></tr>
            <c:forEach var="a" items="${appointments}" begin="0" end="8">
                <tr>
                    <td>${a.patientName}</td><td>${a.providerName}</td>
                    <td>${a.roomNumber}</td><td>${a.timeSlot}</td><td>${a.status}</td>
                </tr>
            </c:forEach>
        </table>
    </div>
</div>
<script>
const chartColors = ['#0b7a75','#14b8a6','#f59e0b','#ef4444','#6366f1'];
function toArray(labels, values) {
    return { labels: labels || [], values: values || [] };
}
const weekly = toArray([
<c:forEach var="l" items="${weeklyChart.labels}" varStatus="s">'${l}'${!s.last ? ',' : ''}</c:forEach>
], [
<c:forEach var="v" items="${weeklyChart.values}" varStatus="s">${v}${!s.last ? ',' : ''}</c:forEach>
]);
const status = toArray([
<c:forEach var="l" items="${statusChart.labels}" varStatus="s">'${l}'${!s.last ? ',' : ''}</c:forEach>
], [
<c:forEach var="v" items="${statusChart.values}" varStatus="s">${v}${!s.last ? ',' : ''}</c:forEach>
]);
const revenue = toArray([
<c:forEach var="l" items="${revenueChart.labels}" varStatus="s">'${l}'${!s.last ? ',' : ''}</c:forEach>
], [
<c:forEach var="v" items="${revenueChart.values}" varStatus="s">${v}${!s.last ? ',' : ''}</c:forEach>
]);

function barChart(id, data, label) {
    new Chart(document.getElementById(id), {
        type: 'bar',
        data: {
            labels: data.labels.length ? data.labels : ['No data'],
            datasets: [{ label: label, data: data.values.length ? data.values : [0], backgroundColor: chartColors, borderRadius: 8 }]
        },
        options: { animation: { duration: 1200 }, plugins: { legend: { display: false } }, scales: { y: { beginAtZero: true } } }
    });
}
function doughnutChart(id, data) {
    new Chart(document.getElementById(id), {
        type: 'doughnut',
        data: {
            labels: data.labels.length ? data.labels : ['None'],
            datasets: [{ data: data.values.length ? data.values : [1], backgroundColor: chartColors }]
        },
        options: { animation: { animateRotate: true, duration: 1400 }, plugins: { legend: { position: 'bottom' } } }
    });
}
barChart('weeklyChart', weekly, 'Appointments');
doughnutChart('statusChart', status);
barChart('revenueChart', revenue, 'PKR');
</script>
</body>
</html>
