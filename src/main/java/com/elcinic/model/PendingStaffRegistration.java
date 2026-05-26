package com.elcinic.model;

import java.time.LocalDateTime;

/**
 * Staff account awaiting admin verification (doctor or nurse).
 */
public class PendingStaffRegistration {
    private int userId;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private Role role;
    private LocalDateTime registeredAt;
    private String specialization;
    private String licenseNumber;
    private String department;

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
}
