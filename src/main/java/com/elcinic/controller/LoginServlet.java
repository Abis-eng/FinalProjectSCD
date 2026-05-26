package com.elcinic.controller;

import com.elcinic.model.Role;
import com.elcinic.model.User;
import com.elcinic.service.ServiceFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (currentUser(request) != null) {
            response.sendRedirect(dashboardPath(currentUser(request), request));
            return;
        }
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        try {
            User user = ServiceFactory.authService().login(username, password).orElseThrow();
            request.getSession(true).setAttribute(SESSION_USER, user);
            response.sendRedirect(dashboardPath(user, request));
        } catch (Exception e) {
            handleError(request, e);
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
        }
    }

    static String dashboardPath(User user, HttpServletRequest request) {
        String ctx = request.getContextPath();
        return switch (user.getRole()) {
            case ADMIN -> ctx + "/admin/dashboard";
            case DOCTOR -> ctx + "/doctor/dashboard";
            case NURSE -> ctx + "/nurse/dashboard";
            case PATIENT -> ctx + "/patient/dashboard";
        };
    }
}
