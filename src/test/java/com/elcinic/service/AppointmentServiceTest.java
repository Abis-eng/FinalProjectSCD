package com.elcinic.service;

import com.elcinic.exception.ServiceException;
import com.elcinic.model.*;
import com.elcinic.repository.AppointmentRepository;
import com.elcinic.repository.DoctorRepository;
import com.elcinic.repository.InvoiceRepository;
import com.elcinic.repository.NotificationRepository;
import com.elcinic.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentServiceTest {

    private final List<Appointment> store = new ArrayList<>();
    private AppointmentService service;
    private User patient;
    private User doctor;

    @BeforeEach
    void setUp() {
        store.clear();
        patient = user(10, Role.PATIENT);
        doctor = user(20, Role.DOCTOR);

        UserRepository users = stubUserRepo();
        AppointmentRepository appointments = stubAppointmentRepo();
        InvoiceRepository invoices = new InvoiceRepository() {
            @Override
            public Optional<com.elcinic.model.Invoice> findByAppointmentId(int appointmentId) {
                return Optional.empty();
            }

            @Override
            public Optional<com.elcinic.model.Invoice> findById(int id) {
                return Optional.empty();
            }

            @Override
            public List<com.elcinic.model.Invoice> findByPatient(int patientId) {
                return List.of();
            }

            @Override
            public List<com.elcinic.model.Invoice> findAll(String keyword, PaymentStatus status) {
                return List.of();
            }

            @Override
            public int create(com.elcinic.model.Invoice invoice) {
                return 1;
            }

            @Override
            public void updatePayment(int id, PaymentStatus status, String paymentMethod) {
            }

            @Override
            public void updatePayment(int id, PaymentStatus status, String paymentMethod,
                                        String paymentReference, String cardLast4) {
            }

            @Override
            public double sumPaidToday() {
                return 0;
            }
        };
        NotificationRepository notifications = new NotificationRepository() {
            @Override
            public List<Notification> findByUser(int userId, boolean unreadOnly) {
                return List.of();
            }

            @Override
            public int countUnread(int userId) {
                return 0;
            }

            @Override
            public int create(Notification n) {
                return 1;
            }

            @Override
            public void markRead(int id, int userId) {
            }

            @Override
            public void markAllRead(int userId) {
            }
        };

        DoctorRepository doctors = new DoctorRepository() {
            @Override
            public Optional<DoctorProfile> findByUserId(int userId) {
                if (userId == doctor.getId()) {
                    DoctorProfile p = new DoctorProfile();
                    p.setUserId(userId);
                    p.setConsultationFee(BigDecimal.valueOf(3000));
                    return Optional.of(p);
                }
                return Optional.empty();
            }

            @Override
            public List<DoctorProfile> findAll(String keyword) {
                return List.of();
            }

            @Override
            public void create(int userId, DoctorProfile profile) {
            }

            @Override
            public void updateConsultationFee(int userId, BigDecimal fee) {
            }
        };

        service = new AppointmentService(appointments, users, doctors,
                new BillingService(invoices), new NotificationService(notifications));
    }

    @Test
    void book_createsAppointment() {
        int id = service.book(patient.getId(), doctor.getId(), ProviderType.DOCTOR,
                LocalDate.now().plusDays(1), "09:00-09:30", "Checkup",
                AppointmentType.CONSULTATION, Priority.NORMAL, "Fever");
        assertEquals(1, id);
        assertEquals(1, store.size());
    }

    @Test
    void book_rejectsPastDate() {
        assertThrows(ServiceException.class, () ->
                service.book(patient.getId(), doctor.getId(), ProviderType.DOCTOR,
                        LocalDate.now().minusDays(1), "09:00-09:30", "Late",
                        AppointmentType.CONSULTATION, Priority.NORMAL, null));
    }

    @Test
    void book_rejectsConflict() {
        service.book(patient.getId(), doctor.getId(), ProviderType.DOCTOR,
                LocalDate.now().plusDays(2), "10:00-10:30", "First",
                AppointmentType.CONSULTATION, Priority.NORMAL, null);
        assertThrows(ServiceException.class, () ->
                service.book(patient.getId(), doctor.getId(), ProviderType.DOCTOR,
                        LocalDate.now().plusDays(2), "10:00-10:30", "Second",
                        AppointmentType.CONSULTATION, Priority.NORMAL, null));
    }

    @Test
    void updateStatus_changesStatus() {
        int id = service.book(patient.getId(), doctor.getId(), ProviderType.DOCTOR,
                LocalDate.now().plusDays(3), "11:00-11:30", "Visit",
                AppointmentType.CONSULTATION, Priority.NORMAL, null);
        service.updateStatus(id, AppointmentStatus.CONFIRMED);
        assertEquals(AppointmentStatus.CONFIRMED, service.getById(id).getStatus());
    }

    private UserRepository stubUserRepo() {
        return new UserRepository() {
            @Override
            public Optional<User> findByUsername(String username) {
                return Optional.empty();
            }

            @Override
            public Optional<User> findByUsernameForLogin(String username) {
                return Optional.empty();
            }

            @Override
            public Optional<User> findById(int id) {
                if (id == patient.getId()) return Optional.of(patient);
                if (id == doctor.getId()) return Optional.of(doctor);
                return Optional.empty();
            }

            @Override
            public List<User> findByRole(Role role) {
                return List.of();
            }

            @Override
            public List<User> search(String keyword, Role role) {
                return List.of();
            }

            @Override
            public List<PendingStaffRegistration> findPendingStaff() {
                return List.of();
            }

            @Override
            public int create(User user) {
                return 0;
            }

            @Override
            public void update(User user) {
            }

            @Override
            public void setActive(int id, boolean active) {
            }

            @Override
            public void updateAccountStatus(int userId, AccountStatus status, Integer verifiedBy, String rejectionReason) {
            }

            @Override
            public boolean existsByUsername(String username) {
                return false;
            }

            @Override
            public boolean existsByEmail(String email) {
                return false;
            }

            @Override
            public void updatePassword(int userId, String passwordHash) {
            }
        };
    }

    private AppointmentRepository stubAppointmentRepo() {
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
                if (appointment.getAppointmentType() == null) {
                    appointment.setAppointmentType(AppointmentType.CONSULTATION);
                }
                if (appointment.getFeeAmount() == null) {
                    appointment.setFeeAmount(BigDecimal.valueOf(60));
                }
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
                                && a.getStatus() != AppointmentStatus.CANCELLED
                                && (excludeId == null || a.getId() != excludeId));
            }
        };
    }

    private User user(int id, Role role) {
        User u = new User();
        u.setId(id);
        u.setRole(role);
        u.setUsername("u" + id);
        u.setFullName("User " + id);
        return u;
    }
}
