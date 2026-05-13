package com.footballacademy.repository;

import com.footballacademy.model.ChatbotData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public
interface ChatbotDataRepository extends JpaRepository<ChatbotData, Long> {
    List<ChatbotData> findByScopeOrderByUploadedAtDesc(ChatbotData.Scope scope);
    List<ChatbotData> findByScopeAndAcademy_IdOrderByUploadedAtDesc(ChatbotData.Scope scope, Long academyId);
    List<ChatbotData> findByScopeAndAcademy_IdAndSport_IdOrderByUploadedAtDesc(ChatbotData.Scope scope, Long academyId, Long sportId);
    List<ChatbotData> findByScopeAndSport_IdOrderByUploadedAtDesc(ChatbotData.Scope scope, Long sportId);
}
