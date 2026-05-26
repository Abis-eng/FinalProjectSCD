package com.elcinic.service;

import com.elcinic.exception.ServiceException;
import com.elcinic.model.Appointment;
import com.elcinic.model.Invoice;
import com.elcinic.model.PaymentStatus;
import com.elcinic.repository.InvoiceRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public class BillingService {

    private static final Set<String> ALLOWED_PAYMENT_METHODS = Set.of("CARD", "CASH", "ONLINE", "BANK_TRANSFER");

    private final InvoiceRepository invoiceRepository;

    public BillingService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
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

    public void pay(int invoiceId, int patientId, String method) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ServiceException("Invoice not found"));
        if (invoice.getPatientId() != patientId) {
            throw new ServiceException("Not your invoice");
        }
        if (invoice.getStatus() == PaymentStatus.PAID) {
            throw new ServiceException("Already paid");
        }
        invoiceRepository.updatePayment(invoiceId, PaymentStatus.PAID, normalizeMethod(method));
    }

    public void markPaidByAdmin(int invoiceId, String method) {
        invoiceRepository.findById(invoiceId).orElseThrow(() -> new ServiceException("Invoice not found"));
        invoiceRepository.updatePayment(invoiceId, PaymentStatus.PAID, normalizeMethod(method));
    }

    public void waive(int invoiceId) {
        invoiceRepository.findById(invoiceId).orElseThrow(() -> new ServiceException("Invoice not found"));
        invoiceRepository.updatePayment(invoiceId, PaymentStatus.WAIVED, "ADMIN_WAIVER");
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
