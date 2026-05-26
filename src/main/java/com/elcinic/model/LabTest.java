package com.elcinic.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class LabTest {
    private int id;
    private int patientId;
    private String patientName;
    private int doctorId;
    private String doctorName;
    private String testName;
    private String resultValue;
    private String resultUnit;
    private LabStatus status;
    private LocalDate orderedDate;
    private LocalDate completedDate;
    private String notes;
    private LocalDateTime createdAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }
    public String getResultValue() { return resultValue; }
    public void setResultValue(String resultValue) { this.resultValue = resultValue; }
    public String getResultUnit() { return resultUnit; }
    public void setResultUnit(String resultUnit) { this.resultUnit = resultUnit; }
    public LabStatus getStatus() { return status; }
    public void setStatus(LabStatus status) { this.status = status; }
    public LocalDate getOrderedDate() { return orderedDate; }
    public void setOrderedDate(LocalDate orderedDate) { this.orderedDate = orderedDate; }
    public LocalDate getCompletedDate() { return completedDate; }
    public void setCompletedDate(LocalDate completedDate) { this.completedDate = completedDate; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
