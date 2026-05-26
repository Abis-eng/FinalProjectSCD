package com.elcinic.model;

public class PrescriptionItem {
    private int id;
    private int medicalRecordId;
    private String medicationName;
    private String dosage;
    private String frequency;
    private int durationDays;
    private String instructions;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getMedicalRecordId() { return medicalRecordId; }
    public void setMedicalRecordId(int medicalRecordId) { this.medicalRecordId = medicalRecordId; }
    public String getMedicationName() { return medicationName; }
    public void setMedicationName(String medicationName) { this.medicationName = medicationName; }
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public int getDurationDays() { return durationDays; }
    public void setDurationDays(int durationDays) { this.durationDays = durationDays; }
    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
}
