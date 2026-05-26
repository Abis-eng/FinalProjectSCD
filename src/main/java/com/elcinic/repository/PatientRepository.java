package com.elcinic.repository;

import com.elcinic.model.PatientProfile;

import java.util.List;
import java.util.Optional;

public interface PatientRepository {
    Optional<PatientProfile> findByUserId(int userId);

    List<PatientProfile> findAll(String keyword);

    List<PatientProfile> findByAssignedDoctor(int doctorId, String keyword);

    void create(int userId, PatientProfile profile);

    void update(PatientProfile profile);

    void assignDoctor(int patientUserId, Integer doctorId);
}
