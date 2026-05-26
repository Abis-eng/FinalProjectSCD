package com.elcinic.controller;

import com.elcinic.model.User;
import com.elcinic.service.ServiceFactory;
import com.elcinic.utility.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/notifications")
public class NotificationServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        request.setAttribute("notifications", ServiceFactory.notificationService()
                .list(user.getId(), "1".equals(request.getParameter("unread"))));
        request.setAttribute("unreadCount", ServiceFactory.notificationService().unreadCount(user.getId()));
        request.getRequestDispatcher("/WEB-INF/views/notifications.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = currentUser(request);
        if ("readAll".equals(request.getParameter("action"))) {
            ServiceFactory.notificationService().markAllRead(user.getId());
        } else {
            int id = ValidationUtil.parsePositiveId(request.getParameter("id"), "Notification");
            ServiceFactory.notificationService().markRead(id, user.getId());
        }
        response.sendRedirect(redirectWithContext(request, "/notifications"));
    }
}
