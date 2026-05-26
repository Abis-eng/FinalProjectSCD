package com.elcinic.service;

import com.elcinic.exception.ServiceException;
import com.elcinic.model.LabStatus;
import com.elcinic.model.LabTest;
import com.elcinic.model.Role;
import com.elcinic.model.User;
import com.elcinic.repository.LabTestRepository;
import com.elcinic.repository.UserRepository;
import com.elcinic.utility.ValidationUtil;

import java.time.LocalDate;
import java.util.List;

public class LabService {

    private final LabTestRepository labRepository;
    private final UserRepository userRepository;
    private NotificationService notificationService;

    public LabService(LabTestRepository labRepository, UserRepository userRepository) {
        this.labRepository = labRepository;
        this.userRepository = userRepository;
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public List<LabTest> forPatient(int patientId) {
        return labRepository.findByPatient(patientId);
    }

    public List<LabTest> forDoctor(int doctorId) {
        return labRepository.findByDoctor(doctorId);
    }

    public LabTest getById(int id) {
        return labRepository.findById(id).orElseThrow(() -> new ServiceException("Lab test not found"));
    }

    public List<LabTest> search(String keyword, String status) {
        LabStatus st = null;
        if (status != null && !status.isBlank()) {
            st = LabStatus.fromString(status);
        }
        return labRepository.findAll(keyword, st);
    }

    public int order(int patientId, int doctorId, String testName, String notes) {
        ValidationUtil.requireNonBlank(testName, "Test name");
        validateDoctor(doctorId);
        validatePatient(patientId);

        LabTest test = new LabTest();
        test.setPatientId(patientId);
        test.setDoctorId(doctorId);
        test.setTestName(testName.trim());
        test.setStatus(LabStatus.ORDERED);
        test.setOrderedDate(LocalDate.now());
        test.setNotes(notes);
        int id = labRepository.create(test);

        if (notificationService != null) {
            notificationService.notify(patientId, "Lab test ordered",
                    "Your doctor ordered: " + testName);
        }
        return id;
    }

    public void updateResult(int id, String resultValue, String resultUnit, LabStatus status, String notes) {
        LabTest test = labRepository.findById(id).orElseThrow(() -> new ServiceException("Lab test not found"));
        if (status == LabStatus.COMPLETED && (resultValue == null || resultValue.isBlank())) {
            throw new ServiceException("Result value is required when completing a test");
        }
        test.setResultValue(resultValue);
        test.setResultUnit(resultUnit);
        test.setStatus(status);
        test.setNotes(notes);
        if (status == LabStatus.COMPLETED) {
            test.setCompletedDate(LocalDate.now());
        }
        labRepository.update(test);

        if (notificationService != null && status == LabStatus.COMPLETED) {
            notificationService.notify(test.getPatientId(), "Lab results ready",
                    test.getTestName() + ": " + resultValue + " " + resultUnit);
        }
    }

    private void validateDoctor(int doctorId) {
        User u = userRepository.findById(doctorId).orElseThrow(() -> new ServiceException("Doctor not found"));
        if (u.getRole() != Role.DOCTOR) {
            throw new ServiceException("Invalid doctor");
        }
    }

    private void validatePatient(int patientId) {
        User u = userRepository.findById(patientId).orElseThrow(() -> new ServiceException("Patient not found"));
        if (u.getRole() != Role.PATIENT) {
            throw new ServiceException("Invalid patient");
        }
    }
}
