package com.footballacademy.repository;

import com.footballacademy.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface VideoAssetRepository extends JpaRepository<VideoAsset, Long> { List<VideoAsset> findByMatch_Id(Long matchId); List<VideoAsset> findByPlayer_Id(Long playerId); }
