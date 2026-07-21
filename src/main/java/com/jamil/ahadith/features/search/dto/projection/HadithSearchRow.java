package com.jamil.ahadith.features.search.dto.projection;

import java.util.UUID;

public interface HadithSearchRow {
    UUID getId();

    String getText();

    String getNormalText();

    Integer getHadithNumber();

    String getType();

    String getSanad();

    UUID getBookId();

    String getBookName();

    UUID getRawiId();

    String getRawiName();

    UUID getRulingId();

    String getRulingName();

    UUID getMuhaddithId();

    String getMuhaddithName();

    UUID getExplanationId();

    String getExplanationText();

    String getExplanationNormalText();

    UUID getSubValidId();
}
