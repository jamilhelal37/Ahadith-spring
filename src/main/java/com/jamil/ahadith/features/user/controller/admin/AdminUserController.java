package com.jamil.ahadith.features.user.controller.admin;

import com.jamil.ahadith.core.web.dto.SearchResponse;
import com.jamil.ahadith.features.user.dto.request.AdminUserStatusUpdateRequestDto;
import com.jamil.ahadith.features.user.dto.request.AdminUserTypeUpdateRequestDto;
import com.jamil.ahadith.features.user.dto.response.AdminUserResponseDto;
import com.jamil.ahadith.features.user.entity.UserStatus;
import com.jamil.ahadith.features.user.entity.UserType;
import com.jamil.ahadith.features.user.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "Admin Users")
public class AdminUserController {
    private final AdminUserService adminUserService;

    @GetMapping
    @Operation(summary = "Search and filter users")
    public SearchResponse<AdminUserResponseDto> search(@RequestParam(required = false) String q,
                                                       @RequestParam(required = false) UserStatus status,
                                                       @RequestParam(required = false) UserType type,
                                                       @RequestParam(defaultValue = "0") Integer page,
                                                       @RequestParam(defaultValue = "20") Integer size,
                                                       @RequestParam(required = false) String sort) {
        return adminUserService.search(q, status, type, page, size, sort);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user details")
    public AdminUserResponseDto getById(@PathVariable UUID id) {
        return adminUserService.getById(id);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update user status")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = "{\"status\":\"disabled\"}")
    ))
    public AdminUserResponseDto updateStatus(@PathVariable UUID id,
                                             @Valid @RequestBody AdminUserStatusUpdateRequestDto request) {
        return adminUserService.updateStatus(id, request);
    }

    @PutMapping("/{id}/type")
    @Operation(summary = "Update user type")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = "{\"type\":\"scholar\"}")
    ))
    public AdminUserResponseDto updateType(@PathVariable UUID id,
                                           @Valid @RequestBody AdminUserTypeUpdateRequestDto request) {
        return adminUserService.updateType(id, request);
    }
}
