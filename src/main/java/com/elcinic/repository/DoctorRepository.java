package com.elcinic.repository;

import com.elcinic.model.DoctorProfile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface DoctorRepository {
    Optional<DoctorProfile> findByUserId(int userId);

    List<DoctorProfile> findAll(String keyword);

    void create(int userId, DoctorProfile profile);

    void updateConsultationFee(int userId, BigDecimal fee);
}
