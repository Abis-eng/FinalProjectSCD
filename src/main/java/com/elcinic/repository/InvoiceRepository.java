package com.elcinic.repository;

import com.elcinic.model.Invoice;
import com.elcinic.model.PaymentStatus;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository {
    Optional<Invoice> findByAppointmentId(int appointmentId);

    Optional<Invoice> findById(int id);

    List<Invoice> findByPatient(int patientId);

    List<Invoice> findAll(String keyword, PaymentStatus status);

    int create(Invoice invoice);

    void updatePayment(int id, PaymentStatus status, String paymentMethod);

    double sumPaidToday();
}
