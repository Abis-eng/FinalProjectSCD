<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="m" tagdir="/WEB-INF/tags" %>
<%@ page import="com.elcinic.model.Role" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Billing - E-Clinic</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/fragments/navbar.jsp"/>
<jsp:include page="/WEB-INF/views/fragments/header.jsp"/>
<div class="container">
    <div class="card">
        <h2>Billing & Invoices</h2>
        <table>
            <tr><th>#</th><th>Patient</th><th>Provider</th><th>Visit</th><th>Amount</th><th>Status</th><th>Action</th></tr>
            <c:forEach var="inv" items="${invoices}">
                <tr>
                    <td>${inv.id}</td>
                    <td>${inv.patientName}</td>
                    <td>${inv.providerName}</td>
                    <td>${inv.appointmentDate}</td>
                    <td><m:formatMoney amount="${inv.amount}"/></td>
                    <td><span class="badge badge-${inv.status.name().toLowerCase()}">${inv.status}</span></td>
                    <td>
                        <c:if test="${inv.status.name() == 'PENDING' && sessionScope.currentUser.role.name() == 'PATIENT'}">
                            <form class="inline-form" method="post" action="${pageContext.request.contextPath}/billing">
                                <input type="hidden" name="action" value="pay">
                                <input type="hidden" name="id" value="${inv.id}">
                                <select name="paymentMethod"><option>CARD</option><option>CASH</option><option>ONLINE</option></select>
                                <button class="btn btn-sm" type="submit">Pay Now</button>
                            </form>
                        </c:if>
                        <c:if test="${sessionScope.currentUser.role.name() == 'ADMIN' && inv.status.name() == 'PENDING'}">
                            <form class="inline-form" method="post" action="${pageContext.request.contextPath}/billing">
                                <input type="hidden" name="id" value="${inv.id}">
                                <button name="action" value="markPaid" class="btn btn-sm">Mark Paid</button>
                                <button name="action" value="waive" class="btn btn-sm btn-secondary">Waive</button>
                            </form>
                        </c:if>
                    </td>
                </tr>
            </c:forEach>
        </table>
        <c:if test="${empty invoices}"><p class="muted">No invoices.</p></c:if>
    </div>
</div>
</body>
</html>
