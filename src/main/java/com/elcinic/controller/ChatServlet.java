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
import java.util.List;

@WebServlet(urlPatterns = {"/chat"})
public class ChatServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User current = currentUser(request);
        Integer otherUserId = parseOptionalInt(request.getParameter("userId"));
        Integer appointmentId = parseOptionalInt(request.getParameter("appointmentId"));

        request.setAttribute("chatUsers", listChatUsers(current));
        request.setAttribute("selectedUserId", otherUserId);
        request.setAttribute("selectedAppointmentId", appointmentId);
        if (otherUserId != null) {
            request.setAttribute("messages", ServiceFactory.chatService().thread(current, otherUserId, appointmentId));
            request.setAttribute("chatWithUser", ServiceFactory.userService().getUser(otherUserId));
        }
        request.getRequestDispatcher("/WEB-INF/views/chat.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User current = currentUser(request);
        try {
            int otherUserId = ValidationUtil.parsePositiveId(request.getParameter("userId"), "User");
            Integer appointmentId = parseOptionalInt(request.getParameter("appointmentId"));
            ServiceFactory.chatService().send(current, otherUserId, appointmentId, request.getParameter("content"));
            setFlash(request, "success", "Message sent");
            response.sendRedirect(redirectWithContext(request, "/chat?userId=" + otherUserId
                    + (appointmentId != null ? "&appointmentId=" + appointmentId : "")));
            return;
        } catch (Exception e) {
            setFlash(request, "error", handleError(request, e));
        }
        response.sendRedirect(redirectWithContext(request, "/chat"));
    }

    private Integer parseOptionalInt(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return ValidationUtil.parsePositiveId(raw, "Id");
    }

    private List<User> listChatUsers(User current) {
        List<User> users = ServiceFactory.userService().searchUsers(null, null);
        return users.stream()
                .filter(u -> u.getId() != current.getId())
                .filter(u -> current.getRole() == Role.ADMIN
                        || u.getRole() == Role.ADMIN
                        || (current.getRole() == Role.PATIENT && (u.getRole() == Role.DOCTOR || u.getRole() == Role.NURSE))
                        || ((current.getRole() == Role.DOCTOR || current.getRole() == Role.NURSE) && u.getRole() == Role.PATIENT))
                .toList();
    }
}
