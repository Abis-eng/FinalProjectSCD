package com.elcinic.service;

import com.elcinic.exception.ServiceException;
import com.elcinic.model.DoctorProfile;
import com.elcinic.model.PatientProfile;
import com.elcinic.model.Role;
import com.elcinic.model.User;
import com.elcinic.repository.DoctorRepository;
import com.elcinic.repository.PatientRepository;
import com.elcinic.repository.UserRepository;
import com.elcinic.utility.ValidationUtil;

import java.time.LocalDate;
import java.util.List;

public class PatientService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;

    public PatientService(PatientRepository patientRepository, DoctorRepository doctorRepository,
                          UserRepository userRepository) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
    }

    public PatientProfile getProfile(int patientUserId) {
        return patientRepository.findByUserId(patientUserId)
                .orElseThrow(() -> new ServiceException("Patient profile not found"));
    }

    public List<PatientProfile> listPatients(String keyword) {
        return patientRepository.findAll(keyword);
    }

    public List<PatientProfile> listPatientsForDoctor(int doctorId, String keyword) {
        return patientRepository.findByAssignedDoctor(doctorId, keyword);
    }

    public void completeRegistration(int userId, LocalDate dob, String bloodType, Integer doctorId) {
        if (doctorId != null) {
            doctorRepository.findByUserId(doctorId)
                    .orElseThrow(() -> new ServiceException("Requested doctor not found"));
        }
        PatientProfile profile = new PatientProfile();
        profile.setUserId(userId);
        profile.setDateOfBirth(dob);
        profile.setBloodType(bloodType);
        profile.setRequestedDoctorId(doctorId);
        patientRepository.create(userId, profile);
    }

    public void assignDoctor(int patientId, int doctorId) {
        doctorRepository.findByUserId(doctorId)
                .orElseThrow(() -> new ServiceException("Doctor not found"));
        patientRepository.assignDoctor(patientId, doctorId);
    }

    public void requestDoctor(int patientId, int doctorId) {
        doctorRepository.findByUserId(doctorId)
                .orElseThrow(() -> new ServiceException("Doctor not found"));
        patientRepository.requestDoctor(patientId, doctorId);
    }

    public void updateProfile(int patientId, LocalDate dob, String bloodType) {
        PatientProfile profile = getProfile(patientId);
        profile.setDateOfBirth(dob);
        profile.setBloodType(bloodType);
        patientRepository.update(profile);
    }

    public List<DoctorProfile> availableDoctors(String keyword) {
        return doctorRepository.findAll(keyword);
    }

    public void validatePatientUser(int userId) {
        if (userRepository != null) {
            User u = userRepository.findById(userId).orElseThrow(() -> new ServiceException("User not found"));
            if (u.getRole() != Role.PATIENT) {
                throw new ServiceException("User is not a patient");
            }
        }
    }

    public void validateAssignDoctorInput(String doctorIdStr) {
        if (doctorIdStr != null && !doctorIdStr.isBlank()) {
            ValidationUtil.parsePositiveId(doctorIdStr, "Doctor");
        }
    }
}
