<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>New Medical Record</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/fragments/navbar.jsp"/>
<div class="container">
    <div class="card">
        <h2>Add Medical Record</h2>
        <form method="post" action="${pageContext.request.contextPath}/records">
            <input type="hidden" name="action" value="create">
            <div class="form-group"><label>Patient</label>
                <select name="patientId" required>
                    <c:forEach var="p" items="${patients}"><option value="${p.id}">${p.fullName}</option></c:forEach>
                </select>
            </div>
            <div class="form-group"><label>Visit Date</label><input type="date" name="visitDate" required></div>
            <div class="form-group"><label>Diagnosis</label><input name="diagnosis" required></div>
            <div class="form-group"><label>Prescription</label><textarea name="prescription"></textarea></div>
            <div class="form-group"><label>Notes</label><textarea name="notes"></textarea></div>
            <button class="btn" type="submit">Save</button>
        </form>
    </div>
</div>
</body>
</html>
