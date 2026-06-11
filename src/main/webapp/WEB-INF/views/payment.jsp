<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="m" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Secure Checkout - E-Clinic</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/fragments/navbar.jsp"/>
<jsp:include page="/WEB-INF/views/fragments/header.jsp"/>
<div class="container page-enter">
    <div class="card checkout-card">
        <h2>Secure Payment</h2>
        <p class="muted">Invoice #${invoice.id} · ${invoice.providerName} · Visit ${invoice.appointmentDate}</p>

        <div class="checkout-steps">
            <div class="checkout-step ${step >= 1 ? 'active' : ''} ${step > 1 ? 'done' : ''}">1. Method</div>
            <div class="checkout-step ${step >= 2 ? 'active' : ''} ${step > 2 ? 'done' : ''}">2. Details</div>
            <div class="checkout-step ${step >= 3 ? 'active' : ''}">3. Review</div>
            <div class="checkout-step">4. Processing</div>
        </div>

        <div class="checkout-summary">
            <strong>Amount due:</strong> <m:formatMoney amount="${invoice.amount}"/>
        </div>

        <c:if test="${step == 1}">
            <form method="post" action="${pageContext.request.contextPath}/payment">
                <input type="hidden" name="step" value="1">
                <p>Select how you would like to pay. Cash payments are handled at the clinic reception.</p>
                <div class="payment-methods">
                    <label class="payment-option">
                        <input type="radio" name="method" value="CARD" required>
                        <span>
                            <strong>Debit / Credit Card</strong>
                            <small>Visa, Mastercard, or local bank card</small>
                        </span>
                    </label>
                    <label class="payment-option">
                        <input type="radio" name="method" value="ONLINE">
                        <span>
                            <strong>JazzCash / EasyPaisa</strong>
                            <small>Mobile wallet with OTP verification</small>
                        </span>
                    </label>
                    <label class="payment-option">
                        <input type="radio" name="method" value="BANK_TRANSFER">
                        <span>
                            <strong>Bank Transfer</strong>
                            <small>Pay from your bank app and enter reference</small>
                        </span>
                    </label>
                </div>
                <button class="btn" type="submit">Continue</button>
                <a class="btn btn-secondary" href="${pageContext.request.contextPath}/patient/billing">Cancel</a>
            </form>
        </c:if>

        <c:if test="${step == 2}">
            <form method="post" action="${pageContext.request.contextPath}/payment">
                <input type="hidden" name="step" value="2">
                <c:if test="${checkout.method == 'CARD'}">
                    <div class="form-group">
                        <label>Name on card</label>
                        <input name="cardHolderName" required placeholder="As printed on card">
                    </div>
                    <div class="form-group">
                        <label>Card number</label>
                        <input name="cardNumber" required placeholder="4111 1111 1111 1111" maxlength="19">
                    </div>
                    <div class="grid-2">
                        <div class="form-group">
                            <label>Expiry (MM/YY)</label>
                            <input name="cardExpiry" required placeholder="12/28" maxlength="5">
                        </div>
                        <div class="form-group">
                            <label>CVV</label>
                            <input name="cardCvv" required placeholder="123" maxlength="4">
                        </div>
                    </div>
                    <p class="muted">Demo only — no real card is charged. Use test number 4111 1111 1111 1111.</p>
                </c:if>
                <c:if test="${checkout.method == 'ONLINE'}">
                    <div class="form-group">
                        <label>Mobile wallet number</label>
                        <input name="mobileNumber" required placeholder="03XXXXXXXXX">
                    </div>
                    <div class="form-group">
                        <label>OTP sent to your mobile</label>
                        <input name="walletOtp" required placeholder="6-digit code" maxlength="6">
                    </div>
                    <p class="muted">For demo, enter any 6-digit OTP after your JazzCash/EasyPaisa number.</p>
                </c:if>
                <c:if test="${checkout.method == 'BANK_TRANSFER'}">
                    <div class="fee-hint">
                        Transfer to <strong>E-Clinic Health Services</strong><br>
                        Bank: HBL · Account: 1234-56789012-34 · IBAN: PK00HABB0000123456789012
                    </div>
                    <div class="form-group">
                        <label>Your bank name</label>
                        <input name="bankName" required placeholder="e.g. HBL, Meezan, UBL">
                    </div>
                    <div class="form-group">
                        <label>Transaction reference</label>
                        <input name="bankTransactionRef" required placeholder="From your bank receipt">
                    </div>
                </c:if>
                <button class="btn" type="submit">Review payment</button>
                <a class="btn btn-secondary" href="${pageContext.request.contextPath}/payment?invoiceId=${invoice.id}&step=1">Back</a>
            </form>
        </c:if>

        <c:if test="${step == 3}">
            <div class="review-box">
                <h3>Review your payment</h3>
                <p><strong>Method:</strong>
                    <c:choose>
                        <c:when test="${checkout.method == 'CARD'}">Debit / Credit Card</c:when>
                        <c:when test="${checkout.method == 'ONLINE'}">JazzCash / EasyPaisa</c:when>
                        <c:otherwise>Bank Transfer</c:otherwise>
                    </c:choose>
                </p>
                <c:if test="${checkout.method == 'CARD'}">
                    <p><strong>Card:</strong> ${checkout.maskedCard}</p>
                    <p><strong>Name:</strong> ${checkout.cardHolderName}</p>
                    <p><strong>Expiry:</strong> ${checkout.cardExpiry}</p>
                </c:if>
                <c:if test="${checkout.method == 'ONLINE'}">
                    <p><strong>Wallet:</strong> ${checkout.mobileNumber}</p>
                </c:if>
                <c:if test="${checkout.method == 'BANK_TRANSFER'}">
                    <p><strong>Bank:</strong> ${checkout.bankName}</p>
                    <p><strong>Reference:</strong> ${checkout.bankTransactionRef}</p>
                </c:if>
                <p><strong>Total:</strong> <m:formatMoney amount="${invoice.amount}"/></p>
            </div>
            <form method="post" action="${pageContext.request.contextPath}/payment">
                <input type="hidden" name="step" value="3">
                <button class="btn" type="submit">Confirm &amp; pay</button>
                <a class="btn btn-secondary" href="${pageContext.request.contextPath}/payment?invoiceId=${invoice.id}&step=2">Back</a>
            </form>
        </c:if>
    </div>
</div>
</body>
</html>
