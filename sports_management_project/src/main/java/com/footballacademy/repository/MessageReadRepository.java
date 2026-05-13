package com.footballacademy.repository;

import com.footballacademy.model.MessageRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Repository
public interface MessageReadRepository extends JpaRepository<MessageRead, Long> {

    // =====================================================
    // === COUNT UNREAD GROUP MESSAGES
    // =====================================================

    /**
     * Count unread messages for a user in a group/division conversation.
     *
     * A message is considered unread if:
     * - receiver_id IS NULL (group message)
     * - sender_id != :userId
     * - no entry exists in message_reads for this user
     */
    @Query(value = """
        SELECT COUNT(*)
          FROM messages m
         WHERE m.conversation_id = :conversationId
           AND m.receiver_id IS NULL
           AND m.sender_id <> :userId
           AND NOT EXISTS (
               SELECT 1
                 FROM message_reads r
                WHERE r.message_id = m.id
                  AND r.user_id = :userId
           )
    """, nativeQuery = true)
    long countUnreadForUser(
            @Param("conversationId") Long conversationId,
            @Param("userId") Long userId
    );

    // =====================================================
    // === CHECK READ STATUS
    // =====================================================

    boolean existsByMessageIdAndUserId(Long messageId, Long userId);

    // =====================================================
    // === UPSERT READ ENTRY (MySQL)
    // =====================================================

    /**
     * Upsert a message read entry.
     *
     * MySQL-specific implementation using ON DUPLICATE KEY UPDATE.
     */
    @Transactional
    @Modifying
    @Query(value = """
        INSERT INTO message_reads (message_id, user_id, read_at)
        VALUES (:messageId, :userId, :readAt)
        ON DUPLICATE KEY UPDATE read_at = :readAt
    """, nativeQuery = true)
    void upsert(
            @Param("messageId") Long messageId,
            @Param("userId") Long userId,
            @Param("readAt") LocalDateTime readAt
    );
}
