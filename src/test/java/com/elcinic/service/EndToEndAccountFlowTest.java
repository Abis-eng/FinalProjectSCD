package com.elcinic.service;

import com.elcinic.model.*;
import com.elcinic.repository.*;
import com.elcinic.testsupport.InMemoryDoctorRepository;
import com.elcinic.testsupport.InMemoryUserRepository;
import com.elcinic.utility.PasswordHasher;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simulates realistic multi-role signup: patient immediate login, doctor/nurse pending → approve → book → bill.
 */
class EndToEndAccountFlowTest {

    @Test
    void fullClinicFlow_allRoles() {
        InMemoryUserRepository users = new InMemoryUserRepository();
        InMemoryDoctorRepository doctors = new InMemoryDoctorRepository();
        List<Appointment> appointments = new ArrayList<>();
        List<Notification> notifications = new ArrayList<>();
        List<Invoice> invoices = new ArrayList<>();

        User admin = seed(users, 1, "admin", Role.ADMIN, AccountStatus.ACTIVE);

        RegistrationService reg = registration(users, doctors, notifications);
        User patient = reg.registerPatient("pt_ali", "secret12", "Ali Raza", "ali@test.com", "03001234567");
        reg.registerDoctor("dr_sara", "secret12", "Dr Sara", "sara@clinic.com", null, "Pediatrics", "MD-500");
        reg.registerNurse("nr_omar", "secret12", "Omar Nurse", "omar@clinic.com", null, "OPD");

        assertEquals(AccountStatus.ACTIVE, users.findById(patient.getId()).orElseThrow().getAccountStatus());
        assertEquals(AccountStatus.PENDING, users.findByUsernameForLogin("dr_sara").orElseThrow().getAccountStatus());

        AuthService auth = new AuthService(users);
        assertDoesNotThrow(() -> auth.login("pt_ali", "secret12"));
        assertThrows(Exception.class, () -> auth.login("dr_sara", "secret12"));

        StaffVerificationService verify = new StaffVerificationService(users, new NotificationService(notifRepo(notifications)));
        verify.approve(admin.getId(), users.findByUsernameForLogin("dr_sara").orElseThrow().getId());

        DoctorProfile docProfile = new DoctorProfile();
        docProfile.setUserId(users.findByUsernameForLogin("dr_sara").orElseThrow().getId());
        docProfile.setConsultationFee(BigDecimal.valueOf(3500));
        docProfile.setSpecialization("Pediatrics");
        docProfile.setLicenseNumber("MD-500");
        doctors.put(docProfile);

        User doctor = auth.login("dr_sara", "secret12").orElseThrow();
        assertEquals(Role.DOCTOR, doctor.getRole());

        AppointmentService appts = appointmentService(users, doctors, appointments, invoices, notifications);
        int apptId = appts.book(patient.getId(), doctor.getId(), ProviderType.DOCTOR,
                LocalDate.now().plusDays(2), "09:00-09:30", "Fever",
                AppointmentType.CONSULTATION, Priority.NORMAL, "High temp");

        assertEquals(0, BigDecimal.valueOf(3500).compareTo(appts.getById(apptId).getFeeAmount()));
        assertEquals("Ali Raza", patient.getFullName());

        BillingService billing = billingService(invoices);
        billing.createForAppointment(appts.getById(apptId));
        assertEquals(1, billing.listForPatient(patient.getId()).size());
        billing.pay(billing.listForPatient(patient.getId()).get(0).getId(), patient.getId(), "CASH");
        assertEquals(PaymentStatus.PAID, billing.listForPatient(patient.getId()).get(0).getStatus());

        assertTrue(notifications.size() >= 2);
    }

