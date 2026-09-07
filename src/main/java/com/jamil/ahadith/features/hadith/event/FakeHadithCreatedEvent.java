package com.jamil.ahadith.features.hadith.event;

import java.util.UUID;

public record FakeHadithCreatedEvent(
        UUID fakeHadithId,
        String text
) {
}