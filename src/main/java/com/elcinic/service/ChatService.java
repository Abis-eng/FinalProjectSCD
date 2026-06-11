package com.elcinic.service;

import com.elcinic.exception.ServiceException;
import com.elcinic.model.Appointment;
import com.elcinic.model.ChatMessage;
import com.elcinic.model.Role;
import com.elcinic.model.User;
import com.elcinic.repository.ChatRepository;
import com.elcinic.repository.UserRepository;
import com.elcinic.utility.ValidationUtil;

import java.util.List;

public class ChatService {
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final AppointmentService appointmentService;

    public ChatService(ChatRepository chatRepository, UserRepository userRepository, AppointmentService appointmentService) {
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
        this.appointmentService = appointmentService;
    }

    public List<ChatMessage> thread(User currentUser, int otherUserId, Integer appointmentId) {
        assertCanTalk(currentUser, otherUserId, appointmentId);
        List<ChatMessage> messages = chatRepository.findThread(currentUser.getId(), otherUserId, appointmentId);
        chatRepository.markThreadRead(currentUser.getId(), otherUserId, appointmentId);
        return messages;
    }

    public void send(User currentUser, int otherUserId, Integer appointmentId, String content) {
        assertCanTalk(currentUser, otherUserId, appointmentId);
        ValidationUtil.requireNonBlank(content, "Message");
        ChatMessage msg = new ChatMessage();
        msg.setSenderId(currentUser.getId());
        msg.setReceiverId(otherUserId);
        msg.setAppointmentId(appointmentId);
        msg.setContent(content.trim());
        chatRepository.create(msg);
    }

    private void assertCanTalk(User currentUser, int otherUserId, Integer appointmentId) {
        User other = userRepository.findById(otherUserId)
                .orElseThrow(() -> new ServiceException("Chat user not found"));
        if (currentUser.getId() == otherUserId) {
            throw new ServiceException("Cannot chat with yourself");
        }
        if (appointmentId != null) {
            Appointment appt = appointmentService.getById(appointmentId);
            boolean related = (appt.getPatientId() == currentUser.getId() && appt.getProviderId() == otherUserId)
                    || (appt.getProviderId() == currentUser.getId() && appt.getPatientId() == otherUserId)
                    || currentUser.getRole() == Role.ADMIN;
            if (!related) {
                throw new ServiceException("You can only use appointment chat with related users");
            }
            return;
        }

        // General chat rules
        if (currentUser.getRole() == Role.ADMIN || other.getRole() == Role.ADMIN) {
            return;
        }
        if (currentUser.getRole() == Role.PATIENT && (other.getRole() == Role.DOCTOR || other.getRole() == Role.NURSE)) {
            return;
        }
        if ((currentUser.getRole() == Role.DOCTOR || currentUser.getRole() == Role.NURSE) && other.getRole() == Role.PATIENT) {
            return;
        }
        throw new ServiceException("General chat is only allowed between patient and medical staff");
    }
}
