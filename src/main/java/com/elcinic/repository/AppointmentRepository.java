package com.elcinic.repository;

import com.elcinic.model.Appointment;
import com.elcinic.model.AppointmentStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository {
    Optional<Appointment> findById(int id);

    List<Appointment> findAll(String keyword, AppointmentStatus status, LocalDate from, LocalDate to);

    List<Appointment> findByPatient(int patientId);

    List<Appointment> findByProvider(int providerId);

    int create(Appointment appointment);

    void update(Appointment appointment);

    void updateStatus(int id, AppointmentStatus status);

    void delete(int id);

    boolean hasConflict(int providerId, LocalDate date, String timeSlot, Integer excludeId);
}
