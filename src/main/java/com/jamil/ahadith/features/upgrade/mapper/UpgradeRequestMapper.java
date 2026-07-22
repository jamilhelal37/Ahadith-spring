package com.jamil.ahadith.features.upgrade.mapper;

import com.jamil.ahadith.core.web.mapper.AuditMapping;

import com.jamil.ahadith.features.upgrade.dto.response.AdminUpgradeRequestResponseDto;
import com.jamil.ahadith.features.upgrade.dto.response.MemberUpgradeRequestResponseDto;
import com.jamil.ahadith.features.upgrade.entity.UpgradeRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UpgradeRequestMapper extends AuditMapping {
    @Mapping(target = "documentAvailable", expression = "java(entity.getDocumentPublicId() != null && !entity.getDocumentPublicId().isBlank())")
    MemberUpgradeRequestResponseDto toMemberResponseDto(UpgradeRequest entity);

    @Mapping(target = "user", source = "user")
    @Mapping(target = "reviewedBy", source = "reviewedBy")
    @Mapping(target = "documentAvailable", expression = "java(entity.getDocumentPublicId() != null && !entity.getDocumentPublicId().isBlank())")
    AdminUpgradeRequestResponseDto toAdminResponseDto(UpgradeRequest entity);
}
