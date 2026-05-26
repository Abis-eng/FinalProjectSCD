package com.elcinic.service;

import com.elcinic.exception.ServiceException;
import com.elcinic.model.Appointment;
import com.elcinic.model.AppointmentStatus;
import com.elcinic.model.AppointmentType;
import com.elcinic.model.DoctorProfile;
import com.elcinic.model.Priority;
import com.elcinic.model.ProviderType;
import com.elcinic.model.Role;
import com.elcinic.model.User;
import com.elcinic.model.VitalSigns;
import com.elcinic.repository.AppointmentRepository;
import com.elcinic.repository.DoctorRepository;
import com.elcinic.repository.UserRepository;
import com.elcinic.utility.ValidationUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class AppointmentService {

    private static final BigDecimal NURSE_BASE_FEE_PKR = BigDecimal.valueOf(1500);

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final BillingService billingService;
    private final NotificationService notificationService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              UserRepository userRepository,
                              DoctorRepository doctorRepository,
                              BillingService billingService,
                              NotificationService notificationService) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.billingService = billingService;
        this.notificationService = notificationService;
    }

    public List<Appointment> search(String keyword, String status, String from, String to) {
        AppointmentStatus st = null;
        if (status != null && !status.isBlank()) {
            st = AppointmentStatus.fromString(status);
        }
        return appointmentRepository.findAll(keyword, st, parseOptionalDate(from), parseOptionalDate(to));
    }

    public List<Appointment> forPatient(int patientId) {
        return appointmentRepository.findByPatient(patientId);
    }

    public List<Appointment> forProvider(int providerId) {
        return appointmentRepository.findByProvider(providerId);
    }

    public Appointment getById(int id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ServiceException("Appointment not found"));
    }

    public int book(int patientId, int providerId, ProviderType type, LocalDate date,
                    String timeSlot, String reason, AppointmentType appointmentType,
                    Priority priority, String symptoms) {
        return book(patientId, providerId, type, date, timeSlot, reason, appointmentType, priority, symptoms, null);
    }

    public int book(int patientId, int providerId, ProviderType type, LocalDate date,
                    String timeSlot, String reason, AppointmentType appointmentType,
                    Priority priority, String symptoms, BigDecimal customFeePkr) {
        validateBooking(patientId, providerId, type, date, timeSlot, null);

        AppointmentType apptType = appointmentType != null ? appointmentType : AppointmentType.CONSULTATION;
        Appointment a = new Appointment();
        a.setPatientId(patientId);
        a.setProviderId(providerId);
        a.setProviderType(type);
        a.setAppointmentDate(date);
        a.setTimeSlot(timeSlot.trim());
        a.setStatus(AppointmentStatus.PENDING);
        a.setAppointmentType(apptType);
        a.setPriority(priority != null ? priority : Priority.NORMAL);
        a.setSymptoms(symptoms);
        a.setRoomNumber(assignRoom(apptType));
        a.setFeeAmount(resolveFee(providerId, type, apptType, customFeePkr));
        a.setReason(reason);

        int id = appointmentRepository.create(a);
        a.setId(id);
        billingService.createForAppointment(a);

        notificationService.notify(patientId, "Appointment booked",
                "Your visit on " + date + " at " + timeSlot + " is pending confirmation.");
        notificationService.notify(providerId, "New appointment request",
                "Patient booked for " + date + " " + timeSlot);

        return id;
    }

    public void updateAppointment(Appointment appointment) {
        validateBooking(appointment.getPatientId(), appointment.getProviderId(),
                appointment.getProviderType(), appointment.getAppointmentDate(),
                appointment.getTimeSlot(), appointment.getId());
        appointmentRepository.update(appointment);
    }

    public void updateStatus(int id, AppointmentStatus status) {
        Appointment a = getById(id);
        validateStatusTransition(a.getStatus(), status);
        appointmentRepository.updateStatus(id, status);

        notificationService.notify(a.getPatientId(), "Appointment " + status.name().toLowerCase(),
                "Your appointment on " + a.getAppointmentDate() + " is now " + status.name());

        if (status == AppointmentStatus.COMPLETED) {
            billingService.createForAppointment(a);
        }
    }

    public void cancel(int id) {
        updateStatus(id, AppointmentStatus.CANCELLED);
    }

    public void delete(int id) {
        Appointment appointment = getById(id);
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new ServiceException("Completed appointments cannot be deleted");
        }
        appointmentRepository.delete(id);
    }

    public BigDecimal resolveFee(int providerId, ProviderType type, AppointmentType appointmentType,
                                 BigDecimal customFeePkr) {
        if (customFeePkr != null && customFeePkr.compareTo(BigDecimal.ZERO) > 0) {
            return customFeePkr.setScale(0, java.math.RoundingMode.HALF_UP);
        }
        BigDecimal base;
        if (type == ProviderType.DOCTOR) {
            base = doctorRepository.findByUserId(providerId)
                    .map(DoctorProfile::getConsultationFee)
                    .orElse(BigDecimal.valueOf(3000));
        } else {
            base = NURSE_BASE_FEE_PKR;
        }
        return base.multiply(BigDecimal.valueOf(appointmentType.feeMultiplier()))
                .setScale(0, java.math.RoundingMode.HALF_UP);
    }

    private String assignRoom(AppointmentType type) {
        return switch (type) {
            case EMERGENCY -> "ER-1";
            case VACCINATION -> "VAX-2";
            default -> "OPD-" + (int) (Math.random() * 8 + 1);
        };
    }

    private void validateBooking(int patientId, int providerId, ProviderType type,
                                 LocalDate date, String timeSlot, Integer excludeId) {
        ValidationUtil.requireNonBlank(timeSlot, "Time slot");
        if (date == null) {
            throw new ServiceException("Appointment date is required");
        }
        if (date.isBefore(LocalDate.now())) {
            throw new ServiceException("Cannot book appointments in the past");
        }

        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new ServiceException("Patient not found"));
        if (patient.getRole() != Role.PATIENT) {
            throw new ServiceException("Invalid patient");
        }

        User provider = userRepository.findById(providerId)
                .orElseThrow(() -> new ServiceException("Provider not found"));
        if (type == ProviderType.DOCTOR && provider.getRole() != Role.DOCTOR) {
            throw new ServiceException("Selected provider is not a doctor");
        }
        if (type == ProviderType.NURSE && provider.getRole() != Role.NURSE) {
            throw new ServiceException("Selected provider is not a nurse");
        }

        if (appointmentRepository.hasConflict(providerId, date, timeSlot.trim(), excludeId)) {
            throw new ServiceException("This time slot is already booked for the provider");
        }
    }

    private void validateStatusTransition(AppointmentStatus current, AppointmentStatus target) {
        if (current == target) {
            return;
        }
        if (current == AppointmentStatus.CANCELLED && target != AppointmentStatus.CANCELLED) {
            throw new ServiceException("Cancelled appointments cannot be reopened");
        }
        if (current == AppointmentStatus.COMPLETED) {
            throw new ServiceException("Completed appointments cannot change status");
        }
    }

    private LocalDate parseOptionalDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return ValidationUtil.parseDate(value, "date");
    }
}
