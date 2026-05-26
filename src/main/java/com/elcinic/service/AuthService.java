package com.elcinic.service;

import com.elcinic.exception.ServiceException;
import com.elcinic.model.AccountStatus;
import com.elcinic.model.User;
import com.elcinic.repository.UserRepository;
import com.elcinic.utility.PasswordHasher;
import com.elcinic.utility.ValidationUtil;

import java.util.Optional;

public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> login(String username, String password) {
        ValidationUtil.validateUsername(username);
        ValidationUtil.requireNonBlank(password, "Password");

        User user = userRepository.findByUsernameForLogin(username.trim())
                .orElseThrow(() -> new ServiceException("Invalid username or password"));

        if (!PasswordHasher.verify(password, user.getPasswordHash())) {
            throw new ServiceException("Invalid username or password");
        }

        AccountStatus status = user.getAccountStatus();
        if (status == AccountStatus.PENDING) {
            throw new ServiceException(
                    "Your account is awaiting administrator approval. You will be notified when it is active.");
        }
        if (status == AccountStatus.REJECTED) {
            String reason = user.getRejectionReason();
            if (reason != null && !reason.isBlank()) {
                throw new ServiceException("Registration was declined: " + reason);
            }
            throw new ServiceException("Registration was declined. Contact the clinic for details.");
        }

        if (!user.isActive()) {
            throw new ServiceException("This account has been deactivated");
        }

        user.setPasswordHash(null);
        return Optional.of(user);
    }

    public void changePassword(int userId, String currentPassword, String newPassword) {
        ValidationUtil.requireNonBlank(currentPassword, "Current password");
        ValidationUtil.validatePassword(newPassword);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException("User not found"));
        if (!PasswordHasher.verify(currentPassword, user.getPasswordHash())) {
            throw new ServiceException("Current password is incorrect");
        }
        if (PasswordHasher.verify(newPassword, user.getPasswordHash())) {
            throw new ServiceException("New password must be different from current password");
        }
        userRepository.updatePassword(userId, PasswordHasher.hash(newPassword));
    }
}
