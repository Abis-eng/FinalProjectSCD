package com.elcinic.model;

public class DashboardStats {
    private int totalPatients;
    private int totalDoctors;
    private int todayAppointments;
    private int pendingAppointments;
    private int completedToday;
    private int unpaidInvoices;
    private double revenueToday;
    private int unreadNotifications;
    private int pendingLabs;

    public int getTotalPatients() { return totalPatients; }
    public void setTotalPatients(int totalPatients) { this.totalPatients = totalPatients; }
    public int getTotalDoctors() { return totalDoctors; }
    public void setTotalDoctors(int totalDoctors) { this.totalDoctors = totalDoctors; }
    public int getTodayAppointments() { return todayAppointments; }
    public void setTodayAppointments(int todayAppointments) { this.todayAppointments = todayAppointments; }
    public int getPendingAppointments() { return pendingAppointments; }
    public void setPendingAppointments(int pendingAppointments) { this.pendingAppointments = pendingAppointments; }
    public int getCompletedToday() { return completedToday; }
    public void setCompletedToday(int completedToday) { this.completedToday = completedToday; }
    public int getUnpaidInvoices() { return unpaidInvoices; }
    public void setUnpaidInvoices(int unpaidInvoices) { this.unpaidInvoices = unpaidInvoices; }
    public double getRevenueToday() { return revenueToday; }
    public void setRevenueToday(double revenueToday) { this.revenueToday = revenueToday; }
    public int getUnreadNotifications() { return unreadNotifications; }
    public void setUnreadNotifications(int unreadNotifications) { this.unreadNotifications = unreadNotifications; }
    public int getPendingLabs() { return pendingLabs; }
    public void setPendingLabs(int pendingLabs) { this.pendingLabs = pendingLabs; }
}
