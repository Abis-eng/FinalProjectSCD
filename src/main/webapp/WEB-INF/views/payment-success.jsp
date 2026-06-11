<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="m" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Payment Successful - E-Clinic</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/fragments/navbar.jsp"/>
<div class="container page-enter">
    <div class="card checkout-card success-card">
        <div class="success-icon">✓</div>
        <h2>Payment successful</h2>
        <p>Your invoice has been paid. A receipt has been sent to your notifications.</p>
        <div class="review-box">
            <p><strong>Transaction reference:</strong> ${reference}</p>
            <c:if test="${not empty invoice}">
                <p><strong>Invoice #:</strong> ${invoice.id}</p>
                <p><strong>Provider:</strong> ${invoice.providerName}</p>
                <p><strong>Amount paid:</strong> <m:formatMoney amount="${invoice.amount}"/></p>
                <p><strong>Method:</strong> ${invoice.paymentMethod}</p>
                <c:if test="${not empty invoice.cardLast4}">
                    <p><strong>Card ending:</strong> **** ${invoice.cardLast4}</p>
                </c:if>
            </c:if>
        </div>
        <a class="btn" href="${pageContext.request.contextPath}/patient/billing">Back to billing</a>
        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/patient/dashboard">Dashboard</a>
    </div>
</div>
</body>
</html>
