package com.footballacademy.DTO;

public
record ParentCombinedDTO(Long id, Long userId, String name, String email, String phone, Integer childrenCount) {
}
