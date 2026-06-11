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
                            <a class="btn btn-sm" href="${pageContext.request.contextPath}/payment?invoiceId=${inv.id}">Pay securely</a>
                            <span class="muted" style="font-size:.8rem;display:block;margin-top:.25rem">Card · JazzCash · Bank transfer</span>
                        </c:if>
                        <c:if test="${sessionScope.currentUser.role.name() == 'ADMIN' && inv.status.name() == 'PENDING'}">
                            <form class="inline-form" method="post" action="${pageContext.request.contextPath}/billing">
                                <input type="hidden" name="id" value="${inv.id}">
                                <select name="paymentMethod">
                                    <option value="CASH">Cash at desk</option>
                                    <option value="CARD">Card at desk</option>
                                    <option value="ONLINE">Online (manual)</option>
                                    <option value="BANK_TRANSFER">Bank transfer</option>
                                </select>
                                <button name="action" value="markPaid" class="btn btn-sm">Mark Paid</button>
                                <button name="action" value="waive" class="btn btn-sm btn-secondary">Waive</button>
                            </form>
                        </c:if>
                        <c:if test="${inv.status.name() == 'PAID' && not empty inv.paymentReference}">
                            <span class="muted" style="font-size:.75rem">${inv.paymentReference}</span>
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
