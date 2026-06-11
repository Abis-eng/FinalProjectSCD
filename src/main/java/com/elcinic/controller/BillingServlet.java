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

@WebServlet("/billing")
public class BillingServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        if (user.getRole() == Role.DOCTOR || user.getRole() == Role.NURSE) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (user.getRole() == Role.PATIENT) {
            request.setAttribute("invoices", ServiceFactory.billingService().listForPatient(user.getId()));
        } else {
            request.setAttribute("invoices", ServiceFactory.billingService()
                    .search(request.getParameter("q"), request.getParameter("status")));
        }
        request.getRequestDispatcher("/WEB-INF/views/billing.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = currentUser(request);
        String action = request.getParameter("action");
        try {
            int id = ValidationUtil.parsePositiveId(request.getParameter("id"), "Invoice");
            if ("markPaid".equals(action) && user.getRole() == Role.ADMIN) {
                ServiceFactory.billingService().markPaidByAdmin(id, request.getParameter("paymentMethod"));
            } else if ("waive".equals(action) && user.getRole() == Role.ADMIN) {
                ServiceFactory.billingService().waive(id);
            } else {
                throw new IllegalArgumentException("Invalid billing action");
            }
            setFlash(request, "success", "Payment updated");
        } catch (Exception e) {
            setFlash(request, "error", handleError(request, e));
        }
        response.sendRedirect(redirectWithContext(request, billingPath(user)));
    }

    static String billingPath(User user) {
        return switch (user.getRole()) {
            case ADMIN -> "/admin/billing";
            case PATIENT -> "/patient/billing";
            default -> "/billing";
        };
    }
}
