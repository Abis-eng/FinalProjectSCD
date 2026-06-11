package com.elcinic.controller;

import com.elcinic.service.ServiceFactory;
import com.elcinic.utility.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = {
        "/patient/dashboard",
        "/patient/appointments",
        "/patient/records",
        "/patient/profile",
        "/patient/billing",
        "/patient/labs"
})
public class PatientServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int patientId = currentUser(request).getId();
        String path = request.getServletPath();

        switch (path) {
            case "/patient/dashboard" -> {
                request.setAttribute("stats", ServiceFactory.dashboardService().forUser(currentUser(request)));
                request.setAttribute("profile", ServiceFactory.patientService().getProfile(patientId));
                request.setAttribute("appointments", ServiceFactory.appointmentService().forPatient(patientId));
                request.getRequestDispatcher("/WEB-INF/views/patient/dashboard.jsp").forward(request, response);
            }
            case "/patient/billing" -> request.getRequestDispatcher("/billing").include(request, response);
            case "/patient/labs" -> request.getRequestDispatcher("/labs").include(request, response);
            case "/patient/appointments" -> request.getRequestDispatcher("/appointments").include(request, response);
            case "/patient/records" -> request.getRequestDispatcher("/records").include(request, response);
            case "/patient/profile" -> {
                request.setAttribute("profile", ServiceFactory.patientService().getProfile(patientId));
                request.setAttribute("doctors", ServiceFactory.patientService().availableDoctors(null));
                request.getRequestDispatcher("/WEB-INF/views/patient/profile.jsp").forward(request, response);
            }
            default -> response.sendError(404);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int patientId = currentUser(request).getId();
        String path = request.getServletPath();
        if (!"/patient/profile".equals(path)) {
            response.sendError(405);
            return;
        }
        try {
            if ("requestDoctor".equals(request.getParameter("action"))) {
                int doctorId = ValidationUtil.parsePositiveId(request.getParameter("doctorId"), "Doctor");
                ServiceFactory.patientService().requestDoctor(patientId, doctorId);
                String patientName = currentUser(request).getFullName();
                String doctorName = ServiceFactory.userService().getUser(doctorId).getFullName();
                ServiceFactory.userService().searchUsers(null, com.elcinic.model.Role.ADMIN)
                        .forEach(admin -> ServiceFactory.notificationService().notify(
                                admin.getId(),
                                "Doctor request from patient",
                                patientName + " requested Dr. " + doctorName + ". Please review in Admin > Patients."
                        ));
                setFlash(request, "success", "Doctor request sent to admin for approval.");
            }
        } catch (Exception e) {
            setFlash(request, "error", handleError(request, e));
        }
        response.sendRedirect(redirectWithContext(request, "/patient/profile"));
    }
}
