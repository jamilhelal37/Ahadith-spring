package com.jamil.ahadith.mappers;

import com.jamil.ahadith.dtos.responses.AdminUserReferenceDto;
import com.jamil.ahadith.entities.User;

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
