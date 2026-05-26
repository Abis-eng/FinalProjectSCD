package com.elcinic.service;

import com.elcinic.exception.ServiceException;
import com.elcinic.model.MedicalRecord;
import com.elcinic.model.PrescriptionItem;
import com.elcinic.model.Role;
import com.elcinic.model.User;
import com.elcinic.repository.MedicalRecordRepository;
import com.elcinic.repository.PrescriptionRepository;
import com.elcinic.repository.UserRepository;
import com.elcinic.utility.ValidationUtil;

import java.time.LocalDate;
import java.util.List;

public class MedicalRecordService {

    private final MedicalRecordRepository recordRepository;
    private final UserRepository userRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final NotificationService notificationService;

    public MedicalRecordService(MedicalRecordRepository recordRepository,
                                UserRepository userRepository,
                                PrescriptionRepository prescriptionRepository,
                                NotificationService notificationService) {
        this.recordRepository = recordRepository;
        this.userRepository = userRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.notificationService = notificationService;
    }

    public List<PrescriptionItem> getPrescriptions(int recordId) {
        return prescriptionRepository.findByMedicalRecord(recordId);
    }

    public List<MedicalRecord> forPatient(int patientId) {
        return recordRepository.findByPatient(patientId);
    }

    public List<MedicalRecord> forDoctor(int doctorId) {
        return recordRepository.findByDoctor(doctorId);
    }

    public List<MedicalRecord> search(String keyword) {
        return recordRepository.search(keyword);
    }

    public MedicalRecord getById(int id) {
        return recordRepository.findById(id)
                .orElseThrow(() -> new ServiceException("Medical record not found"));
    }

    public int create(int patientId, int doctorId, LocalDate visitDate,
                      String diagnosis, String prescription, String notes) {
        ValidationUtil.requireNonBlank(diagnosis, "Diagnosis");
        if (visitDate == null) {
            throw new ServiceException("Visit date is required");
        }

        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new ServiceException("Patient not found"));
        if (patient.getRole() != Role.PATIENT) {
            throw new ServiceException("Invalid patient");
        }

        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new ServiceException("Doctor not found"));
        if (doctor.getRole() != Role.DOCTOR) {
            throw new ServiceException("Only doctors can create medical records");
        }

        MedicalRecord record = new MedicalRecord();
        record.setPatientId(patientId);
        record.setDoctorId(doctorId);
        record.setVisitDate(visitDate);
        record.setDiagnosis(diagnosis.trim());
        record.setPrescription(prescription);
        record.setNotes(notes);
        int id = recordRepository.create(record);
        notificationService.notify(patientId, "New medical record",
                "Diagnosis recorded: " + diagnosis.trim());
        return id;
    }

    public void addPrescriptionItems(int recordId, List<PrescriptionItem> items) {
        getById(recordId);
        for (PrescriptionItem item : items) {
            if (item.getMedicationName() == null || item.getMedicationName().isBlank()) {
                continue;
            }
            item.setMedicalRecordId(recordId);
            prescriptionRepository.create(item);
        }
    }

    public void update(MedicalRecord record) {
        ValidationUtil.requireNonBlank(record.getDiagnosis(), "Diagnosis");
        getById(record.getId());
        recordRepository.update(record);
    }

    public void delete(int id) {
        recordRepository.delete(id);
    }
}
