package com.jamil.ahadith.features.hadith.dto.response;

import com.jamil.ahadith.features.catalog.entity.Ruling;

import com.jamil.ahadith.features.catalog.entity.Rawi;

import com.jamil.ahadith.features.hadith.entity.Hadith;

import com.jamil.ahadith.features.notification.entity.Notification;

import com.jamil.ahadith.features.catalog.entity.Book;

import com.jamil.ahadith.features.user.entity.User;

import com.jamil.ahadith.features.hadith.entity.Explaining;

import com.jamil.ahadith.core.web.dto.AdminUserReferenceDto;

import com.jamil.ahadith.features.account.entity.*;
import com.jamil.ahadith.features.audit.entity.*;
import com.jamil.ahadith.features.auth.entity.*;
import com.jamil.ahadith.features.catalog.entity.*;
import com.jamil.ahadith.features.hadith.entity.*;
import com.jamil.ahadith.features.interaction.entity.*;
import com.jamil.ahadith.features.notification.entity.*;
import com.jamil.ahadith.features.search.entity.*;
import com.jamil.ahadith.features.upgrade.entity.*;
import com.jamil.ahadith.features.user.entity.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class HadithResponseDto {
    private UUID id;
    private Hadith subValid;
    private Explaining explaining;
    private String type;
    private String text;
    private String normalText;
    private String searchText;
    private Integer hadithNumber;
    private Ruling ruling;
    private Rawi rawi;
    private Book book;
    private String sanad;
    private AdminUserReferenceDto createdBy;
    private AdminUserReferenceDto updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
