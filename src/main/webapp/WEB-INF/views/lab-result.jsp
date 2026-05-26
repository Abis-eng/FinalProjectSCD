<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Lab Result</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/fragments/navbar.jsp"/>
<div class="container">
    <div class="card">
        <h2>Update Lab Result — ${labTest.testName}</h2>
        <p>Patient: <strong>${labTest.patientName}</strong></p>
        <form method="post" action="${pageContext.request.contextPath}/labs">
            <input type="hidden" name="action" value="update">
            <input type="hidden" name="id" value="${labTest.id}">
            <div class="form-group"><label>Result Value</label><input name="resultValue" value="${labTest.resultValue}"></div>
            <div class="form-group"><label>Unit</label><input name="resultUnit" value="${labTest.resultUnit}" placeholder="mg/dL"></div>
            <div class="form-group"><label>Status</label>
                <select name="status">
                    <option>IN_PROGRESS</option>
                    <option selected>COMPLETED</option>
                    <option>CANCELLED</option>
                </select>
            </div>
            <div class="form-group"><label>Notes</label><textarea name="notes">${labTest.notes}</textarea></div>
            <button class="btn" type="submit">Save Result</button>
        </form>
    </div>
</div>
</body>
</html>
