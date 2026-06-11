package com.elcinic.repository;

import com.elcinic.model.ChatMessage;

import java.util.List;

public interface ChatRepository {
    List<ChatMessage> findThread(int userA, int userB, Integer appointmentId);
    int create(ChatMessage message);
    void markThreadRead(int currentUserId, int otherUserId, Integer appointmentId);
}
