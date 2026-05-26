package com.elcinic.service;

import com.elcinic.exception.ServiceException;
import com.elcinic.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class BillingServiceTest {

    private final Map<Integer, Invoice> invoices = new HashMap<>();
    private BillingService billingService;
    private int invoiceSeq = 1;

    @BeforeEach
    void setUp() {
        invoices.clear();
        invoiceSeq = 1;
        billingService = new BillingService(new com.elcinic.repository.InvoiceRepository() {
            @Override
            public Optional<Invoice> findByAppointmentId(int appointmentId) {
                return invoices.values().stream()
                        .filter(i -> i.getAppointmentId() == appointmentId).findFirst();
            }

            @Override
            public Optional<Invoice> findById(int id) {
                return Optional.ofNullable(invoices.get(id));
            }

            @Override
            public List<Invoice> findByPatient(int patientId) {
                return invoices.values().stream().filter(i -> i.getPatientId() == patientId).toList();
            }

            @Override
            public List<Invoice> findAll(String keyword, PaymentStatus status) {
                return List.copyOf(invoices.values());
            }

            @Override
            public int create(Invoice invoice) {
                invoice.setId(invoiceSeq++);
                invoices.put(invoice.getId(), invoice);
                return invoice.getId();
            }

            @Override
            public void updatePayment(int id, PaymentStatus status, String paymentMethod) {
                Invoice inv = invoices.get(id);
                if (inv != null) {
                    inv.setStatus(status);
                    inv.setPaymentMethod(paymentMethod);
                }
            }

            @Override
            public double sumPaidToday() {
                return 0;
            }
        });
    }

    @Test
    void createForAppointment_usesAppointmentFee() {
        Appointment a = appointment(1, 10, BigDecimal.valueOf(4500));
        billingService.createForAppointment(a);
        Invoice inv = billingService.listForPatient(10).get(0);
        assertEquals(0, BigDecimal.valueOf(4500).compareTo(inv.getAmount()));
        assertEquals(PaymentStatus.PENDING, inv.getStatus());
    }

    @Test
    void createForAppointment_idempotent() {
        Appointment a = appointment(1, 10, BigDecimal.valueOf(3000));
        billingService.createForAppointment(a);
        billingService.createForAppointment(a);
        assertEquals(1, billingService.listForPatient(10).size());
    }

    @Test
    void pay_wrongPatientRejected() {
        Appointment a = appointment(2, 10, BigDecimal.valueOf(2000));
        billingService.createForAppointment(a);
        int invId = billingService.listForPatient(10).get(0).getId();
        assertThrows(ServiceException.class, () -> billingService.pay(invId, 99, "CASH"));
    }

    @Test
    void pay_success() {
        Appointment a = appointment(3, 10, BigDecimal.valueOf(2000));
        billingService.createForAppointment(a);
        int invId = billingService.listForPatient(10).get(0).getId();
        billingService.pay(invId, 10, "ONLINE");
        assertEquals(PaymentStatus.PAID, invoices.get(invId).getStatus());
    }

    @Test
    void pay_rejectsInvalidMethod() {
        Appointment a = appointment(4, 10, BigDecimal.valueOf(2000));
        billingService.createForAppointment(a);
        int invId = billingService.listForPatient(10).get(0).getId();
        assertThrows(ServiceException.class, () -> billingService.pay(invId, 10, "BITCOIN"));
    }

    @Test
    void pay_rejectsBlankMethod() {
        Appointment a = appointment(5, 10, BigDecimal.valueOf(2000));
        billingService.createForAppointment(a);
        int invId = billingService.listForPatient(10).get(0).getId();
        assertThrows(ServiceException.class, () -> billingService.pay(invId, 10, ""));
    }

    @Test
    void waive_setsWaivedStatus() {
        Appointment a = appointment(6, 10, BigDecimal.valueOf(2000));
        billingService.createForAppointment(a);
        int invId = billingService.listForPatient(10).get(0).getId();
        billingService.waive(invId);
        assertEquals(PaymentStatus.WAIVED, invoices.get(invId).getStatus());
    }

    private static Appointment appointment(int id, int patientId, BigDecimal fee) {
        Appointment a = new Appointment();
        a.setId(id);
        a.setPatientId(patientId);
        a.setProviderId(20);
        a.setProviderType(ProviderType.DOCTOR);
        a.setAppointmentDate(java.time.LocalDate.now().plusDays(1));
        a.setTimeSlot("09:00-09:30");
        a.setStatus(AppointmentStatus.PENDING);
        a.setAppointmentType(AppointmentType.CONSULTATION);
        a.setFeeAmount(fee);
        return a;
    }
}
