package com.footballacademy.config.scheduler;

import com.footballacademy.services.scouting.AcademyPerformanceRankingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AcademyPerformanceRankingJob {
    private static final Logger logger = LoggerFactory.getLogger(AcademyPerformanceRankingJob.class);
    private final AcademyPerformanceRankingService rankingService;

    public AcademyPerformanceRankingJob(AcademyPerformanceRankingService rankingService) {
        this.rankingService = rankingService;
    }

    @Scheduled(cron = "0 30 3 * * *", zone = "Africa/Tunis")
    public void recomputeAcademyRankings() {
        try {
            rankingService.recomputeAllAcademyScores();
        } catch (Exception e) {
            logger.warn("Academy ranking recomputation failed: {}", e.getMessage());
        }
    }
}
