package com.elcinic.service;

import com.elcinic.exception.ServiceException;
import com.elcinic.model.AccountStatus;
import com.elcinic.model.PendingStaffRegistration;
import com.elcinic.model.Role;
import com.elcinic.model.User;
import com.elcinic.repository.UserRepository;
import com.elcinic.utility.ValidationUtil;

import java.util.List;

public class StaffVerificationService {

    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public StaffVerificationService(UserRepository userRepository,
                                    NotificationService notificationService) {
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public List<PendingStaffRegistration> listPending() {
        return userRepository.findPendingStaff();
    }

    public void approve(int adminUserId, int staffUserId) {
        User staff = requirePendingStaff(staffUserId);
        userRepository.updateAccountStatus(staffUserId, AccountStatus.ACTIVE, adminUserId, null);
        notificationService.notify(staffUserId,
                "Account approved",
                "Your " + staff.getRole().name().toLowerCase() + " account is now active. You can log in.");
    }

    public void reject(int adminUserId, int staffUserId, String reason) {
        requirePendingStaff(staffUserId);
        ValidationUtil.requireNonBlank(reason, "Rejection reason");
        String msg = reason.trim();
        userRepository.updateAccountStatus(staffUserId, AccountStatus.REJECTED, adminUserId, msg);
        notificationService.notify(staffUserId,
                "Registration declined",
                "Your account request was not approved. Reason: " + msg.trim());
    }

    private User requirePendingStaff(int staffUserId) {
        User staff = userRepository.findById(staffUserId)
                .orElseThrow(() -> new ServiceException("User not found"));
        if (staff.getRole() != Role.DOCTOR && staff.getRole() != Role.NURSE) {
            throw new ServiceException("Only doctor or nurse accounts can be verified here");
        }
        if (staff.getAccountStatus() != AccountStatus.PENDING) {
            throw new ServiceException("This account is not pending approval");
        }
        return staff;
    }
}
