package com.footballacademy.DTO.dashboard;

import java.util.List;

public
record ParentDashboardDTO(Long parentId, int childrenCount, double unpaidTotal, int unpaidMonths, List<ChildStatsDTO> children) {
}
