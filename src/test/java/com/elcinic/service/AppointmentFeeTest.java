package com.elcinic.service;

import com.elcinic.model.*;
import com.elcinic.repository.AppointmentRepository;
import com.elcinic.repository.InvoiceRepository;
import com.elcinic.repository.NotificationRepository;
import com.elcinic.testsupport.InMemoryDoctorRepository;
import com.elcinic.testsupport.InMemoryUserRepository;
import com.elcinic.utility.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentFeeTest {

    private AppointmentService service;
    private InMemoryDoctorRepository doctors;
    private User patient;
    private User doctor;

    @BeforeEach
    void setUp() {
        InMemoryUserRepository users = new InMemoryUserRepository();
        doctors = new InMemoryDoctorRepository();
        List<Appointment> store = new ArrayList<>();

        patient = user(10, Role.PATIENT, "patient1");
        doctor = user(20, Role.DOCTOR, "dr.fee");
        users.seed(patient);
        users.seed(doctor);

        DoctorProfile profile = new DoctorProfile();
        profile.setUserId(doctor.getId());
        profile.setConsultationFee(BigDecimal.valueOf(4000));
        profile.setSpecialization("General");
        profile.setLicenseNumber("MD-X");
        doctors.put(profile);

        AppointmentRepository appointments = stubAppointments(store);
        service = new AppointmentService(appointments, users, doctors,
                new BillingService(stubInvoices()), stubNotifications());
    }

    @Test
    void book_doctorConsultation_usesDoctorBaseFee() {
        int id = service.book(patient.getId(), doctor.getId(), ProviderType.DOCTOR,
                LocalDate.now().plusDays(1), "09:00-09:30", "Checkup",
                AppointmentType.CONSULTATION, Priority.NORMAL, null);
        assertEquals(0, BigDecimal.valueOf(4000).compareTo(service.getById(id).getFeeAmount()));
    }

    @Test
    void book_emergency_appliesMultiplier() {
        int id = service.book(patient.getId(), doctor.getId(), ProviderType.DOCTOR,
                LocalDate.now().plusDays(1), "10:00-10:30", "Pain",
                AppointmentType.EMERGENCY, Priority.URGENT, "Chest pain");
        assertEquals(0, BigDecimal.valueOf(10000).compareTo(service.getById(id).getFeeAmount()));
    }

    @Test
    void book_followUp_appliesDiscountMultiplier() {
        int id = service.book(patient.getId(), doctor.getId(), ProviderType.DOCTOR,
                LocalDate.now().plusDays(2), "11:00-11:30", "Review",
                AppointmentType.FOLLOW_UP, Priority.NORMAL, null);
        assertEquals(0, BigDecimal.valueOf(2600).compareTo(service.getById(id).getFeeAmount()));
    }

    @Test
    void book_nurse_usesNurseBaseFee() {
        User nurse = user(30, Role.NURSE, "nurse1");
        InMemoryUserRepository users = new InMemoryUserRepository();
        users.seed(patient);
        users.seed(nurse);
        List<Appointment> store = new ArrayList<>();
        AppointmentService nurseService = new AppointmentService(
                stubAppointments(store), users, doctors,
                new BillingService(stubInvoices()), stubNotifications());

        int id = nurseService.book(patient.getId(), nurse.getId(), ProviderType.NURSE,
                LocalDate.now().plusDays(1), "14:00-14:30", "Dressing",
                AppointmentType.CONSULTATION, Priority.NORMAL, null);
        assertEquals(0, BigDecimal.valueOf(1500).compareTo(nurseService.getById(id).getFeeAmount()));
    }

    @Test
    void book_customFee_overridesCalculation() {
        int id = service.book(patient.getId(), doctor.getId(), ProviderType.DOCTOR,
                LocalDate.now().plusDays(3), "15:00-15:30", "Special",
                AppointmentType.CONSULTATION, Priority.NORMAL, null,
                BigDecimal.valueOf(7500));
        assertEquals(0, BigDecimal.valueOf(7500).compareTo(service.getById(id).getFeeAmount()));
    }

    @Test
    void resolveFee_vaccinationMultiplier() {
        BigDecimal fee = service.resolveFee(doctor.getId(), ProviderType.DOCTOR,
                AppointmentType.VACCINATION, null);
        assertEquals(0, BigDecimal.valueOf(5400).compareTo(fee));
    }

    private static User user(int id, Role role, String username) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setPasswordHash(PasswordHasher.hash("secret12"));
        u.setFullName("User " + id);
        u.setEmail(username + "@test.com");
        u.setRole(role);
        u.setActive(true);
        u.setAccountStatus(AccountStatus.ACTIVE);
        return u;
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
                return false;
            }
        };
    }

    private static InvoiceRepository stubInvoices() {
        return new InvoiceRepository() {
            @Override
            public Optional<Invoice> findByAppointmentId(int appointmentId) {
                return Optional.empty();
            }

            @Override
            public Optional<Invoice> findById(int id) {
                return Optional.empty();
            }

            @Override
            public List<Invoice> findByPatient(int patientId) {
                return List.of();
            }

            @Override
            public List<Invoice> findAll(String keyword, PaymentStatus status) {
                return List.of();
            }

            @Override
            public int create(Invoice invoice) {
                return 1;
            }

            @Override
            public void updatePayment(int id, PaymentStatus status, String paymentMethod) {
            }

            @Override
            public double sumPaidToday() {
                return 0;
            }
        };
    }

    private static NotificationService stubNotifications() {
        return new NotificationService(new NotificationRepository() {
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
        });
    }
}
