package com.footballacademy.repository;

import com.footballacademy.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public
interface ConversationRepository extends JpaRepository<Conversation, Long> {
    @Query("select distinct c from Conversation c left join fetch c.participantIds where :userId member of c.participantIds") List<Conversation> findByParticipantIdFetchParticipants(
    @Param("userId") Long userId);
    @Query("select c from Conversation c left join fetch c.participantIds where c.id = :conversationId") Optional<Conversation> findByIdFetchParticipants(
    @Param("conversationId") Long conversationId);
    @Query("select c from Conversation c where c.type = 'DIRECT' and :a member of c.participantIds and :b member of c.participantIds") Optional<Conversation> findFirstDirectBetween(
    @Param("a") Long a,
    @Param("b") Long b);
    @Query("select c from Conversation c where c.type = :type and c.divisionId = :divisionId") Optional<Conversation> findFirstByTypeAndDivisionId(
    @Param("type") Conversation.ConversationType type,
    @Param("divisionId") Long divisionId);
    List<Conversation> findByAcademy_Id(Long academyId);
    default Optional<Conversation> findDirectBetween(Long u1, Long u2) {
        return findFirstDirectBetween(u1, u2);
    } default Optional<Conversation> findDivisionGroup(Long divisionId) {
        return findFirstByTypeAndDivisionId(Conversation.ConversationType.DIVISION, divisionId);
    }
}
