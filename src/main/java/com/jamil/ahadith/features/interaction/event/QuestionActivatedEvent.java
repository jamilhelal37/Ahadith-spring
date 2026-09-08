package com.jamil.ahadith.features.interaction.event;

import java.util.UUID;

public record QuestionActivatedEvent(
        UUID userId,
        UUID questionId
) {
}