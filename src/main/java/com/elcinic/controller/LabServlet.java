package com.elcinic.controller;

import com.elcinic.model.LabStatus;
import com.elcinic.model.Role;
import com.elcinic.model.User;
import com.elcinic.service.ServiceFactory;
import com.elcinic.utility.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/labs")
public class LabServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        if ("new".equals(request.getParameter("action")) && user.getRole() == Role.DOCTOR) {
            request.setAttribute("patients", ServiceFactory.userService().searchUsers(null, Role.PATIENT));
            request.getRequestDispatcher("/WEB-INF/views/lab-form.jsp").forward(request, response);
            return;
        }
        if ("edit".equals(request.getParameter("action"))) {
            if (user.getRole() == Role.PATIENT) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            int id = ValidationUtil.parsePositiveId(request.getParameter("id"), "Lab test");
            request.setAttribute("labTest", ServiceFactory.labService().getById(id));
            request.getRequestDispatcher("/WEB-INF/views/lab-result.jsp").forward(request, response);
            return;
        }

        request.setAttribute("labTests", switch (user.getRole()) {
            case PATIENT -> ServiceFactory.labService().forPatient(user.getId());
            case DOCTOR -> ServiceFactory.labService().forDoctor(user.getId());
            default -> ServiceFactory.labService().search(request.getParameter("q"), request.getParameter("status"));
        });
        request.getRequestDispatcher("/WEB-INF/views/labs.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = currentUser(request);
        try {
            if ("order".equals(request.getParameter("action"))) {
                if (user.getRole() != Role.DOCTOR && user.getRole() != Role.ADMIN) {
                    throw new IllegalArgumentException("Only doctors/admin can order lab tests");
                }
                ServiceFactory.labService().order(
                        ValidationUtil.parsePositiveId(request.getParameter("patientId"), "Patient"),
                        user.getRole() == Role.DOCTOR ? user.getId()
                                : ValidationUtil.parsePositiveId(request.getParameter("doctorId"), "Doctor"),
                        request.getParameter("testName"),
                        request.getParameter("notes"));
                setFlash(request, "success", "Lab test ordered");
            } else if ("update".equals(request.getParameter("action"))) {
                if (user.getRole() != Role.DOCTOR && user.getRole() != Role.ADMIN) {
                    throw new IllegalArgumentException("Only doctors/admin can update lab results");
                }
                int id = ValidationUtil.parsePositiveId(request.getParameter("id"), "Lab test");
                ServiceFactory.labService().updateResult(
                        id,
                        request.getParameter("resultValue"),
                        request.getParameter("resultUnit"),
                        LabStatus.fromString(request.getParameter("status")),
                        request.getParameter("notes"));
                setFlash(request, "success", "Lab result saved");
            }
        } catch (Exception e) {
            setFlash(request, "error", handleError(request, e));
        }
        response.sendRedirect(redirectWithContext(request, labPath(user)));
    }

    static String labPath(User user) {
        return switch (user.getRole()) {
            case ADMIN -> "/admin/labs";
            case DOCTOR -> "/doctor/labs";
            case PATIENT -> "/patient/labs";
            default -> "/labs";
        };
    }
}
