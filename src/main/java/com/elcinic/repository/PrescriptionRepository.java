package com.elcinic.repository;

import com.elcinic.model.PrescriptionItem;

import java.util.List;

public interface PrescriptionRepository {
    List<PrescriptionItem> findByMedicalRecord(int recordId);

    void create(PrescriptionItem item);

    void deleteByRecord(int recordId);
}
