package com.jamil.ahadith.core.validation;

public final class ValidationLimits {
    public static final int EMAIL_MAX = 254;
    public static final int NAME_MAX = 200;
    public static final int PASSWORD_MIN = 8;
    public static final int PASSWORD_MAX = 128;
    public static final int URL_MAX = 2048;
    public static final int QUERY_MAX = 500;
    public static final int FILTER_LIST_MAX = 100;
    public static final int HADITH_TEXT_MAX = 10_000;
    public static final int SANAD_MAX = 4_000;
    public static final int EXPLANATION_TEXT_MAX = 20_000;
    public static final int COMMENT_MAX = 4_000;
    public static final int QUESTION_MAX = 8_000;
    public static final int ANSWER_MAX = 8_000;
    public static final int NOTIFICATION_TITLE_MAX = 200;
    public static final int NOTIFICATION_BODY_MAX = 2_000;
    public static final int NOTES_MAX = 2_000;
    public static final int TOKEN_MAX = 2_048;

    private ValidationLimits() {
    }
}
