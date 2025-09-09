package com.campusconnect.api.repository;

import com.campusconnect.api.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {
    List<Message> findByConversationIdOrderByCreatedAtAsc(String conversationId);

    @Query("SELECT DISTINCT m.conversationId FROM Message m WHERE m.sender.id = :userId OR " +
            "m.conversationId IN (SELECT m2.conversationId FROM Message m2 WHERE m2.sender.id != :userId)")
    List<String> findConversationIdsByUserId(String userId);

    @Query("SELECT m FROM Message m WHERE m.conversationId = :conversationId AND m.sender.id != :userId AND m.isRead = false")
    List<Message> findUnreadMessagesByConversationAndUser(String conversationId, String userId);
}
