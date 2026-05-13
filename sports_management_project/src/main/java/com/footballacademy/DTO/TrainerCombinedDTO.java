package com.footballacademy.DTO;

public
record TrainerCombinedDTO(Long id, Long userId, String name, String email, String phone, String specialty, String experience, String license, String notes, Long divisionId) {
}
