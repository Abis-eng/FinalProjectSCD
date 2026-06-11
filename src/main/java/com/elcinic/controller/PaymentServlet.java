package com.elcinic.controller;

import com.elcinic.model.Invoice;
import com.elcinic.model.PaymentCheckoutState;
import com.elcinic.model.Role;
import com.elcinic.model.User;
import com.elcinic.service.ServiceFactory;
import com.elcinic.utility.PaymentValidationUtil;
import com.elcinic.utility.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/payment")
public class PaymentServlet extends BaseServlet {

    public static final String CHECKOUT_SESSION = "paymentCheckout";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        if (user == null || user.getRole() != Role.PATIENT) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String phase = request.getParameter("phase");
        if ("processing".equals(phase)) {
            PaymentCheckoutState checkout = checkoutState(request);
            if (checkout == null) {
                response.sendRedirect(redirectWithContext(request, "/patient/billing"));
                return;
            }
            request.setAttribute("invoiceId", checkout.getInvoiceId());
            request.getRequestDispatcher("/WEB-INF/views/payment-processing.jsp").forward(request, response);
            return;
        }
        if ("success".equals(phase)) {
            int invoiceId = ValidationUtil.parsePositiveId(request.getParameter("invoiceId"), "Invoice");
            Invoice invoice = ServiceFactory.billingService().listForPatient(user.getId()).stream()
                    .filter(i -> i.getId() == invoiceId)
                    .findFirst()
                    .orElse(null);
            request.setAttribute("invoice", invoice);
            request.setAttribute("reference", request.getParameter("ref"));
            request.getRequestDispatcher("/WEB-INF/views/payment-success.jsp").forward(request, response);
            clearCheckout(request);
            return;
        }

        int invoiceId = ValidationUtil.parsePositiveId(request.getParameter("invoiceId"), "Invoice");
        Invoice invoice = ServiceFactory.billingService().getInvoiceForPatient(invoiceId, user.getId());
        PaymentCheckoutState checkout = checkoutState(request);
        if (checkout == null || checkout.getInvoiceId() != invoiceId) {
            checkout = new PaymentCheckoutState();
            checkout.setInvoiceId(invoiceId);
            request.getSession().setAttribute(CHECKOUT_SESSION, checkout);
        }

        int step = parseStep(request.getParameter("step"), 1);
        request.setAttribute("invoice", invoice);
        request.setAttribute("checkout", checkout);
        request.setAttribute("step", step);
        request.getRequestDispatcher("/WEB-INF/views/payment.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = currentUser(request);
        if (user == null || user.getRole() != Role.PATIENT) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String action = request.getParameter("action");
        if ("finalize".equals(action)) {
            finalizePayment(request, response, user);
            return;
        }

        PaymentCheckoutState checkout = checkoutState(request);
        if (checkout == null) {
            response.sendRedirect(redirectWithContext(request, "/patient/billing"));
            return;
        }

        try {
            int step = parseStep(request.getParameter("step"), 1);
            switch (step) {
                case 1 -> handleMethodStep(request, checkout);
                case 2 -> handleDetailsStep(request, checkout);
                case 3 -> {
                    ServiceFactory.billingService().getInvoiceForPatient(checkout.getInvoiceId(), user.getId());
                }
                default -> throw new IllegalArgumentException("Invalid checkout step");
            }
            int nextStep = step + 1;
            if (step == 3) {
                response.sendRedirect(redirectWithContext(request,
                        "/payment?phase=processing&invoiceId=" + checkout.getInvoiceId()));
                return;
            }
            response.sendRedirect(redirectWithContext(request,
                    "/payment?invoiceId=" + checkout.getInvoiceId() + "&step=" + nextStep));
        } catch (Exception e) {
            setFlash(request, "error", handleError(request, e));
            response.sendRedirect(redirectWithContext(request,
                    "/payment?invoiceId=" + checkout.getInvoiceId() + "&step=" + parseStep(request.getParameter("step"), 1)));
        }
    }

    private void handleMethodStep(HttpServletRequest request, PaymentCheckoutState checkout) {
        String method = request.getParameter("method");
        PaymentValidationUtil.requirePatientOnlineMethod(method);
        checkout.setMethod(method.trim().toUpperCase());
        request.getSession().setAttribute(CHECKOUT_SESSION, checkout);
    }

    private void handleDetailsStep(HttpServletRequest request, PaymentCheckoutState checkout) {
        String method = checkout.getMethod();
        if ("CARD".equals(method)) {
            PaymentValidationUtil.validateCardHolderName(request.getParameter("cardHolderName"));
            String digits = PaymentValidationUtil.normalizeCardNumber(request.getParameter("cardNumber"));
            PaymentValidationUtil.normalizeExpiry(request.getParameter("cardExpiry"));
            PaymentValidationUtil.validateCvv(request.getParameter("cardCvv"));
            checkout.setCardHolderName(request.getParameter("cardHolderName").trim());
            checkout.setCardExpiry(request.getParameter("cardExpiry").trim());
            checkout.setCardLast4(digits.substring(digits.length() - 4));
            checkout.setMaskedCard(PaymentValidationUtil.maskCardNumber(digits));
        } else if ("ONLINE".equals(method)) {
            checkout.setMobileNumber(PaymentValidationUtil.normalizeMobileWallet(request.getParameter("mobileNumber")));
            PaymentValidationUtil.validateWalletOtp(request.getParameter("walletOtp"));
        } else if ("BANK_TRANSFER".equals(method)) {
            PaymentValidationUtil.validateBankTransfer(
                    request.getParameter("bankName"),
                    request.getParameter("bankTransactionRef"));
            checkout.setBankName(request.getParameter("bankName").trim());
            checkout.setBankTransactionRef(request.getParameter("bankTransactionRef").trim());
        } else {
            throw new IllegalArgumentException("Unsupported payment method");
        }
        request.getSession().setAttribute(CHECKOUT_SESSION, checkout);
    }

    private void finalizePayment(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        PaymentCheckoutState checkout = checkoutState(request);
        if (checkout == null || checkout.getMethod() == null) {
            response.sendRedirect(redirectWithContext(request, "/patient/billing"));
            return;
        }
        try {
            String reference = ServiceFactory.billingService()
                    .completeOnlineCheckout(checkout.getInvoiceId(), user.getId(), checkout);
            ServiceFactory.userService().searchUsers(null, Role.ADMIN).forEach(admin ->
                    ServiceFactory.notificationService().notify(
                            admin.getId(),
                            "Invoice paid online",
                            "Invoice #" + checkout.getInvoiceId() + " paid via " + checkout.getMethod()
                                    + ". Reference: " + reference
                    ));
            response.sendRedirect(redirectWithContext(request,
                    "/payment?phase=success&invoiceId=" + checkout.getInvoiceId() + "&ref=" + reference));
        } catch (Exception e) {
            setFlash(request, "error", handleError(request, e));
            response.sendRedirect(redirectWithContext(request,
                    "/payment?invoiceId=" + checkout.getInvoiceId() + "&step=3"));
        }
    }

    private PaymentCheckoutState checkoutState(HttpServletRequest request) {
        return (PaymentCheckoutState) request.getSession().getAttribute(CHECKOUT_SESSION);
    }

    private void clearCheckout(HttpServletRequest request) {
        request.getSession().removeAttribute(CHECKOUT_SESSION);
    }

    private int parseStep(String step, int defaultStep) {
        if (step == null || step.isBlank()) {
            return defaultStep;
        }
        int value = Integer.parseInt(step);
        if (value < 1 || value > 3) {
            throw new IllegalArgumentException("Invalid step");
        }
        return value;
    }
}
