package com.campusconnect.api.dto.message;

import com.campusconnect.api.entity.enums.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SendMessageRequestDTO {
    @NotBlank(message = "Conversation ID is required")
    private String conversationId;

    @NotBlank(message = "Content is required")
    private String content;

    @NotNull(message = "Message type is required")
    private MessageType type = MessageType.TEXT;
}
