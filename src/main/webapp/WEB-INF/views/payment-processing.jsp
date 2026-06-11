<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Processing Payment - E-Clinic</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/fragments/navbar.jsp"/>
<div class="container page-enter">
    <div class="card checkout-card processing-card">
        <div class="processing-spinner"></div>
        <h2>Processing your payment</h2>
        <p class="muted">Please wait while we verify with the payment gateway. Do not close this page.</p>
        <ul class="processing-steps">
            <li>Verifying payment details…</li>
            <li>Contacting bank / wallet provider…</li>
            <li>Confirming transaction…</li>
        </ul>
        <form id="finalizeForm" method="post" action="${pageContext.request.contextPath}/payment">
            <input type="hidden" name="action" value="finalize">
        </form>
    </div>
</div>
<script>
    setTimeout(function () {
        document.getElementById('finalizeForm').submit();
    }, 2800);
</script>
</body>
</html>
