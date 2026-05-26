package com.elcinic.service;

import com.elcinic.exception.ServiceException;
import com.elcinic.model.*;
import com.elcinic.repository.DoctorRepository;
import com.elcinic.repository.NotificationRepository;
import com.elcinic.repository.NurseRepository;
import com.elcinic.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class RegistrationServiceTest {

    private final Map<String, User> users = new HashMap<>();
    private RegistrationService registrationService;

    @BeforeEach
    void setUp() {
        users.clear();
        UserRepository userRepo = new UserRepository() {
            @Override
            public Optional<User> findByUsername(String username) {
                return Optional.ofNullable(users.get(username));
            }

            @Override
            public Optional<User> findByUsernameForLogin(String username) {
                return findByUsername(username);
            }

            @Override
            public Optional<User> findById(int id) {
                return users.values().stream().filter(u -> u.getId() == id).findFirst();
            }

            @Override
            public List<User> findByRole(Role role) {
                return users.values().stream().filter(u -> u.getRole() == role).toList();
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
                user.setId(users.size() + 1);
                users.put(user.getUsername(), user);
                return user.getId();
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
                return users.containsKey(username);
            }

            @Override
            public boolean existsByEmail(String email) {
                return users.values().stream().anyMatch(u -> u.getEmail().equals(email));
            }

            @Override
            public void updatePassword(int userId, String passwordHash) {
            }
        };

        DoctorRepository doctorRepo = new DoctorRepository() {
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
        NurseRepository nurseRepo = new NurseRepository() {
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
        NotificationRepository notificationRepo = new NotificationRepository() {
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
        NotificationService notifications = new NotificationService(notificationRepo);

        registrationService = new RegistrationService(userRepo, doctorRepo, nurseRepo, notifications);

        User admin = new User();
        admin.setId(1);
        admin.setUsername("admin");
        admin.setRole(Role.ADMIN);
        admin.setEmail("admin@clinic.com");
        admin.setAccountStatus(AccountStatus.ACTIVE);
        users.put("admin", admin);
    }

    @Test
    void registerPatient_isActiveImmediately() {
        User p = registrationService.registerPatient("alice", "secret12", "Alice", "a@test.com", null);
        assertEquals(Role.PATIENT, p.getRole());
        assertEquals(AccountStatus.ACTIVE, users.get("alice").getAccountStatus());
    }

    @Test
    void registerDoctor_isPending() {
        registrationService.registerDoctor("dr1", "secret12", "Dr One", "d@test.com", null,
                "Cardiology", "MD-999");
        assertEquals(AccountStatus.PENDING, users.get("dr1").getAccountStatus());
    }

    @Test
    void registerPatient_duplicateUsername() {
        registrationService.registerPatient("alice", "secret12", "Alice", "a@test.com", null);
        assertThrows(ServiceException.class, () ->
                registrationService.registerPatient("alice", "secret12", "Other", "b@test.com", null));
    }
}
