package com.footballacademy.repository;

import com.footballacademy.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface VideoAnalysisRepository extends JpaRepository<VideoAnalysis, Long> { Optional<VideoAnalysis> findByVideoAsset_Id(Long videoAssetId); }
