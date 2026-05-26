package com.elcinic.service;

import com.elcinic.exception.ServiceException;
import com.elcinic.model.AccountStatus;
import com.elcinic.model.PendingStaffRegistration;
import com.elcinic.model.Role;
import com.elcinic.model.User;
import com.elcinic.repository.UserRepository;
import com.elcinic.utility.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private final Map<String, User> byUsername = new HashMap<>();
    private final Map<Integer, User> byId = new HashMap<>();
    private AuthService authService;

    @BeforeEach
    void setUp() {
        byUsername.clear();
        byId.clear();
        UserRepository repo = stubRepository();
        authService = new AuthService(repo);

        User admin = activeUser(1, "admin", "admin123", Role.ADMIN);
        byUsername.put("admin", admin);
        byId.put(1, admin);
    }

    private UserRepository stubRepository() {
        return new UserRepository() {
            @Override
            public Optional<User> findByUsername(String username) {
                User u = byUsername.get(username);
                if (u != null && u.getAccountStatus() == AccountStatus.ACTIVE && u.isActive()) {
                    return Optional.of(u);
                }
                return Optional.empty();
            }

            @Override
            public Optional<User> findByUsernameForLogin(String username) {
                return Optional.ofNullable(byUsername.get(username));
            }

            @Override
            public Optional<User> findById(int id) {
                return Optional.ofNullable(byId.get(id));
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
                user.setId(byId.size() + 1);
                byUsername.put(user.getUsername(), user);
                byId.put(user.getId(), user);
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
                return byUsername.containsKey(username);
            }

            @Override
            public boolean existsByEmail(String email) {
                return byUsername.values().stream().anyMatch(u -> u.getEmail().equals(email));
            }

            @Override
            public void updatePassword(int userId, String passwordHash) {
            }
        };
    }

    private User activeUser(int id, String username, String password, Role role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPasswordHash(PasswordHasher.hash(password));
        user.setRole(role);
        user.setEmail(username + "@test.com");
        user.setFullName(username);
        user.setActive(true);
        user.setAccountStatus(AccountStatus.ACTIVE);
        return user;
    }

    @Test
    void login_success() {
        User user = authService.login("admin", "admin123").orElseThrow();
        assertEquals(Role.ADMIN, user.getRole());
        assertNull(user.getPasswordHash());
    }

    @Test
    void login_wrongPassword() {
        assertThrows(ServiceException.class, () -> authService.login("admin", "wrong1"));
    }

    @Test
    void login_unknownUser() {
        assertThrows(ServiceException.class, () -> authService.login("nobody", "admin123"));
    }

    @Test
    void login_pendingStaffBlocked() {
        User pending = activeUser(2, "newdoc", "secret12", Role.DOCTOR);
        pending.setAccountStatus(AccountStatus.PENDING);
        byUsername.put("newdoc", pending);
        ServiceException ex = assertThrows(ServiceException.class,
                () -> authService.login("newdoc", "secret12"));
        assertTrue(ex.getMessage().contains("approval"));
    }

    @Test
    void login_rejectedStaffBlocked() {
        User rejected = activeUser(3, "baddoc", "secret12", Role.DOCTOR);
        rejected.setAccountStatus(AccountStatus.REJECTED);
        rejected.setRejectionReason("Invalid license");
        byUsername.put("baddoc", rejected);
        ServiceException ex = assertThrows(ServiceException.class,
                () -> authService.login("baddoc", "secret12"));
        assertTrue(ex.getMessage().contains("Invalid license"));
    }
}
