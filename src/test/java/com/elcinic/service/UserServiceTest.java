package com.elcinic.service;

import com.elcinic.exception.ServiceException;
import com.elcinic.model.*;
import com.elcinic.repository.NurseRepository;
import com.elcinic.repository.PatientRepository;
import com.elcinic.testsupport.InMemoryDoctorRepository;
import com.elcinic.testsupport.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private InMemoryUserRepository users;
    private InMemoryDoctorRepository doctors;
    private UserService userService;

    @BeforeEach
    void setUp() {
        users = new InMemoryUserRepository();
        doctors = new InMemoryDoctorRepository();
        PatientRepository patients = new PatientRepository() {
            @Override
            public Optional<PatientProfile> findByUserId(int userId) {
                return Optional.empty();
            }

            @Override
            public List<PatientProfile> findAll(String keyword) {
                return List.of();
            }

            @Override
            public List<PatientProfile> findByAssignedDoctor(int doctorId, String keyword) {
                return List.of();
            }

            @Override
            public void create(int userId, PatientProfile profile) {
            }

            @Override
            public void update(PatientProfile profile) {
            }

            @Override
            public void assignDoctor(int patientUserId, Integer doctorId) {
            }

            @Override
            public void requestDoctor(int patientUserId, Integer doctorId) {
            }
        };
        NurseRepository nurses = new NurseRepository() {
            @Override
            public Optional<NurseProfile> findByUserId(int userId) {
                return Optional.empty();
            }

            @Override
            public List<NurseProfile> findAll(String keyword) {
                return List.of();
            }

            @Override
            public void create(int userId, NurseProfile profile) {
            }
        };
        userService = new UserService(users, patients, doctors, nurses);
    }

    @Test
    void createStaff_doctor_isActiveWithDefaultFee() {
        User doc = userService.createStaff(Role.DOCTOR, "dr.new", "secret12", "Dr New",
                "new@clinic.com", "03001234567", "General", "MD-200");
        assertEquals(Role.DOCTOR, doc.getRole());
        assertEquals(AccountStatus.ACTIVE, users.findById(doc.getId()).orElseThrow().getAccountStatus());
        assertEquals(BigDecimal.valueOf(3000), doctors.findByUserId(doc.getId()).orElseThrow().getConsultationFee());
    }

    @Test
    void createStaff_nurse_requiresDepartment() {
        assertThrows(IllegalArgumentException.class, () ->
                userService.createStaff(Role.NURSE, "nurse1", "secret12", "Nurse", "n@test.com", null, "", null));
    }

    @Test
    void createStaff_rejectsPatientRole() {
        assertThrows(ServiceException.class, () ->
                userService.createStaff(Role.PATIENT, "p1", "secret12", "X", "x@test.com", null, "x", null));
    }

    @Test
    void updateConsultationFee_enforcesMinimum() {
        User doc = userService.createStaff(Role.DOCTOR, "dr.fee", "secret12", "Dr Fee",
                "fee@clinic.com", null, "Cardio", "MD-1");
        assertThrows(ServiceException.class, () ->
                userService.updateConsultationFee(doc.getId(), BigDecimal.valueOf(100)));
    }

    @Test
    void updateConsultationFee_enforcesMaximum() {
        User doc = userService.createStaff(Role.DOCTOR, "dr.max", "secret12", "Dr Max",
                "max@clinic.com", null, "Cardio", "MD-2");
        assertThrows(ServiceException.class, () ->
                userService.updateConsultationFee(doc.getId(), BigDecimal.valueOf(999999)));
    }

    @Test
    void updateConsultationFee_success() {
        User doc = userService.createStaff(Role.DOCTOR, "dr.ok", "secret12", "Dr OK",
                "ok@clinic.com", null, "Ortho", "MD-3");
        userService.updateConsultationFee(doc.getId(), BigDecimal.valueOf(5000));
        assertEquals(0, BigDecimal.valueOf(5000).compareTo(
                doctors.findByUserId(doc.getId()).orElseThrow().getConsultationFee()));
    }
}
