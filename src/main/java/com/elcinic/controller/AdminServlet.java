package com.elcinic.controller;

import com.elcinic.model.Role;
import com.elcinic.service.ServiceFactory;
import com.elcinic.utility.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = {
        "/admin/dashboard",
        "/admin/users",
        "/admin/appointments",
        "/admin/records",
        "/admin/patients",
        "/admin/billing",
        "/admin/labs",
        "/admin/reports"
})
public class AdminServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        switch (path) {
            case "/admin/dashboard" -> {
                var dashRepo = new com.elcinic.repository.DashboardRepository();
                request.setAttribute("stats", ServiceFactory.dashboardService().forUser(currentUser(request)));
                request.setAttribute("appointments", ServiceFactory.appointmentService().search(null, null, null, null));
                request.setAttribute("weeklyChart", dashRepo.weeklyAppointments());
                request.setAttribute("statusChart", dashRepo.appointmentsByStatus());
                request.setAttribute("revenueChart", dashRepo.revenueLast7Days());
                request.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(request, response);
            }
            case "/admin/billing" -> request.getRequestDispatcher("/billing").include(request, response);
            case "/admin/labs" -> request.getRequestDispatcher("/labs").include(request, response);
            case "/admin/reports" -> {
                request.setAttribute("stats", ServiceFactory.dashboardService().forUser(currentUser(request)));
                request.setAttribute("invoices", ServiceFactory.billingService().search(null, null));
                request.getRequestDispatcher("/WEB-INF/views/admin/reports.jsp").forward(request, response);
            }
            case "/admin/users" -> {
                Role role = null;
                String roleParam = request.getParameter("role");
                if (roleParam != null && !roleParam.isBlank()) {
                    role = Role.fromString(roleParam);
                }
                request.setAttribute("users", ServiceFactory.userService().searchUsers(request.getParameter("q"), role));
                request.getRequestDispatcher("/WEB-INF/views/admin/users.jsp").forward(request, response);
            }
            case "/admin/patients" -> {
                request.setAttribute("patients", ServiceFactory.patientService().listPatients(request.getParameter("q")));
                request.setAttribute("doctors", ServiceFactory.userService().listDoctors(null));
                request.getRequestDispatcher("/WEB-INF/views/admin/patients.jsp").forward(request, response);
            }
            case "/admin/appointments" -> forwardAppointments(request, response);
            case "/admin/records" -> forwardRecords(request, response);
            default -> response.sendError(404);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getServletPath();
        try {
            if ("/admin/users".equals(path)) {
                String action = request.getParameter("action");
                if ("create".equals(action)) {
                    Role role = Role.fromString(request.getParameter("role"));
                    ServiceFactory.userService().createStaff(
                            role,
                            request.getParameter("username"),
                            request.getParameter("password"),
                            request.getParameter("fullName"),
                            request.getParameter("email"),
                            request.getParameter("phone"),
                            request.getParameter("specializationOrDept"),
                            request.getParameter("license")
                    );
                    setFlash(request, "success", "Staff member created");
                } else if ("deactivate".equals(action)) {
                    ServiceFactory.userService().deactivateUser(
                            ValidationUtil.parsePositiveId(request.getParameter("id"), "User"));
                    setFlash(request, "success", "User deactivated");
                }
            } else if ("/admin/patients".equals(path) && "assign".equals(request.getParameter("action"))) {
                ServiceFactory.patientService().assignDoctor(
                        ValidationUtil.parsePositiveId(request.getParameter("patientId"), "Patient"),
                        ValidationUtil.parsePositiveId(request.getParameter("doctorId"), "Doctor"));
                setFlash(request, "success", "Doctor assigned to patient");
            }
        } catch (Exception e) {
            setFlash(request, "error", handleError(request, e));
        }
        response.sendRedirect(redirectWithContext(request, path));
    }

    private void forwardAppointments(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/appointments").include(request, response);
    }

    private void forwardRecords(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/records").include(request, response);
    }
}
