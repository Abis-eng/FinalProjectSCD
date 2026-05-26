package com.elcinic.service;

import com.elcinic.model.DashboardStats;
import com.elcinic.model.Role;
import com.elcinic.model.User;
import com.elcinic.repository.DashboardRepository;

public class DashboardService {

    private final DashboardRepository dashboardRepository;
    private final NotificationService notificationService;

    public DashboardService(DashboardRepository dashboardRepository,
                            NotificationService notificationService) {
        this.dashboardRepository = dashboardRepository;
        this.notificationService = notificationService;
    }

    public DashboardStats forUser(User user) {
        DashboardStats stats;
        if (user.getRole() == Role.ADMIN) {
            stats = dashboardRepository.loadAdminStats();
        } else {
            stats = dashboardRepository.loadForUser(user.getId(), user.getRole().name());
        }
        stats.setUnreadNotifications(notificationService.unreadCount(user.getId()));
        return stats;
    }
}
