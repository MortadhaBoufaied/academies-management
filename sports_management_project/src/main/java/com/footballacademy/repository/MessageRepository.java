package com.footballacademy.repository;

import com.footballacademy.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // =====================================================
    // === BASIC MESSAGE QUERIES
    // =====================================================

    List<Message> findByConversationIdOrderByTimestampAsc(Long conversationId);

    Message findTopByConversationIdOrderByTimestampDesc(Long conversationId);

    long countByConversationIdAndReceiverIdAndReadFalse(
            Long conversationId,
            Long receiverId
    );

    List<Message> findByConversationIdAndReceiverIdAndReadFalse(
            Long conversationId,
            Long receiverId
    );

    /**
     * For group/division conversations (receiverId = null).
     */
    List<Message> findByConversationIdAndReceiverIdIsNullOrderByTimestampAsc(
            Long conversationId
    );

    // =====================================================
    // === BULK UPDATE OPERATIONS
    // =====================================================

    /**
     * Bulk mark direct messages as read for a specific receiver.
     *
     * Uses JPQL UPDATE for performance.
     */
    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE Message m
           SET m.read = true
         WHERE m.conversationId = :convId
           AND m.receiverId = :userId
           AND m.read = false
    """)
    int markDirectAsRead(
            @Param("convId") Long conversationId,
            @Param("userId") Long userId
    );

    // =====================================================
    // === GROUP / DIVISION HELPERS
    // =====================================================

    /**
     * For GROUP/DIVISION conversations:
     * Returns all message IDs not sent by the given user.
     *
     * Used for message_reads upserts.
     *
     * IMPORTANT:
     * If your Message entity uses m.conversation instead of m.conversationId,
     * replace m.conversationId with m.conversation.id.
     */
    @Query("""
        SELECT m.id
          FROM Message m
         WHERE m.conversationId = :convId
           AND (m.senderId IS NULL OR m.senderId <> :userId)
    """)
    List<Long> findAllMessageIdsForConversationExceptSender(
            @Param("convId") Long conversationId,
            @Param("userId") Long userId
    );
}
