package com.jamil.ahadith.features.admin.controller;

import com.jamil.ahadith.features.admin.dto.AdminDashboardResponseDto;
import com.jamil.ahadith.features.admin.service.AdminDashboardService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping({"/admin/dashboard", "/api/v1/admin/dashboard"})
public class AdminDashboardController {
    private final AdminDashboardService adminDashboardService;

    @GetMapping
    public AdminDashboardResponseDto getDashboard() {
        return adminDashboardService.getDashboard();
    }
}
