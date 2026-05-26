package com.elcinic.model;

import java.time.LocalDateTime;

public class VitalSigns {
    private int id;
    private int appointmentId;
    private String bloodPressure;
    private Integer pulse;
    private Double temperature;
    private Double weightKg;
    private Double heightCm;
    private int recordedBy;
    private String recordedByName;
    private LocalDateTime recordedAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getAppointmentId() { return appointmentId; }
    public void setAppointmentId(int appointmentId) { this.appointmentId = appointmentId; }
    public String getBloodPressure() { return bloodPressure; }
    public void setBloodPressure(String bloodPressure) { this.bloodPressure = bloodPressure; }
    public Integer getPulse() { return pulse; }
    public void setPulse(Integer pulse) { this.pulse = pulse; }
    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
    public Double getWeightKg() { return weightKg; }
    public void setWeightKg(Double weightKg) { this.weightKg = weightKg; }
    public Double getHeightCm() { return heightCm; }
    public void setHeightCm(Double heightCm) { this.heightCm = heightCm; }
    public int getRecordedBy() { return recordedBy; }
    public void setRecordedBy(int recordedBy) { this.recordedBy = recordedBy; }
    public String getRecordedByName() { return recordedByName; }
    public void setRecordedByName(String recordedByName) { this.recordedByName = recordedByName; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}
