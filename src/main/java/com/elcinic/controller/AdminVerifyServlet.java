package com.elcinic.controller;

import com.elcinic.model.User;
import com.elcinic.service.ServiceFactory;
import com.elcinic.utility.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = {"/admin/verify"})
public class AdminVerifyServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("pending", ServiceFactory.staffVerificationService().listPending());
        request.getRequestDispatcher("/WEB-INF/views/admin/verify.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            User admin = currentUser(request);
            int staffId = ValidationUtil.parsePositiveId(request.getParameter("userId"), "User");
            String action = request.getParameter("action");
            if ("approve".equals(action)) {
                ServiceFactory.staffVerificationService().approve(admin.getId(), staffId);
                setFlash(request, "success", "Account approved. The user can now log in.");
            } else if ("reject".equals(action)) {
                ServiceFactory.staffVerificationService().reject(
                        admin.getId(), staffId, request.getParameter("reason"));
                setFlash(request, "success", "Registration declined.");
            } else {
                throw new IllegalArgumentException("Unknown action");
            }
        } catch (Exception e) {
            setFlash(request, "error", handleError(request, e));
        }
        response.sendRedirect(redirectWithContext(request, "/admin/verify"));
    }
}
