package com.elcinic.service;

import com.elcinic.exception.ServiceException;
import com.elcinic.model.Appointment;
import com.elcinic.model.AppointmentStatus;
import com.elcinic.model.Invoice;
import com.elcinic.model.PaymentCheckoutState;
import com.elcinic.model.PaymentStatus;
import com.elcinic.repository.AppointmentRepository;
import com.elcinic.repository.InvoiceRepository;
import com.elcinic.utility.PaymentValidationUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class BillingService {

    private static final Set<String> ALLOWED_PAYMENT_METHODS = Set.of("CARD", "CASH", "ONLINE", "BANK_TRANSFER");
    private static final Set<String> PATIENT_ONLINE_METHODS = Set.of("CARD", "ONLINE", "BANK_TRANSFER");

    private final InvoiceRepository invoiceRepository;
    private final AppointmentRepository appointmentRepository;
    private final NotificationService notificationService;

    public BillingService(InvoiceRepository invoiceRepository) {
        this(invoiceRepository, null, null);
    }

    public BillingService(InvoiceRepository invoiceRepository,
                          AppointmentRepository appointmentRepository,
                          NotificationService notificationService) {
        this.invoiceRepository = invoiceRepository;
        this.appointmentRepository = appointmentRepository;
        this.notificationService = notificationService;
    }

    public void createForAppointment(Appointment appointment) {
        if (invoiceRepository.findByAppointmentId(appointment.getId()).isPresent()) {
            return;
        }
        Invoice invoice = new Invoice();
        invoice.setAppointmentId(appointment.getId());
        invoice.setPatientId(appointment.getPatientId());
        if (appointment.getFeeAmount() != null) {
            invoice.setAmount(appointment.getFeeAmount());
        } else {
            invoice.setAmount(BigDecimal.valueOf(3000 * appointment.getAppointmentType().feeMultiplier())
                    .setScale(0, java.math.RoundingMode.HALF_UP));
        }
        invoice.setStatus(PaymentStatus.PENDING);
        invoiceRepository.create(invoice);
    }

    public List<Invoice> listForPatient(int patientId) {
        return invoiceRepository.findByPatient(patientId);
    }

    public List<Invoice> search(String keyword, String status) {
        PaymentStatus st = null;
        if (status != null && !status.isBlank()) {
            st = PaymentStatus.fromString(status);
        }
        return invoiceRepository.findAll(keyword, st);
    }

    public Invoice getInvoiceForPatient(int invoiceId, int patientId) {
        return requirePayableInvoice(invoiceId, patientId);
    }

    public String completeOnlineCheckout(int invoiceId, int patientId, PaymentCheckoutState checkout) {
        Invoice invoice = requirePayableInvoice(invoiceId, patientId);
        PaymentValidationUtil.requirePatientOnlineMethod(checkout.getMethod());
        String method = checkout.getMethod().trim().toUpperCase();
        String reference = generatePaymentReference(invoiceId, method);
        String cardLast4 = "CARD".equals(method) ? checkout.getCardLast4() : null;
        invoiceRepository.updatePayment(invoiceId, PaymentStatus.PAID, method, reference, cardLast4);
        notifyPaymentParties(invoice, method, reference);
        return reference;
    }

    public void pay(int invoiceId, int patientId, String method) {
        Invoice invoice = requirePayableInvoice(invoiceId, patientId);
        String normalized = normalizePatientMethod(method);
        invoiceRepository.updatePayment(invoiceId, PaymentStatus.PAID, normalized, null, null);
        notifyPaymentParties(invoice, normalized, null);
    }

    public void markPaidByAdmin(int invoiceId, String method) {
        Invoice invoice = invoiceRepository.findById(invoiceId).orElseThrow(() -> new ServiceException("Invoice not found"));
        String normalized = normalizeMethod(method);
        invoiceRepository.updatePayment(invoiceId, PaymentStatus.PAID, normalized, null, null);
        notifyPaymentParties(invoice, normalized, null);
    }

    public void waive(int invoiceId) {
        invoiceRepository.findById(invoiceId).orElseThrow(() -> new ServiceException("Invoice not found"));
        invoiceRepository.updatePayment(invoiceId, PaymentStatus.WAIVED, "ADMIN_WAIVER", null, null);
    }

    private Invoice requirePayableInvoice(int invoiceId, int patientId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ServiceException("Invoice not found"));
        if (invoice.getPatientId() != patientId) {
            throw new ServiceException("Not your invoice");
        }
        if (invoice.getStatus() != PaymentStatus.PENDING) {
            throw new ServiceException("This invoice is already settled");
        }
        ensureAppointmentPayable(invoice.getAppointmentId());
        return invoice;
    }

    private void ensureAppointmentPayable(int appointmentId) {
        if (appointmentRepository == null) {
            return;
        }
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ServiceException("Linked appointment not found"));
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new ServiceException("Cannot pay for a cancelled appointment");
        }
    }

    private void notifyPaymentParties(Invoice invoice, String method, String reference) {
        if (notificationService == null) {
            return;
        }
        String amount = invoice.getAmount() != null ? invoice.getAmount().toPlainString() : "0";
        String patientLabel = invoice.getPatientName() != null && !invoice.getPatientName().isBlank()
                ? invoice.getPatientName() : "Patient";
        String dateLabel = invoice.getAppointmentDate() != null ? invoice.getAppointmentDate() : "scheduled visit";
        String refLabel = reference != null && !reference.isBlank() ? reference : "N/A";

        notificationService.notify(
                invoice.getPatientId(),
                "Payment received",
                "Your payment of Rs. " + amount + " via " + method + " was successful. Reference: " + refLabel
        );

        if (appointmentRepository == null) {
            return;
        }
        appointmentRepository.findById(invoice.getAppointmentId()).ifPresent(appt ->
                notificationService.notify(
                        appt.getProviderId(),
                        "Patient payment received",
                        patientLabel + " paid Rs. " + amount + " via " + method
                                + " for the appointment on " + dateLabel
                                + ". Reference: " + refLabel
                                + ". Payment is complete — you may proceed with the visit."
                )
        );
    }

    private String generatePaymentReference(int invoiceId, String method) {
        String prefix = switch (method) {
            case "CARD" -> "ELC-CARD";
            case "ONLINE" -> "ELC-WALLET";
            case "BANK_TRANSFER" -> "ELC-BANK";
            default -> "ELC-PAY";
        };
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int suffix = ThreadLocalRandom.current().nextInt(1000, 9999);
        return prefix + "-" + date + "-INV" + invoiceId + "-" + suffix;
    }

    private String normalizePatientMethod(String method) {
        if (method == null || method.isBlank()) {
            throw new ServiceException("Payment method is required");
        }
        String normalized = method.trim().toUpperCase();
        if (!PATIENT_ONLINE_METHODS.contains(normalized)) {
            throw new ServiceException("Use the secure checkout for card or online payment");
        }
        return normalized;
    }

    private String normalizeMethod(String method) {
        if (method == null || method.isBlank()) {
            throw new ServiceException("Payment method is required");
        }
        String normalized = method.trim().toUpperCase();
        if (!ALLOWED_PAYMENT_METHODS.contains(normalized)) {
            throw new ServiceException("Unsupported payment method");
        }
        return normalized;
    }
}
