package com.jamil.ahadith.features.hadith.dto.request;

import com.jamil.ahadith.features.catalog.entity.Ruling;

import com.jamil.ahadith.features.catalog.entity.Rawi;

import com.jamil.ahadith.features.hadith.entity.Hadith;

import com.jamil.ahadith.features.notification.entity.Notification;

import com.jamil.ahadith.features.catalog.entity.Book;

import com.jamil.ahadith.features.user.entity.User;

import com.jamil.ahadith.features.hadith.entity.Explaining;

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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class HadithRequestDto {
    private Hadith subValid;
    private Explaining explaining;
    private String type;
    @NotBlank(message = "Hadith text is required")
    private String text;
    private String normalText;
    private String searchText;
    @NotNull(message = "Hadith number is required")
    private Integer hadithNumber;
    private Ruling ruling;
    private Rawi rawi;
    private Book book;
    private String sanad;
}
