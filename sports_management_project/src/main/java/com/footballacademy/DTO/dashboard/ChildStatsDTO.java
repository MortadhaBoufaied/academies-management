package com.footballacademy.DTO.dashboard;

public
record ChildStatsDTO(Long playerId, String name, Long divisionId, Integer goals, Integer assists, Integer matches, Double averageRating) {
}
