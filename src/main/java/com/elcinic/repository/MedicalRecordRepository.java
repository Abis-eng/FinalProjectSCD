package com.elcinic.repository;

import com.elcinic.model.MedicalRecord;

import java.util.List;
import java.util.Optional;

public interface MedicalRecordRepository {
    Optional<MedicalRecord> findById(int id);

    List<MedicalRecord> findByPatient(int patientId);

    List<MedicalRecord> findByDoctor(int doctorId);

    List<MedicalRecord> search(String keyword);

    int create(MedicalRecord record);

    void update(MedicalRecord record);

    void delete(int id);
}
