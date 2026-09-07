package com.jamil.ahadith.features.audit.repository;

import com.jamil.ahadith.features.audit.entity.ActivityLog;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

public final class ActivityLogSpecifications {
    private ActivityLogSpecifications() {
    }

    public static Specification<ActivityLog> search(UUID actorUserId, String tableName, String message) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new ArrayList<Predicate>();

            if (actorUserId != null) {
                predicates.add(criteriaBuilder.equal(root.get("actorUserId"), actorUserId));
            }
            if (tableName != null) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("tableName")),
                        tableName.toLowerCase(Locale.ROOT)
                ));
            }
            if (message != null) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("message")),
                        "%" + message.toLowerCase(Locale.ROOT) + "%"
                ));
            }

            return predicates.isEmpty()
                    ? null
                    : criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
