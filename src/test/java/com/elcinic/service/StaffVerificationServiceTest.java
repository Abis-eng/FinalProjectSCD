package com.elcinic.service;

import com.elcinic.exception.ServiceException;
import com.elcinic.model.*;
import com.elcinic.repository.NotificationRepository;
import com.elcinic.testsupport.InMemoryUserRepository;
import com.elcinic.utility.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StaffVerificationServiceTest {

    private InMemoryUserRepository users;
    private final List<Notification> notifications = new ArrayList<>();
    private StaffVerificationService service;

    @BeforeEach
    void setUp() {
        users = new InMemoryUserRepository();
        NotificationRepository notificationRepo = new NotificationRepository() {
            @Override
            public List<Notification> findByUser(int userId, boolean unreadOnly) {
                return notifications.stream().filter(n -> n.getUserId() == userId).toList();
            }

            @Override
            public int countUnread(int userId) {
                return (int) notifications.stream().filter(n -> n.getUserId() == userId && !n.isRead()).count();
            }

            @Override
            public int create(Notification n) {
                notifications.add(n);
                return notifications.size();
            }

            @Override
            public void markRead(int id, int userId) {
            }

            @Override
            public void markAllRead(int userId) {
            }
        };
        service = new StaffVerificationService(users, new NotificationService(notificationRepo));

        User admin = activeUser(1, "admin", Role.ADMIN);
        users.seed(admin);

        User pendingDoc = pendingUser(2, "dr.pending", Role.DOCTOR);
        users.seed(pendingDoc);
    }

    @Test
    void listPending_includesDoctor() {
        assertEquals(1, service.listPending().size());
        assertEquals("dr.pending", service.listPending().get(0).getUsername());
    }

    @Test
    void approve_activatesAccount() {
        service.approve(1, 2);
        User u = users.findByUsernameForLogin("dr.pending").orElseThrow();
        assertEquals(AccountStatus.ACTIVE, u.getAccountStatus());
        assertTrue(notifications.stream().anyMatch(n -> n.getTitle().contains("approved")));
        assertEquals(0, service.listPending().size());
    }

    @Test
    void approve_allowsLogin() {
        service.approve(1, 2);
        AuthService auth = new AuthService(users);
        User loggedIn = auth.login("dr.pending", "secret12").orElseThrow();
        assertEquals(Role.DOCTOR, loggedIn.getRole());
    }

    @Test
    void reject_blocksLoginWithReason() {
        service.reject(1, 2, "Invalid license document");
        AuthService auth = new AuthService(users);
        ServiceException ex = assertThrows(ServiceException.class,
                () -> auth.login("dr.pending", "secret12"));
        assertTrue(ex.getMessage().contains("Invalid license"));
    }

    @Test
    void reject_requiresReason() {
        assertThrows(IllegalArgumentException.class, () -> service.reject(1, 2, "  "));
    }

    @Test
    void approve_rejectsPatient() {
        User patient = pendingUser(3, "notstaff", Role.PATIENT);
        patient.setAccountStatus(AccountStatus.PENDING);
        users.seed(patient);
        assertThrows(ServiceException.class, () -> service.approve(1, 3));
    }

    private static User activeUser(int id, String username, Role role) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setPasswordHash(PasswordHasher.hash("secret12"));
        u.setFullName(username);
        u.setEmail(username + "@test.com");
        u.setRole(role);
        u.setActive(true);
        u.setAccountStatus(AccountStatus.ACTIVE);
        return u;
    }

    private static User pendingUser(int id, String username, Role role) {
        User u = activeUser(id, username, role);
        u.setAccountStatus(AccountStatus.PENDING);
        return u;
    }
}
