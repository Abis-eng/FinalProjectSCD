package com.elcinic.filter;

import com.elcinic.controller.BaseServlet;
import com.elcinic.model.Role;
import com.elcinic.model.User;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebFilter("/*")
public class AuthFilter implements Filter {

    private static final List<String> PUBLIC = List.of(
            "", "/", "/index.jsp", "/login", "/register", "/css/", "/error.jsp"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String path = req.getRequestURI().substring(req.getContextPath().length());

        if (isPublic(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        User user = session != null ? (User) session.getAttribute(BaseServlet.SESSION_USER) : null;

        if (user == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        if (path.startsWith("/admin") && user.getRole() != Role.ADMIN) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (path.startsWith("/doctor") && user.getRole() != Role.DOCTOR) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (path.startsWith("/nurse") && user.getRole() != Role.NURSE) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (path.startsWith("/patient") && user.getRole() != Role.PATIENT) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isPublic(String path) {
        if (PUBLIC.contains(path)) {
            return true;
        }
        return path.startsWith("/css/") || path.endsWith(".css");
    }
}
