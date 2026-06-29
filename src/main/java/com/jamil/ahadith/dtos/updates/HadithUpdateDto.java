package com.jamil.ahadith.dtos.updates;

import com.jamil.ahadith.entities.*;
import lombok.Data;

@Data
public class HadithUpdateDto {
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
}
