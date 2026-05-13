package com.footballacademy.DTO;

import java.time.LocalDate;

public
record PlayerCombinedDTO(Long id, Long userId, String nom, String email, String tel, LocalDate dateNaissance, String position, Integer age, String nationalite, String imageUrl, boolean paid, Double height, Double weight, Integer goals, Integer assists, Integer matches, Double rating, Long divisionId, Long parentId, Long trainerId) {
}
