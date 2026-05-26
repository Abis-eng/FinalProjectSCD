package com.elcinic.service;

import com.elcinic.exception.ServiceException;
import com.elcinic.model.*;

import java.math.BigDecimal;
import com.elcinic.repository.DoctorRepository;
import com.elcinic.repository.NurseRepository;
import com.elcinic.repository.UserRepository;
import com.elcinic.utility.PasswordHasher;
import com.elcinic.utility.ValidationUtil;

public class RegistrationService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final NurseRepository nurseRepository;
    private final NotificationService notificationService;

    public RegistrationService(UserRepository userRepository,
                               DoctorRepository doctorRepository,
                               NurseRepository nurseRepository,
                               NotificationService notificationService) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.nurseRepository = nurseRepository;
        this.notificationService = notificationService;
    }

    public User registerPatient(String username, String password, String fullName,
                                String email, String phone) {
        User user = buildUser(username, password, fullName, email, phone, Role.PATIENT);
        user.setAccountStatus(AccountStatus.ACTIVE);
        int id = userRepository.create(user);
        return withoutPassword(user, id);
    }

    public User registerDoctor(String username, String password, String fullName,
                               String email, String phone, String specialization, String licenseNumber) {
        ValidationUtil.requireNonBlank(specialization, "Specialization");
        ValidationUtil.requireNonBlank(licenseNumber, "License number");
        User user = buildUser(username, password, fullName, email, phone, Role.DOCTOR);
        user.setAccountStatus(AccountStatus.PENDING);
        int id = userRepository.create(user);
        DoctorProfile profile = new DoctorProfile();
        profile.setSpecialization(specialization.trim());
        profile.setLicenseNumber(licenseNumber.trim());
        profile.setConsultationFee(BigDecimal.valueOf(3000));
        doctorRepository.create(id, profile);
        notifyAdminsNewStaff(user, "Doctor");
        return withoutPassword(user, id);
    }

    public User registerNurse(String username, String password, String fullName,
                              String email, String phone, String department) {
        ValidationUtil.requireNonBlank(department, "Department");
        User user = buildUser(username, password, fullName, email, phone, Role.NURSE);
        user.setAccountStatus(AccountStatus.PENDING);
        int id = userRepository.create(user);
        NurseProfile profile = new NurseProfile();
        profile.setDepartment(department.trim());
        nurseRepository.create(id, profile);
        notifyAdminsNewStaff(user, "Nurse");
        return withoutPassword(user, id);
    }

    private User withoutPassword(User stored, int id) {
        User response = new User();
        response.setId(id);
        response.setUsername(stored.getUsername());
        response.setFullName(stored.getFullName());
        response.setEmail(stored.getEmail());
        response.setPhone(stored.getPhone());
        response.setRole(stored.getRole());
        response.setAccountStatus(stored.getAccountStatus());
        response.setActive(stored.isActive());
        return response;
    }

    private User buildUser(String username, String password, String fullName,
                           String email, String phone, Role role) {
        if (role == Role.ADMIN) {
            throw new ServiceException("Admin accounts cannot be registered publicly");
        }
        ValidationUtil.validateUsername(username);
        ValidationUtil.validatePassword(password);
        ValidationUtil.requireNonBlank(fullName, "Full name");
        ValidationUtil.validateEmail(email);

        if (userRepository.existsByUsername(username.trim())) {
            throw new ServiceException("Username already exists");
        }
        if (userRepository.existsByEmail(email.trim())) {
            throw new ServiceException("Email already registered");
        }

        User user = new User();
        user.setUsername(username.trim());
        user.setPasswordHash(PasswordHasher.hash(password));
        user.setFullName(fullName.trim());
        user.setEmail(email.trim());
        user.setPhone(phone != null ? phone.trim() : null);
        user.setRole(role);
        user.setActive(true);
        return user;
    }

    private void notifyAdminsNewStaff(User user, String label) {
        for (User admin : userRepository.findByRole(Role.ADMIN)) {
            notificationService.notify(admin.getId(),
                    "New " + label + " registration",
                    user.getFullName() + " (" + user.getUsername() + ") is awaiting approval.");
        }
    }
}
