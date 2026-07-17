package com.jamil.ahadith.core.web.mapper;

import com.jamil.ahadith.core.web.dto.AdminUserReferenceDto;
import com.jamil.ahadith.features.user.entity.User;

public interface AuditMapping {
    default AdminUserReferenceDto toAdminUserReferenceDto(User user) {
        if (user == null) {
            return null;
        }
        return new AdminUserReferenceDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getType() == null ? null : user.getType().name()
        );
    }
}
