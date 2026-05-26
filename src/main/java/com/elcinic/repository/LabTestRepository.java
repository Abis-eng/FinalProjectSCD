package com.elcinic.repository;

import com.elcinic.model.LabStatus;
import com.elcinic.model.LabTest;

import java.util.List;
import java.util.Optional;

public interface LabTestRepository {
    Optional<LabTest> findById(int id);

    List<LabTest> findByPatient(int patientId);

    List<LabTest> findByDoctor(int doctorId);

    List<LabTest> findAll(String keyword, LabStatus status);

    int create(LabTest test);

    void update(LabTest test);

    int countPending();
}
