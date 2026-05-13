package com.footballacademy.DTO.dashboard;

public
record AdminDashboardDTO(long users, long players, long trainers, long parents, long divisions, long activities, double monthlyRevenue, long pendingPayments, long overduePayments) {
}
