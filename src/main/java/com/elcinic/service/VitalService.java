package com.elcinic.service;

import com.elcinic.model.VitalSigns;
import com.elcinic.repository.VitalSignsRepository;

import java.util.Optional;

public class VitalService {

    private final VitalSignsRepository repository;

    public VitalService(VitalSignsRepository repository) {
        this.repository = repository;
    }

    public Optional<VitalSigns> getByAppointment(int appointmentId) {
        return repository.findByAppointment(appointmentId);
    }

    public void save(VitalSigns vitals) {
        repository.save(vitals);
    }
}
