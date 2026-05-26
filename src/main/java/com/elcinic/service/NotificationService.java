package com.elcinic.service;

import com.elcinic.model.Notification;
import com.elcinic.repository.NotificationRepository;

import java.util.List;

public class NotificationService {

    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    public List<Notification> list(int userId, boolean unreadOnly) {
        return repository.findByUser(userId, unreadOnly);
    }

    public int unreadCount(int userId) {
        return repository.countUnread(userId);
    }

    public void notify(int userId, String title, String message) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setTitle(title);
        n.setMessage(message);
        repository.create(n);
    }

    public void markRead(int id, int userId) {
        repository.markRead(id, userId);
    }

    public void markAllRead(int userId) {
        repository.markAllRead(userId);
    }
}
