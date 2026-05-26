package com.elcinic.service;

import com.elcinic.repository.*;

public final class ServiceFactory {

    private static final UserRepository USER_REPO = new JdbcUserRepository();
    private static final PatientRepository PATIENT_REPO = new JdbcPatientRepository();
    private static final DoctorRepository DOCTOR_REPO = new JdbcDoctorRepository();
    private static final NurseRepository NURSE_REPO = new JdbcNurseRepository();
    private static final AppointmentRepository APPOINTMENT_REPO = new JdbcAppointmentRepository();
    private static final MedicalRecordRepository RECORD_REPO = new JdbcMedicalRecordRepository();
    private static final InvoiceRepository INVOICE_REPO = new JdbcInvoiceRepository();
    private static final NotificationRepository NOTIFICATION_REPO = new JdbcNotificationRepository();
    private static final LabTestRepository LAB_REPO = new JdbcLabTestRepository();
    private static final VitalSignsRepository VITAL_REPO = new JdbcVitalSignsRepository();
    private static final PrescriptionRepository PRESCRIPTION_REPO = new JdbcPrescriptionRepository();
    private static final DashboardRepository DASHBOARD_REPO = new DashboardRepository();

    private static final NotificationService NOTIFICATION_SERVICE = new NotificationService(NOTIFICATION_REPO);
    private static final BillingService BILLING_SERVICE = new BillingService(INVOICE_REPO);
    private static final AuthService AUTH_SERVICE = new AuthService(USER_REPO);
    private static final RegistrationService REGISTRATION_SERVICE =
            new RegistrationService(USER_REPO, DOCTOR_REPO, NURSE_REPO, NOTIFICATION_SERVICE);
    private static final StaffVerificationService STAFF_VERIFICATION_SERVICE =
            new StaffVerificationService(USER_REPO, NOTIFICATION_SERVICE);
    private static final UserService USER_SERVICE = new UserService(USER_REPO, PATIENT_REPO, DOCTOR_REPO, NURSE_REPO);
    private static final AppointmentService APPOINTMENT_SERVICE =
            new AppointmentService(APPOINTMENT_REPO, USER_REPO, DOCTOR_REPO, BILLING_SERVICE, NOTIFICATION_SERVICE);
    private static final MedicalRecordService RECORD_SERVICE =
            new MedicalRecordService(RECORD_REPO, USER_REPO, PRESCRIPTION_REPO, NOTIFICATION_SERVICE);
    private static final PatientService PATIENT_SERVICE = new PatientService(PATIENT_REPO, DOCTOR_REPO, USER_REPO);
    private static final LabService LAB_SERVICE = new LabService(LAB_REPO, USER_REPO);
    private static final VitalService VITAL_SERVICE = new VitalService(VITAL_REPO);
    private static final DashboardService DASHBOARD_SERVICE = new DashboardService(DASHBOARD_REPO, NOTIFICATION_SERVICE);

    static {
        LAB_SERVICE.setNotificationService(NOTIFICATION_SERVICE);
    }

    private ServiceFactory() {
    }

    public static AuthService authService() { return AUTH_SERVICE; }
    public static RegistrationService registrationService() { return REGISTRATION_SERVICE; }
    public static StaffVerificationService staffVerificationService() { return STAFF_VERIFICATION_SERVICE; }
    public static UserService userService() { return USER_SERVICE; }
    public static AppointmentService appointmentService() { return APPOINTMENT_SERVICE; }
    public static MedicalRecordService medicalRecordService() { return RECORD_SERVICE; }
    public static PatientService patientService() { return PATIENT_SERVICE; }
    public static BillingService billingService() { return BILLING_SERVICE; }
    public static NotificationService notificationService() { return NOTIFICATION_SERVICE; }
    public static LabService labService() { return LAB_SERVICE; }
    public static VitalService vitalService() { return VITAL_SERVICE; }
    public static DashboardService dashboardService() { return DASHBOARD_SERVICE; }
}
