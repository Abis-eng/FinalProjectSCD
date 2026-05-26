package com.elcinic.controller;

import com.elcinic.model.Role;
import com.elcinic.model.User;
import com.elcinic.service.ServiceFactory;
import com.elcinic.utility.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;

@WebServlet(name = "RegisterServlet", urlPatterns = {"/register"})
public class RegisterServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String role = request.getParameter("role");
        if (role == null || role.isBlank()) {
            role = "PATIENT";
        }
        request.setAttribute("selectedRole", role.toUpperCase());
        try {
            request.setAttribute("doctors", ServiceFactory.userService().listDoctors(null));
        } catch (Exception e) {
            handleError(request, e);
            request.setAttribute("doctors", Collections.emptyList());
        }
        request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String roleParam = request.getParameter("role");
        try {
            Role role = Role.fromString(roleParam);
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            String fullName = request.getParameter("fullName");
            String email = request.getParameter("email");
            String phone = request.getParameter("phone");

            User user;
            String flashMessage;
            switch (role) {
                case PATIENT -> {
                    user = ServiceFactory.registrationService().registerPatient(
                            username, password, fullName, email, phone);
                    LocalDate dob = null;
                    String dobStr = request.getParameter("dateOfBirth");
                    if (dobStr != null && !dobStr.isBlank()) {
                        dob = ValidationUtil.parseDate(dobStr, "Date of birth");
                    }
                    Integer doctorId = null;
                    String doctorIdStr = request.getParameter("assignedDoctorId");
                    if (doctorIdStr != null && !doctorIdStr.isBlank()) {
                        doctorId = ValidationUtil.parsePositiveId(doctorIdStr, "Doctor");
                    }
                    ServiceFactory.patientService().completeRegistration(
                            user.getId(), dob, request.getParameter("bloodType"), doctorId);
                    flashMessage = "Patient account created. You can log in now.";
                }
                case DOCTOR -> {
                    ServiceFactory.registrationService().registerDoctor(
                            username, password, fullName, email, phone,
                            request.getParameter("specialization"),
                            request.getParameter("licenseNumber"));
                    flashMessage = "Doctor registration submitted. An administrator must approve your account before you can log in.";
                }
                case NURSE -> {
                    ServiceFactory.registrationService().registerNurse(
                            username, password, fullName, email, phone,
                            request.getParameter("department"));
                    flashMessage = "Nurse registration submitted. An administrator must approve your account before you can log in.";
                }
                default -> throw new IllegalArgumentException("Invalid registration role");
            }

            setFlash(request, "success", flashMessage);
            response.sendRedirect(redirectWithContext(request, "/login"));
        } catch (Exception e) {
            handleError(request, e);
            request.setAttribute("doctors", ServiceFactory.userService().listDoctors(null));
            request.setAttribute("selectedRole", roleParam != null ? roleParam.toUpperCase() : "PATIENT");
            request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
        }
    }
}
