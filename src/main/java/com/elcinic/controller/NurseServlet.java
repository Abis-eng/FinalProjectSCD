package com.elcinic.controller;

import com.elcinic.service.ServiceFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = {"/nurse/dashboard", "/nurse/appointments"})
public class NurseServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        if ("/nurse/dashboard".equals(path)) {
            request.setAttribute("appointments",
                    ServiceFactory.appointmentService().forProvider(currentUser(request).getId()));
            request.getRequestDispatcher("/WEB-INF/views/nurse/dashboard.jsp").forward(request, response);
        } else if ("/nurse/appointments".equals(path)) {
            request.getRequestDispatcher("/appointments").include(request, response);
        } else {
            response.sendError(404);
        }
    }
}
