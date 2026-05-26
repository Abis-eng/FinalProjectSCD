package com.elcinic.controller;

import com.elcinic.model.*;
import com.elcinic.service.ServiceFactory;
import com.elcinic.utility.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@WebServlet(name = "AppointmentServlet", urlPatterns = {"/appointments"})
public class AppointmentServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        String action = request.getParameter("action");
        if ("new".equals(action)) {
            loadFormData(request, user);
            request.getRequestDispatcher("/WEB-INF/views/appointment-form.jsp").forward(request, response);
            return;
        }
        if ("view".equals(action) || "edit".equals(action)) {
            int id = ValidationUtil.parsePositiveId(request.getParameter("id"), "Appointment");
            Appointment appt = ServiceFactory.appointmentService().getById(id);
            assertCanAccessAppointment(user, appt);
            request.setAttribute("appointment", appt);
            request.setAttribute("vitals", ServiceFactory.vitalService().getByAppointment(id).orElse(null));
            request.setAttribute("invoice", ServiceFactory.billingService().listForPatient(appt.getPatientId())
                    .stream().filter(i -> i.getAppointmentId() == id).findFirst().orElse(null));
            if ("edit".equals(action)) {
                loadFormData(request, user);
                request.getRequestDispatcher("/WEB-INF/views/appointment-form.jsp").forward(request, response);
            } else {
                request.getRequestDispatcher("/WEB-INF/views/appointment-detail.jsp").forward(request, response);
            }
            return;
        }

        List<Appointment> list = loadList(request, user);
        request.setAttribute("appointments", list);
        request.getRequestDispatcher("/WEB-INF/views/appointments.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = currentUser(request);
        String action = request.getParameter("action");
        if (action == null || action.isBlank()) {
            setFlash(request, "error", "Action is required");
            response.sendRedirect(redirectWithContext(request, appointmentsPath(user)));
            return;
        }
        try {
            switch (action) {
                case "create" -> create(request, user);
                case "update" -> update(request);
                case "status" -> updateStatus(request);
                case "delete" -> delete(request);
                case "vitals" -> saveVitals(request, user);
                default -> throw new IllegalArgumentException("Unknown action");
            }
            setFlash(request, "success", "Appointment saved successfully");
        } catch (Exception e) {
            setFlash(request, "error", handleError(request, e));
        }
        response.sendRedirect(redirectWithContext(request, appointmentsPath(user)));
    }

    private void create(HttpServletRequest request, User user) {
        int patientId = user.getRole() == Role.PATIENT
                ? user.getId()
                : ValidationUtil.parsePositiveId(request.getParameter("patientId"), "Patient");

        int providerId = ValidationUtil.parsePositiveId(request.getParameter("providerId"), "Provider");
        ProviderType type = ProviderType.fromString(request.getParameter("providerType"));
        LocalDate date = ValidationUtil.parseDate(request.getParameter("appointmentDate"), "Appointment date");

        BigDecimal customFee = null;
        String feeParam = request.getParameter("feeAmount");
        if (feeParam != null && !feeParam.isBlank()) {
            customFee = new BigDecimal(feeParam.trim());
        }

        ServiceFactory.appointmentService().book(
                patientId, providerId, type, date,
                request.getParameter("timeSlot"), request.getParameter("reason"),
                AppointmentType.fromString(request.getParameter("appointmentType")),
                Priority.fromString(request.getParameter("priority")),
                request.getParameter("symptoms"),
                customFee);
    }

    private void saveVitals(HttpServletRequest request, User user) {
        if (user.getRole() == Role.PATIENT) {
            throw new IllegalArgumentException("Patients cannot record vitals");
        }
        int appointmentId = ValidationUtil.parsePositiveId(request.getParameter("appointmentId"), "Appointment");
        Appointment appt = ServiceFactory.appointmentService().getById(appointmentId);
        assertCanAccessAppointment(user, appt);
        VitalSigns v = new VitalSigns();
        v.setAppointmentId(appointmentId);
        v.setBloodPressure(request.getParameter("bloodPressure"));
        v.setPulse(parseOptionalInt(request.getParameter("pulse"), "Pulse", 20, 260));
        v.setTemperature(parseOptionalDouble(request.getParameter("temperature"), "Temperature", 30, 45));
        v.setWeightKg(parseOptionalDouble(request.getParameter("weightKg"), "Weight", 1, 500));
        v.setHeightCm(parseOptionalDouble(request.getParameter("heightCm"), "Height", 20, 300));
        v.setRecordedBy(user.getId());
        ServiceFactory.vitalService().save(v);
    }

    private void update(HttpServletRequest request) {
        int id = ValidationUtil.parsePositiveId(request.getParameter("id"), "Appointment");
        Appointment a = ServiceFactory.appointmentService().getById(id);
        assertCanAccessAppointment(currentUser(request), a);
        a.setProviderId(ValidationUtil.parsePositiveId(request.getParameter("providerId"), "Provider"));
        a.setProviderType(ProviderType.fromString(request.getParameter("providerType")));
        a.setAppointmentDate(ValidationUtil.parseDate(request.getParameter("appointmentDate"), "Appointment date"));
        a.setTimeSlot(request.getParameter("timeSlot"));
        a.setStatus(AppointmentStatus.fromString(request.getParameter("status")));
        a.setReason(request.getParameter("reason"));
        a.setNotes(request.getParameter("notes"));
        ServiceFactory.appointmentService().updateAppointment(a);
    }

    private void updateStatus(HttpServletRequest request) {
        int id = ValidationUtil.parsePositiveId(request.getParameter("id"), "Appointment");
        Appointment appointment = ServiceFactory.appointmentService().getById(id);
        assertCanAccessAppointment(currentUser(request), appointment);
        AppointmentStatus status = AppointmentStatus.fromString(request.getParameter("status"));
        if (currentUser(request).getRole() == Role.PATIENT && status != AppointmentStatus.CANCELLED) {
            throw new IllegalArgumentException("Patients can only cancel appointments");
        }
        ServiceFactory.appointmentService().updateStatus(id, status);
    }

    private void delete(HttpServletRequest request) {
        int id = ValidationUtil.parsePositiveId(request.getParameter("id"), "Appointment");
        assertCanAccessAppointment(currentUser(request), ServiceFactory.appointmentService().getById(id));
        ServiceFactory.appointmentService().delete(id);
    }

    private List<Appointment> loadList(HttpServletRequest request, User user) {
        String keyword = request.getParameter("q");
        String status = request.getParameter("status");
        String from = request.getParameter("from");
        String to = request.getParameter("to");

        return switch (user.getRole()) {
            case PATIENT -> ServiceFactory.appointmentService().forPatient(user.getId());
            case DOCTOR, NURSE -> ServiceFactory.appointmentService().forProvider(user.getId());
            default -> ServiceFactory.appointmentService().search(keyword, status, from, to);
        };
    }

    private void loadFormData(HttpServletRequest request, User user) {
        request.setAttribute("doctors", ServiceFactory.userService().listDoctors(null));
        request.setAttribute("nurses", ServiceFactory.userService().listNurses(null));
        if (user.getRole() == Role.ADMIN) {
            request.setAttribute("patients", ServiceFactory.userService().searchUsers(null, Role.PATIENT));
        }
        if (user.getRole() == Role.DOCTOR) {
            request.setAttribute("doctorProfile", ServiceFactory.userService().getDoctorProfile(user.getId()));
        }
    }

    private String appointmentsPath(User user) {
        return switch (user.getRole()) {
            case ADMIN -> "/admin/appointments";
            case DOCTOR -> "/doctor/appointments";
            case NURSE -> "/nurse/appointments";
            case PATIENT -> "/patient/appointments";
        };
    }

    private void assertCanAccessAppointment(User user, Appointment appt) {
        boolean allowed = switch (user.getRole()) {
            case ADMIN -> true;
            case PATIENT -> appt.getPatientId() == user.getId();
            case DOCTOR, NURSE -> appt.getProviderId() == user.getId();
        };
        if (!allowed) {
            throw new IllegalArgumentException("You are not allowed to access this appointment");
        }
    }

    private Integer parseOptionalInt(String raw, String field, int min, int max) {
        if (raw == null || raw.isBlank()) return null;
        try {
            int value = Integer.parseInt(raw.trim());
            if (value < min || value > max) {
                throw new IllegalArgumentException(field + " must be between " + min + " and " + max);
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid " + field);
        }
    }

    private Double parseOptionalDouble(String raw, String field, double min, double max) {
        if (raw == null || raw.isBlank()) return null;
        try {
            double value = Double.parseDouble(raw.trim());
            if (value < min || value > max) {
                throw new IllegalArgumentException(field + " must be between " + min + " and " + max);
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid " + field);
        }
    }
}
