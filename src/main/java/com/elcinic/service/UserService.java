package com.elcinic.service;

import com.elcinic.exception.ServiceException;
import com.elcinic.model.AccountStatus;
import com.elcinic.model.*;
import com.elcinic.repository.*;
import com.elcinic.utility.PasswordHasher;
import com.elcinic.utility.ValidationUtil;

import java.math.BigDecimal;
import java.util.List;

public class UserService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final NurseRepository nurseRepository;

    public UserService(UserRepository userRepository,
                       PatientRepository patientRepository,
                       DoctorRepository doctorRepository,
                       NurseRepository nurseRepository) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.nurseRepository = nurseRepository;
    }

    public List<User> searchUsers(String keyword, Role role) {
        return userRepository.search(keyword, role);
    }

    public User createStaff(Role role, String username, String password, String fullName,
                            String email, String phone, String specializationOrDept, String license) {
        if (role != Role.DOCTOR && role != Role.NURSE) {
            throw new ServiceException("Only doctor or nurse can be created here");
        }
        ValidationUtil.validateUsername(username);
        ValidationUtil.validatePassword(password);
        ValidationUtil.requireNonBlank(fullName, "Full name");
        ValidationUtil.validateEmail(email);

        if (userRepository.existsByUsername(username.trim())) {
            throw new ServiceException("Username already exists");
        }
        if (userRepository.existsByEmail(email.trim())) {
            throw new ServiceException("Email already exists");
        }

        User user = new User();
        user.setUsername(username.trim());
        user.setPasswordHash(PasswordHasher.hash(password));
        user.setFullName(fullName.trim());
        user.setEmail(email.trim());
        user.setPhone(phone);
        user.setRole(role);
        user.setAccountStatus(AccountStatus.ACTIVE);

        int id = userRepository.create(user);
        user.setId(id);

        if (role == Role.DOCTOR) {
            ValidationUtil.requireNonBlank(specializationOrDept, "Specialization");
            ValidationUtil.requireNonBlank(license, "License number");
            DoctorProfile profile = new DoctorProfile();
            profile.setSpecialization(specializationOrDept.trim());
            profile.setLicenseNumber(license.trim());
            profile.setConsultationFee(BigDecimal.valueOf(3000));
            doctorRepository.create(id, profile);
        } else {
            ValidationUtil.requireNonBlank(specializationOrDept, "Department");
            NurseProfile profile = new NurseProfile();
            profile.setDepartment(specializationOrDept.trim());
            nurseRepository.create(id, profile);
        }
        return publicUser(user, id);
    }

    private User publicUser(User stored, int id) {
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

    public void deactivateUser(int id) {
        userRepository.setActive(id, false);
    }

    public User getUser(int id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ServiceException("User not found"));
    }

    public List<DoctorProfile> listDoctors(String keyword) {
        return doctorRepository.findAll(keyword);
    }

    public List<NurseProfile> listNurses(String keyword) {
        return nurseRepository.findAll(keyword);
    }

    public void updateConsultationFee(int doctorUserId, BigDecimal feePkr) {
        if (feePkr == null || feePkr.compareTo(BigDecimal.valueOf(500)) < 0) {
            throw new ServiceException("Consultation fee must be at least Rs. 500");
        }
        if (feePkr.compareTo(BigDecimal.valueOf(500000)) > 0) {
            throw new ServiceException("Consultation fee is too high");
        }
        doctorRepository.findByUserId(doctorUserId)
                .orElseThrow(() -> new ServiceException("Doctor profile not found"));
        doctorRepository.updateConsultationFee(doctorUserId, feePkr.setScale(0, java.math.RoundingMode.HALF_UP));
    }

    public DoctorProfile getDoctorProfile(int doctorUserId) {
        return doctorRepository.findByUserId(doctorUserId)
                .orElseThrow(() -> new ServiceException("Doctor profile not found"));
    }
}
