package com.elcinic.controller;

import com.elcinic.model.MedicalRecord;
import com.elcinic.model.Role;
import com.elcinic.model.User;
import com.elcinic.service.ServiceFactory;
import com.elcinic.utility.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "MedicalRecordServlet", urlPatterns = {"/records"})
public class MedicalRecordServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        String action = request.getParameter("action");

        if ("new".equals(action) && (user.getRole() == Role.DOCTOR || user.getRole() == Role.ADMIN)) {
            request.setAttribute("patients", ServiceFactory.userService().searchUsers(null, Role.PATIENT));
            request.getRequestDispatcher("/WEB-INF/views/record-form.jsp").forward(request, response);
            return;
        }

        List<MedicalRecord> records = switch (user.getRole()) {
            case PATIENT -> ServiceFactory.medicalRecordService().forPatient(user.getId());
            case DOCTOR -> ServiceFactory.medicalRecordService().forDoctor(user.getId());
            default -> ServiceFactory.medicalRecordService().search(request.getParameter("q"));
        };
        request.setAttribute("records", records);
        request.getRequestDispatcher("/WEB-INF/views/records.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = currentUser(request);
        if (user.getRole() == Role.PATIENT || user.getRole() == Role.NURSE) {
            setFlash(request, "error", "You are not authorized for this operation");
            response.sendRedirect(recordsPath(user, request));
            return;
        }
        String action = request.getParameter("action");
        try {
            if ("create".equals(action)) {
                int doctorId = user.getRole() == Role.DOCTOR ? user.getId()
                        : ValidationUtil.parsePositiveId(request.getParameter("doctorId"), "Doctor");
                ServiceFactory.medicalRecordService().create(
                        ValidationUtil.parsePositiveId(request.getParameter("patientId"), "Patient"),
                        doctorId,
                        ValidationUtil.parseDate(request.getParameter("visitDate"), "Visit date"),
                        request.getParameter("diagnosis"),
                        request.getParameter("prescription"),
                        request.getParameter("notes")
                );
            } else if ("delete".equals(action)) {
                ServiceFactory.medicalRecordService().delete(
                        ValidationUtil.parsePositiveId(request.getParameter("id"), "Record"));
            }
            setFlash(request, "success", "Medical record updated");
        } catch (Exception e) {
            setFlash(request, "error", handleError(request, e));
        }
        response.sendRedirect(recordsPath(user, request));
    }

    private String recordsPath(User user, HttpServletRequest request) {
        return redirectWithContext(request, switch (user.getRole()) {
            case ADMIN -> "/admin/records";
            case DOCTOR -> "/doctor/records";
            case PATIENT -> "/patient/records";
            default -> "/login";
        });
    }
}
