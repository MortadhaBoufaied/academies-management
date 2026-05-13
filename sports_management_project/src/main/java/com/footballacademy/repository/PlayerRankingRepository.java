package com.footballacademy.repository;

import com.footballacademy.model.PlayerRanking;
import com.footballacademy.model.PlayerRanking.Tier;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public
interface PlayerRankingRepository extends JpaRepository<PlayerRanking, Long> {
    Optional<PlayerRanking> findByPlayerId(Long playerId);
    List<PlayerRanking> findAllByOrderByScoreDesc();
    List<PlayerRanking> findByTierOrderByScoreDesc(Tier tier);
}
