package com.campusconnect.api.controller;

import com.campusconnect.api.dto.message.MessageResponseDTO;
import com.campusconnect.api.dto.message.SendMessageRequestDTO;
import com.campusconnect.api.service.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<MessageResponseDTO> sendMessage(
            @Valid @RequestBody SendMessageRequestDTO request,
            HttpServletRequest httpRequest) {
        MessageResponseDTO response = messageService.sendMessage(request, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<List<MessageResponseDTO>> getConversationMessages(
            @PathVariable String conversationId) {
        List<MessageResponseDTO> messages = messageService.getConversationMessages(conversationId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<String>> getUserConversations(HttpServletRequest httpRequest) {
        List<String> conversationIds = messageService.getUserConversations(httpRequest);
        return ResponseEntity.ok(conversationIds);
    }

    @PutMapping("/conversations/{conversationId}/read")
    public ResponseEntity<String> markMessagesAsRead(
            @PathVariable String conversationId,
            HttpServletRequest httpRequest) {
        messageService.markMessagesAsRead(conversationId, httpRequest);
        return ResponseEntity.ok("Messages marked as read");
    }

    @GetMapping("/conversations/{conversationId}/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadMessageCount(
            @PathVariable String conversationId,
            HttpServletRequest httpRequest) {
        long count = messageService.getUnreadMessageCount(conversationId, httpRequest);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @PostMapping("/system")
    public ResponseEntity<MessageResponseDTO> sendSystemMessage(
            @RequestParam String conversationId,
            @RequestParam String content) {
        MessageResponseDTO response = messageService.sendSystemMessage(conversationId, content);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/delivery-notification")
    public ResponseEntity<MessageResponseDTO> sendDeliveryNotification(
            @RequestParam String requesterId,
            @RequestParam String travelerId,
            @RequestParam String content) {
        MessageResponseDTO response = messageService.sendDeliveryNotification(requesterId, travelerId, content);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
