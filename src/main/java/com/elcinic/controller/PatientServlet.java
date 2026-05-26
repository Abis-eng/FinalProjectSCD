package com.elcinic.controller;

import com.elcinic.service.ServiceFactory;
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
}
