package com.campusconnect.api.dto.message;

import com.campusconnect.api.entity.enums.MessageType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageResponseDTO {
    private String id;
    private String conversationId;
    private String senderId;
    private String senderName;
    private String content;
    private MessageType type;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
