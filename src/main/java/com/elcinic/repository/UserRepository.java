package com.elcinic.repository;

import com.elcinic.model.AccountStatus;
import com.elcinic.model.PendingStaffRegistration;
import com.elcinic.model.Role;
import com.elcinic.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameForLogin(String username);

    Optional<User> findById(int id);

    List<User> findByRole(Role role);

    List<User> search(String keyword, Role role);

    List<PendingStaffRegistration> findPendingStaff();

    int create(User user);

    void update(User user);

    void setActive(int id, boolean active);

    void updateAccountStatus(int userId, AccountStatus status, Integer verifiedBy, String rejectionReason);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    void updatePassword(int userId, String passwordHash);
}
