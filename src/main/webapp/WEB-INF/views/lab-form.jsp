<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Order Lab Test</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/fragments/navbar.jsp"/>
<div class="container">
    <div class="card">
        <h2>Order Laboratory Test</h2>
        <form method="post" action="${pageContext.request.contextPath}/labs">
            <input type="hidden" name="action" value="order">
            <div class="form-group"><label>Patient</label>
                <select name="patientId" required>
                    <c:forEach var="p" items="${patients}"><option value="${p.id}">${p.fullName}</option></c:forEach>
                </select>
            </div>
            <div class="form-group"><label>Test Name</label>
                <select name="testName" required>
                    <option>Complete Blood Count (CBC)</option>
                    <option>Lipid Profile</option>
                    <option>Blood Glucose</option>
                    <option>Liver Function Test</option>
                    <option>Thyroid Panel</option>
                    <option>Urinalysis</option>
                    <option>X-Ray Chest</option>
                </select>
            </div>
            <div class="form-group"><label>Notes</label><textarea name="notes"></textarea></div>
            <button class="btn" type="submit">Order</button>
        </form>
    </div>
</div>
</body>
</html>
