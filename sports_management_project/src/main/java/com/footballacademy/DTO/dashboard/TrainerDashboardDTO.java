package com.footballacademy.DTO.dashboard;

import java.util.List;

public
record TrainerDashboardDTO(Long trainerId, Long divisionId, String divisionName, int playersCount, int activitiesThisMonth, List<ActivitySummaryDTO> activities) {
}
