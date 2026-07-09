package com.jamil.ahadith.dtos.responses;

import lombok.Data;

import java.util.UUID;

@Data
public class HadithDto {
    private UUID id;
    private String text;
    private Integer hadithNumber;
    private String bookName;
    private String rawiName;
    private String rulingName;
    private String explainingText;
}
