package com.elcinic.controller;

import com.elcinic.model.User;
import com.elcinic.utility.ErrorMessages;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;

public abstract class BaseServlet extends HttpServlet {

    public static final String SESSION_USER = "currentUser";

    protected User currentUser(HttpServletRequest request) {
        return (User) request.getSession().getAttribute(SESSION_USER);
    }

    protected void setFlash(HttpServletRequest request, String type, String message) {
        request.getSession().setAttribute("flashType", type);
        request.getSession().setAttribute("flashMessage", message);
    }

    protected String redirectWithContext(HttpServletRequest request, String path) {
        return request.getContextPath() + path;
    }

    protected String handleError(HttpServletRequest request, Exception e) {
        String msg = ErrorMessages.from(e);
        request.setAttribute("error", msg);
        request.getServletContext().log("Request error", e);
        return msg;
    }
}
