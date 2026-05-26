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
import java.math.BigDecimal;

@WebServlet("/profile")
public class ProfileServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        request.setAttribute("profileUser", ServiceFactory.userService().getUser(user.getId()));
        if (user.getRole() == Role.PATIENT) {
            request.setAttribute("patientProfile", ServiceFactory.patientService().getProfile(user.getId()));
        }
        if (user.getRole() == Role.DOCTOR) {
            request.setAttribute("doctorProfile", ServiceFactory.userService().getDoctorProfile(user.getId()));
        }
        request.getRequestDispatcher("/WEB-INF/views/profile-settings.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = currentUser(request);
        try {
            if ("password".equals(request.getParameter("action"))) {
                ServiceFactory.authService().changePassword(
                        user.getId(),
                        request.getParameter("currentPassword"),
                        request.getParameter("newPassword"));
                setFlash(request, "success", "Password changed successfully");
            } else if ("fee".equals(request.getParameter("action")) && user.getRole() == Role.DOCTOR) {
                String feeStr = request.getParameter("consultationFee");
                ValidationUtil.requireNonBlank(feeStr, "Consultation fee");
                BigDecimal fee = new BigDecimal(feeStr.trim());
                ServiceFactory.userService().updateConsultationFee(user.getId(), fee);
                setFlash(request, "success", "Your consultation fee has been updated (PKR).");
            }
        } catch (Exception e) {
            setFlash(request, "error", handleError(request, e));
        }
        response.sendRedirect(redirectWithContext(request, "/profile"));
    }
}
