package com.elcinic.model;

import java.time.LocalDate;

public class PatientProfile {
    private int userId;
    private String fullName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private String bloodType;
    private Integer assignedDoctorId;
    private String assignedDoctorName;
    private Integer requestedDoctorId;
    private String requestedDoctorName;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public Integer getAssignedDoctorId() {
        return assignedDoctorId;
    }

    public void setAssignedDoctorId(Integer assignedDoctorId) {
        this.assignedDoctorId = assignedDoctorId;
    }

    public String getAssignedDoctorName() {
        return assignedDoctorName;
    }

    public void setAssignedDoctorName(String assignedDoctorName) {
        this.assignedDoctorName = assignedDoctorName;
    }

    public Integer getRequestedDoctorId() {
        return requestedDoctorId;
    }

    public void setRequestedDoctorId(Integer requestedDoctorId) {
        this.requestedDoctorId = requestedDoctorId;
    }

    public String getRequestedDoctorName() {
        return requestedDoctorName;
    }

    public void setRequestedDoctorName(String requestedDoctorName) {
        this.requestedDoctorName = requestedDoctorName;
    }
}
