package com.elcinic.repository;

import com.elcinic.model.VitalSigns;

import java.util.Optional;

public interface VitalSignsRepository {
    Optional<VitalSigns> findByAppointment(int appointmentId);

    void save(VitalSigns vitals);
}
