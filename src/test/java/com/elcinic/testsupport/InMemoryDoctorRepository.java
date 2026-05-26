package com.elcinic.testsupport;

import com.elcinic.model.DoctorProfile;
import com.elcinic.repository.DoctorRepository;

import java.math.BigDecimal;
import java.util.*;

public class InMemoryDoctorRepository implements DoctorRepository {

    private final Map<Integer, DoctorProfile> profiles = new HashMap<>();

    @Override
    public Optional<DoctorProfile> findByUserId(int userId) {
        return Optional.ofNullable(profiles.get(userId)).map(this::copy);
    }

    @Override
    public List<DoctorProfile> findAll(String keyword) {
        return profiles.values().stream().map(this::copy).toList();
    }

    @Override
    public void create(int userId, DoctorProfile profile) {
        profile.setUserId(userId);
        profiles.put(userId, profile);
    }

    @Override
    public void updateConsultationFee(int userId, BigDecimal fee) {
        DoctorProfile p = profiles.get(userId);
        if (p != null) {
            p.setConsultationFee(fee);
        }
    }

    public void put(DoctorProfile profile) {
        profiles.put(profile.getUserId(), profile);
    }

    private DoctorProfile copy(DoctorProfile p) {
        DoctorProfile c = new DoctorProfile();
        c.setUserId(p.getUserId());
        c.setSpecialization(p.getSpecialization());
        c.setLicenseNumber(p.getLicenseNumber());
        c.setFullName(p.getFullName());
        c.setConsultationFee(p.getConsultationFee());
        return c;
    }
}
