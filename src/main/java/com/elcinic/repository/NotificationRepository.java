package com.elcinic.repository;

import com.elcinic.model.Notification;

import java.util.List;

public interface NotificationRepository {
    List<Notification> findByUser(int userId, boolean unreadOnly);

    int countUnread(int userId);

    int create(Notification n);

    void markRead(int id, int userId);

    void markAllRead(int userId);
}
