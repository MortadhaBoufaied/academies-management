package com.footballacademy.DTO;

import com.footballacademy.model.PlayerRanking.Tier;

public
record PlayerRankingDTO(Long playerId, String name, String position, Integer age, Integer goals, Integer assists, Integer matches, Double averageRating, String divisionName, String trainerName, Double score, Tier tier, String imageUrl) {
}
