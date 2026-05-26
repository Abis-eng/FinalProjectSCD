package com.elcinic.controller;

import com.elcinic.service.ServiceFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = {
        "/doctor/dashboard", "/doctor/appointments", "/doctor/records",
        "/doctor/patients", "/doctor/labs", "/doctor/billing"
})
public class DoctorServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int doctorId = currentUser(request).getId();
        String path = request.getServletPath();

        switch (path) {
            case "/doctor/dashboard" -> {
                request.setAttribute("stats", ServiceFactory.dashboardService().forUser(currentUser(request)));
                request.setAttribute("appointments", ServiceFactory.appointmentService().forProvider(doctorId));
                request.setAttribute("records", ServiceFactory.medicalRecordService().forDoctor(doctorId));
                request.getRequestDispatcher("/WEB-INF/views/doctor/dashboard.jsp").forward(request, response);
            }
            case "/doctor/labs" -> request.getRequestDispatcher("/labs").include(request, response);
            case "/doctor/billing" -> request.getRequestDispatcher("/billing").include(request, response);
            case "/doctor/appointments" -> request.getRequestDispatcher("/appointments").include(request, response);
            case "/doctor/records" -> request.getRequestDispatcher("/records").include(request, response);
            case "/doctor/patients" -> {
                request.setAttribute("patients", ServiceFactory.patientService()
                        .listPatientsForDoctor(doctorId, request.getParameter("q")));
                request.getRequestDispatcher("/WEB-INF/views/doctor/patients.jsp").forward(request, response);
            }
            default -> response.sendError(404);
        }
    }
}
