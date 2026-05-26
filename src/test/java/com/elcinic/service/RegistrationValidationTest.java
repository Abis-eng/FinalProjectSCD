package com.elcinic.service;

import com.elcinic.exception.ServiceException;
import com.elcinic.model.*;
import com.elcinic.repository.DoctorRepository;
import com.elcinic.repository.NotificationRepository;
import com.elcinic.repository.NurseRepository;
import com.elcinic.testsupport.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RegistrationValidationTest {

    private InMemoryUserRepository users;
    private RegistrationService registration;

    @BeforeEach
    void setUp() {
        users = new InMemoryUserRepository();
        DoctorRepository doctors = new DoctorRepository() {
            @Override
            public Optional<DoctorProfile> findByUserId(int userId) {
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
            public void updateConsultationFee(int userId, java.math.BigDecimal fee) {
            }
        };
        NurseRepository nurses = new NurseRepository() {
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
        registration = new RegistrationService(users, doctors, nurses, new NotificationService(notifications));
    }

    @Test
    void registerPatient_rejectsShortPassword() {
        assertThrows(IllegalArgumentException.class, () ->
                registration.registerPatient("alice", "123", "Alice", "a@test.com", null));
    }

    @Test
    void registerPatient_rejectsInvalidEmail() {
        assertThrows(IllegalArgumentException.class, () ->
                registration.registerPatient("alice", "secret12", "Alice", "not-an-email", null));
    }

    @Test
    void registerPatient_rejectsShortUsername() {
        assertThrows(IllegalArgumentException.class, () ->
                registration.registerPatient("ab", "secret12", "Alice", "a@test.com", null));
    }

    @Test
    void registerPatient_duplicateEmail() {
        registration.registerPatient("alice", "secret12", "Alice", "same@test.com", null);
        assertThrows(ServiceException.class, () ->
                registration.registerPatient("bob", "secret12", "Bob", "same@test.com", null));
    }

    @Test
    void registerDoctor_requiresLicense() {
        assertThrows(IllegalArgumentException.class, () ->
                registration.registerDoctor("dr1", "secret12", "Dr", "d@test.com", null, "Cardio", ""));
    }

    @Test
    void registerDoctor_requiresSpecialization() {
        assertThrows(IllegalArgumentException.class, () ->
                registration.registerDoctor("dr1", "secret12", "Dr", "d@test.com", null, "", "MD-1"));
    }

    @Test
    void registerNurse_isPending() {
        registration.registerNurse("nurse1", "secret12", "Nurse One", "n@test.com", "0300", "ICU");
        assertEquals(AccountStatus.PENDING, users.findByUsernameForLogin("nurse1").orElseThrow().getAccountStatus());
    }

    @Test
    void registerNurse_requiresDepartment() {
        assertThrows(IllegalArgumentException.class, () ->
                registration.registerNurse("n1", "secret12", "N", "n@test.com", null, ""));
    }

    @Test
    void registerPatient_setsFullName() {
        User p = registration.registerPatient("zara", "secret12", "Zara Khan", "z@test.com", "03001112222");
        assertEquals("Zara Khan", users.findById(p.getId()).orElseThrow().getFullName());
    }
}
