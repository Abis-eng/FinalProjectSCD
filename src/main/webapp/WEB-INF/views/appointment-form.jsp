<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page import="com.elcinic.model.Role" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Book Appointment - E-Clinic</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/fragments/navbar.jsp"/>
<div class="container page-enter">
    <div class="card">
        <h2>${empty appointment ? 'Book Appointment' : 'Edit Appointment'}</h2>
        <form method="post" action="${pageContext.request.contextPath}/appointments" id="bookingForm">
            <input type="hidden" name="action" value="${empty appointment ? 'create' : 'update'}">
            <c:if test="${not empty appointment}"><input type="hidden" name="id" value="${appointment.id}"></c:if>

            <%
                com.elcinic.model.User u = (com.elcinic.model.User) session.getAttribute("currentUser");
                if (u.getRole() == Role.ADMIN) {
            %>
            <div class="form-group"><label>Patient</label>
                <select name="patientId" required>
                    <c:forEach var="p" items="${patients}">
                        <option value="${p.id}">${p.fullName}</option>
                    </c:forEach>
                </select>
            </div>
            <% } %>

            <div class="form-group"><label>Provider Type</label>
                <select name="providerType" id="providerType" required onchange="toggleProvider(); updateFeeHint();">
                    <option value="DOCTOR">Doctor</option>
                    <option value="NURSE">Nurse</option>
                </select>
            </div>
            <div class="form-group"><label>Provider</label>
                <select name="providerId" id="doctorSelect" required onchange="updateFeeHint()">
                    <c:forEach var="d" items="${doctors}">
                        <option value="${d.userId}" data-fee="${d.consultationFee}">Dr. ${d.fullName} — ${d.specialization} (from Rs. ${d.consultationFee})</option>
                    </c:forEach>
                </select>
                <select name="providerId" id="nurseSelect" style="display:none" disabled data-base-fee="1500">
                    <c:forEach var="n" items="${nurses}">
                        <option value="${n.userId}">${n.fullName} (${n.department})</option>
                    </c:forEach>
                </select>
            </div>
            <div class="form-group"><label>Visit Type</label>
                <select name="appointmentType" id="appointmentType" required onchange="updateFeeHint()">
                    <option value="CONSULTATION" data-mult="1">Consultation (base fee)</option>
                    <option value="FOLLOW_UP" data-mult="0.65">Follow-up (×0.65)</option>
                    <option value="EMERGENCY" data-mult="2.5">Emergency (×2.5)</option>
                    <option value="VACCINATION" data-mult="1.35">Vaccination (×1.35)</option>
                </select>
            </div>
            <p id="feeHint" class="fee-hint">Estimated fee: <strong>Rs. 3,000</strong></p>

            <c:if test="${sessionScope.currentUser.role == 'DOCTOR'}">
                <div class="form-group">
                    <label>Custom fee for this visit (PKR, optional)</label>
                    <input type="number" name="feeAmount" id="feeAmount" min="500" step="100" placeholder="Leave blank for auto-calculated fee">
                </div>
            </c:if>

            <div class="form-group"><label>Priority</label>
                <select name="priority"><option>NORMAL</option><option>URGENT</option></select>
            </div>
            <div class="form-group"><label>Symptoms</label><textarea name="symptoms" rows="2"></textarea></div>
            <div class="form-group"><label>Date</label><input type="date" name="appointmentDate" required></div>
            <div class="form-group"><label>Time Slot</label>
                <select name="timeSlot" required>
                    <option>09:00-09:30</option><option>09:30-10:00</option><option>10:00-10:30</option>
                    <option>11:00-11:30</option><option>14:00-14:30</option><option>15:00-15:30</option>
                </select>
            </div>
            <div class="form-group"><label>Reason</label><textarea name="reason" rows="3"></textarea></div>
            <c:if test="${not empty appointment}">
                <div class="form-group"><label>Status</label>
                    <select name="status">
                        <option>PENDING</option><option>CONFIRMED</option>
                        <option>COMPLETED</option><option>CANCELLED</option>
                    </select>
                </div>
                <div class="form-group"><label>Notes</label><textarea name="notes">${appointment.notes}</textarea></div>
            </c:if>
            <button class="btn" type="submit">Save</button>
            <a class="btn btn-secondary" href="javascript:history.back()">Cancel</a>
        </form>
    </div>
</div>
<script>
function toggleProvider() {
    const type = document.getElementById('providerType').value;
    const doc = document.getElementById('doctorSelect');
    const nurse = document.getElementById('nurseSelect');
    if (type === 'DOCTOR') {
        doc.style.display = ''; doc.disabled = false; doc.name = 'providerId';
        nurse.style.display = 'none'; nurse.disabled = true; nurse.removeAttribute('name');
    } else {
        nurse.style.display = ''; nurse.disabled = false; nurse.name = 'providerId';
        doc.style.display = 'none'; doc.disabled = true; doc.removeAttribute('name');
    }
}
function updateFeeHint() {
    const type = document.getElementById('providerType').value;
    const appt = document.getElementById('appointmentType');
    const mult = parseFloat(appt.options[appt.selectedIndex].dataset.mult || '1');
    let base = 1500;
    if (type === 'DOCTOR') {
        const sel = document.getElementById('doctorSelect');
        const opt = sel.options[sel.selectedIndex];
        base = parseFloat(opt.dataset.fee || '3000');
    }
    const total = Math.round(base * mult);
    document.getElementById('feeHint').innerHTML = 'Estimated fee: <strong>Rs. ' + total.toLocaleString('en-PK') + '</strong>';
}
document.addEventListener('DOMContentLoaded', function() { toggleProvider(); updateFeeHint(); });
</script>
</body>
</html>
