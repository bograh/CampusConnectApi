package com.campusconnect.api.service;

import com.campusconnect.api.security.JwtService;
import com.campusconnect.api.dto.message.MessageResponseDTO;
import com.campusconnect.api.dto.message.SendMessageRequestDTO;
import com.campusconnect.api.entity.Message;
import com.campusconnect.api.entity.User;
import com.campusconnect.api.entity.enums.MessageType;
import com.campusconnect.api.exception.NotFoundException;
import com.campusconnect.api.repository.MessageRepository;
import com.campusconnect.api.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Transactional
    public MessageResponseDTO sendMessage(SendMessageRequestDTO request, HttpServletRequest httpRequest) {
        String email = jwtService.getEmailFromToken(httpRequest);
        User sender = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Message message = Message.builder()
                .conversationId(request.getConversationId())
                .sender(sender)
                .content(request.getContent())
                .type(request.getType())
                .isRead(false)
                .build();

        message = messageRepository.save(message);

        return mapToResponseDTO(message);
    }

    public List<MessageResponseDTO> getConversationMessages(String conversationId) {
        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        return messages.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<String> getUserConversations(HttpServletRequest httpRequest) {
        String email = jwtService.getEmailFromToken(httpRequest);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return messageRepository.findConversationIdsByUserId(user.getId());
    }

    @Transactional
    public void markMessagesAsRead(String conversationId, HttpServletRequest httpRequest) {
        String email = jwtService.getEmailFromToken(httpRequest);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        List<Message> unreadMessages = messageRepository.findUnreadMessagesByConversationAndUser(
                conversationId, user.getId());

        for (Message message : unreadMessages) {
            message.setIsRead(true);
        }

        messageRepository.saveAll(unreadMessages);
    }

    public long getUnreadMessageCount(String conversationId, HttpServletRequest httpRequest) {
        String email = jwtService.getEmailFromToken(httpRequest);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        List<Message> unreadMessages = messageRepository.findUnreadMessagesByConversationAndUser(
                conversationId, user.getId());

        return unreadMessages.size();
    }

    @Transactional
    public MessageResponseDTO sendSystemMessage(String conversationId, String content) {
        Message message = Message.builder()
                .conversationId(conversationId)
                .sender(null)
                .content(content)
                .type(MessageType.SYSTEM)
                .isRead(false)
                .build();

        message = messageRepository.save(message);

        return mapToResponseDTO(message);
    }

    public String generateConversationId(String userId1, String userId2) {
        String[] ids = {userId1, userId2};
        java.util.Arrays.sort(ids);
        return String.join("-", ids);
    }

    @Transactional
    public MessageResponseDTO sendDeliveryNotification(String requesterId, String travelerId, String content) {
        String conversationId = generateConversationId(requesterId, travelerId);
        return sendSystemMessage(conversationId, content);
    }

    private MessageResponseDTO mapToResponseDTO(Message message) {
        MessageResponseDTO response = new MessageResponseDTO();
        response.setId(message.getId());
        response.setConversationId(message.getConversationId());
        
        if (message.getSender() != null) {
            response.setSenderId(message.getSender().getId());
            response.setSenderName(message.getSender().getFirstName() + " " + message.getSender().getLastName());
        } else {
            response.setSenderId("system");
            response.setSenderName("System");
        }
        
        response.setContent(message.getContent());
        response.setType(message.getType());
        response.setIsRead(message.getIsRead());
        response.setCreatedAt(message.getCreatedAt());
        
        return response;
    }
}