    private static User seed(InMemoryUserRepository users, int id, String username, Role role, AccountStatus status) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setPasswordHash(PasswordHasher.hash("secret12"));
        u.setFullName(username);
        u.setEmail(username + "@clinic.com");
        u.setRole(role);
        u.setActive(true);
        u.setAccountStatus(status);
        users.seed(u);
        return u;
    }

    private static RegistrationService registration(InMemoryUserRepository users,
                                                    InMemoryDoctorRepository doctors,
                                                    List<Notification> notifications) {
        return new RegistrationService(users, doctors, stubNurses(), new NotificationService(notifRepo(notifications)));
    }

    private static NurseRepository stubNurses() {
        return new NurseRepository() {
            @Override
            public Optional<NurseProfile> findByUserId(int userId) {
                return Optional.empty();
            }

            @Override
            public List<NurseProfile> findAll(String keyword) {
                return List.of();
            }

            @Override
            public void create(int userId, NurseProfile profile) {
            }
        };
    }

    private static NotificationRepository notifRepo(List<Notification> list) {
        return new NotificationRepository() {
            @Override
            public List<Notification> findByUser(int userId, boolean unreadOnly) {
                return list.stream().filter(n -> n.getUserId() == userId).toList();
            }

            @Override
            public int countUnread(int userId) {
                return 0;
            }

            @Override
            public int create(Notification n) {
                list.add(n);
                return list.size();
            }

            @Override
            public void markRead(int id, int userId) {
            }

            @Override
            public void markAllRead(int userId) {
            }
        };
    }

    private static AppointmentService appointmentService(InMemoryUserRepository users,
                                                         InMemoryDoctorRepository doctors,
                                                         List<Appointment> store,
                                                         List<Invoice> invoices,
                                                         List<Notification> notifications) {
        return new AppointmentService(stubAppointments(store), users, doctors,
                billingService(invoices), new NotificationService(notifRepo(notifications)));
    }

    private static BillingService billingService(List<Invoice> store) {
        return new BillingService(new InvoiceRepository() {
            private int seq = 1;

            @Override
            public Optional<Invoice> findByAppointmentId(int appointmentId) {
                return store.stream().filter(i -> i.getAppointmentId() == appointmentId).findFirst();
            }

            @Override
            public Optional<Invoice> findById(int id) {
                return store.stream().filter(i -> i.getId() == id).findFirst();
            }

            @Override
            public List<Invoice> findByPatient(int patientId) {
                return store.stream().filter(i -> i.getPatientId() == patientId).toList();
            }

            @Override
            public List<Invoice> findAll(String keyword, PaymentStatus status) {
                return store;
            }

            @Override
            public int create(Invoice invoice) {
                invoice.setId(seq++);
                store.add(invoice);
                return invoice.getId();
            }

            @Override
            public void updatePayment(int id, PaymentStatus status, String paymentMethod) {
                store.stream().filter(i -> i.getId() == id).findFirst()
                        .ifPresent(i -> {
                            i.setStatus(status);
                            i.setPaymentMethod(paymentMethod);
                        });
            }

            @Override
            public double sumPaidToday() {
                return 0;
            }
        });
    }

    private static AppointmentRepository stubAppointments(List<Appointment> store) {
        return new AppointmentRepository() {
            private int seq = 1;

            @Override
            public Optional<Appointment> findById(int id) {
                return store.stream().filter(a -> a.getId() == id).findFirst();
            }

            @Override
            public List<Appointment> findAll(String keyword, AppointmentStatus status, LocalDate from, LocalDate to) {
                return store;
            }

            @Override
            public List<Appointment> findByPatient(int patientId) {
                return store.stream().filter(a -> a.getPatientId() == patientId).toList();
            }

            @Override
            public List<Appointment> findByProvider(int providerId) {
                return store.stream().filter(a -> a.getProviderId() == providerId).toList();
            }

            @Override
            public int create(Appointment appointment) {
                appointment.setId(seq++);
                store.add(appointment);
                return appointment.getId();
            }

            @Override
            public void update(Appointment appointment) {
            }

            @Override
            public void updateStatus(int id, AppointmentStatus status) {
                findById(id).ifPresent(a -> a.setStatus(status));
            }

            @Override
            public void delete(int id) {
                store.removeIf(a -> a.getId() == id);
            }

            @Override
            public boolean hasConflict(int providerId, LocalDate date, String timeSlot, Integer excludeId) {
                return store.stream().anyMatch(a ->
                        a.getProviderId() == providerId
                                && a.getAppointmentDate().equals(date)
                                && a.getTimeSlot().equals(timeSlot)
                                && a.getStatus() != AppointmentStatus.CANCELLED);
            }
        };
    }
}
