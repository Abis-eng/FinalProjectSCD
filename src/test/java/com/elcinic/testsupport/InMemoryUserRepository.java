package com.elcinic.testsupport;

import com.elcinic.model.*;
import com.elcinic.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

/** In-memory user store for service-layer tests. */
public class InMemoryUserRepository implements UserRepository {

    private final Map<Integer, User> byId = new HashMap<>();
    private final Map<String, User> byUsername = new HashMap<>();
    private final List<PendingStaffRegistration> pendingStaff = new ArrayList<>();
    private int nextId = 1;

    @Override
    public Optional<User> findByUsername(String username) {
        User u = byUsername.get(username);
        if (u != null && u.isActive() && u.getAccountStatus() == AccountStatus.ACTIVE) {
            return Optional.of(copy(u));
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByUsernameForLogin(String username) {
        User u = byUsername.get(username);
        return u == null ? Optional.empty() : Optional.of(copy(u));
    }

    @Override
    public Optional<User> findById(int id) {
        User u = byId.get(id);
        return u == null ? Optional.empty() : Optional.of(copy(u));
    }

    @Override
    public List<User> findByRole(Role role) {
        return byId.values().stream()
                .filter(u -> u.getRole() == role && u.isActive() && u.getAccountStatus() == AccountStatus.ACTIVE)
                .map(this::copy)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> search(String keyword, Role role) {
        return byId.values().stream()
                .filter(u -> u.isActive() && u.getAccountStatus() == AccountStatus.ACTIVE)
                .filter(u -> role == null || u.getRole() == role)
                .filter(u -> keyword == null || keyword.isBlank()
                        || u.getFullName().toLowerCase().contains(keyword.toLowerCase())
                        || u.getUsername().toLowerCase().contains(keyword.toLowerCase()))
                .map(this::copy)
                .collect(Collectors.toList());
    }

    @Override
    public List<PendingStaffRegistration> findPendingStaff() {
        return List.copyOf(pendingStaff);
    }

    @Override
    public int create(User user) {
        user.setId(nextId++);
        byId.put(user.getId(), user);
        byUsername.put(user.getUsername(), user);
        if (user.getAccountStatus() == AccountStatus.PENDING
                && (user.getRole() == Role.DOCTOR || user.getRole() == Role.NURSE)) {
            PendingStaffRegistration p = new PendingStaffRegistration();
            p.setUserId(user.getId());
            p.setUsername(user.getUsername());
            p.setFullName(user.getFullName());
            p.setEmail(user.getEmail());
            p.setPhone(user.getPhone());
            p.setRole(user.getRole());
            pendingStaff.add(p);
        }
        return user.getId();
    }

    @Override
    public void update(User user) {
        byId.put(user.getId(), user);
        byUsername.put(user.getUsername(), user);
    }

    @Override
    public void setActive(int id, boolean active) {
        User u = byId.get(id);
        if (u != null) {
            u.setActive(active);
        }
    }

    @Override
    public void updateAccountStatus(int userId, AccountStatus status, Integer verifiedBy, String rejectionReason) {
        User u = byId.get(userId);
        if (u == null) {
            return;
        }
        u.setAccountStatus(status);
        u.setVerifiedBy(verifiedBy);
        u.setRejectionReason(rejectionReason);
        if (status == AccountStatus.ACTIVE) {
            u.setActive(true);
            pendingStaff.removeIf(p -> p.getUserId() == userId);
        } else if (status == AccountStatus.REJECTED) {
            u.setActive(false);
            pendingStaff.removeIf(p -> p.getUserId() == userId);
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        return byUsername.containsKey(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return byId.values().stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(email.trim()));
    }

    @Override
    public void updatePassword(int userId, String passwordHash) {
        User u = byId.get(userId);
        if (u != null) {
            u.setPasswordHash(passwordHash);
        }
    }

    public void seed(User user) {
        if (user.getId() == 0) {
            create(user);
        } else {
            byId.put(user.getId(), user);
            byUsername.put(user.getUsername(), user);
            nextId = Math.max(nextId, user.getId() + 1);
            trackPending(user);
        }
    }

    private void trackPending(User user) {
        if (user.getAccountStatus() == AccountStatus.PENDING
                && (user.getRole() == Role.DOCTOR || user.getRole() == Role.NURSE)) {
            PendingStaffRegistration p = new PendingStaffRegistration();
            p.setUserId(user.getId());
            p.setUsername(user.getUsername());
            p.setFullName(user.getFullName());
            p.setEmail(user.getEmail());
            p.setPhone(user.getPhone());
            p.setRole(user.getRole());
            pendingStaff.add(p);
        }
    }

    private User copy(User u) {
        User c = new User();
        c.setId(u.getId());
        c.setUsername(u.getUsername());
        c.setPasswordHash(u.getPasswordHash());
        c.setFullName(u.getFullName());
        c.setEmail(u.getEmail());
        c.setPhone(u.getPhone());
        c.setRole(u.getRole());
        c.setActive(u.isActive());
        c.setAccountStatus(u.getAccountStatus());
        c.setRejectionReason(u.getRejectionReason());
        c.setVerifiedBy(u.getVerifiedBy());
        return c;
    }
}
