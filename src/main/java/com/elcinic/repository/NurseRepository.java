package com.elcinic.repository;

import com.elcinic.model.NurseProfile;

import java.util.List;
import java.util.Optional;

public interface NurseRepository {
    Optional<NurseProfile> findByUserId(int userId);

    List<NurseProfile> findAll(String keyword);

    void create(int userId, NurseProfile profile);
}
