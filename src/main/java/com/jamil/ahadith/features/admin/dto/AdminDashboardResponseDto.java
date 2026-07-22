package com.jamil.ahadith.features.admin.dto;

public record AdminDashboardResponseDto(
        long ahadith,
        long books,
        long rawis,
        long rulings,
        long topics,
        long muhaddiths,
        long users
) {
}
