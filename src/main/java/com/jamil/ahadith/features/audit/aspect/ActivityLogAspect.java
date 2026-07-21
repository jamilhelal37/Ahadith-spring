package com.jamil.ahadith.features.audit.aspect;

import com.jamil.ahadith.features.audit.entity.ActivityLog;
import com.jamil.ahadith.features.audit.repository.ActivityLogRepository;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Aspect
@Component
@AllArgsConstructor
public class ActivityLogAspect {
    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

    @Pointcut("execution(* com.jamil.ahadith.features..service..*(..))")
    public void serviceLayerMethods() {
    }

    @AfterReturning(pointcut = "serviceLayerMethods()", returning = "result")
    public void logServiceCall(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        if (!isTrackedMethod(methodName)) {
            return;
        }

        String tableName = inferTableName(joinPoint.getTarget().getClass().getSimpleName(), methodName);
        UUID recordId = extractRecordId(result);
        Map<String, Object> oldData = Map.of();
        Map<String, Object> newData = buildNewData(joinPoint, result, methodName, tableName);

        Optional<User> actorOpt = getCurrentActor();
        if (actorOpt.isEmpty()) {
            return;
        }

        User actor = actorOpt.get();
        ActivityLog log = new ActivityLog();
        log.setActorUserId(actor.getId());
        log.setActorName(actor.getName());
        log.setActorEmail(actor.getEmail());
        log.setActorAvatarUrl(actor.getAvatarUrl());
        log.setMessage(buildMessage(methodName, tableName));
        log.setTableName(tableName);
        log.setRecordId(recordId);
        log.setOldData(oldData);
        log.setNewData(newData);
        log.setCreatedAt(LocalDateTime.now());
        activityLogRepository.save(log);
    }

    private boolean isTrackedMethod(String methodName) {
        return methodName.startsWith("create")
                || methodName.startsWith("update")
                || methodName.startsWith("delete");
    }

    private String inferTableName(String serviceClassName, String methodName) {
        String base = serviceClassName.replace("Service", "").toLowerCase();
        return switch (base) {
            case "topic" -> "topics";
            case "hadith" -> "ahadith";
            case "book" -> "books";
            case "muhaddith" -> "muhaddiths";
            case "rawi" -> "rawis";
            case "ruling" -> "ruling";
            case "question" -> "questions";
            case "notification" -> "notifications";
            case "similarahadith" -> "similar_ahadith";
            case "upgraderequest" -> "upgrade_requests";
            default -> base;
        };
    }

    private UUID extractRecordId(Object result) {
        if (result instanceof UUID uuid) {
            return uuid;
        }
        if (result instanceof com.jamil.ahadith.features.catalog.dto.response.TopicResponseDto responseDto) {
            return responseDto.getId();
        }
        if (result instanceof com.jamil.ahadith.features.hadith.dto.response.HadithResponseDto responseDto) {
            return responseDto.getId();
        }
        if (result instanceof com.jamil.ahadith.features.catalog.dto.response.BookResponseDto responseDto) {
            return responseDto.getId();
        }
        if (result instanceof com.jamil.ahadith.features.catalog.dto.response.MuhaddithResponseDto responseDto) {
            return responseDto.getId();
        }
        if (result instanceof com.jamil.ahadith.features.catalog.dto.response.RawiResponseDto responseDto) {
            return responseDto.getId();
        }
        if (result instanceof com.jamil.ahadith.features.catalog.dto.response.RulingResponseDto responseDto) {
            return responseDto.getId();
        }
        if (result instanceof com.jamil.ahadith.features.interaction.dto.response.MemberQuestionResponseDto responseDto) {
            return responseDto.getId();
        }
        if (result instanceof com.jamil.ahadith.features.interaction.dto.response.ScholarQuestionResponseDto responseDto) {
            return responseDto.getId();
        }
        if (result instanceof com.jamil.ahadith.features.notification.dto.response.NotificationResponseDto responseDto) {
            return responseDto.getId();
        }
        if (result instanceof com.jamil.ahadith.features.hadith.dto.response.SimilarAhadithResponseDto responseDto) {
            return responseDto.getId();
        }
        if (result instanceof com.jamil.ahadith.features.upgrade.dto.response.UpgradeRequestResponseDto responseDto) {
            return responseDto.getId();
        }
        return null;
    }

    private Map<String, Object> buildNewData(JoinPoint joinPoint, Object result, String methodName, String tableName) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("operation", methodName.startsWith("create") ? "create" : methodName.startsWith("update") ? "update" : "delete");
        data.put("service", joinPoint.getTarget().getClass().getSimpleName());
        data.put("table", tableName);
        data.put("resultType", result != null ? result.getClass().getSimpleName() : "void");
        data.put("recordId", extractRecordId(result));
        return data;
    }

    private Optional<User> getCurrentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        String principal = authentication.getName();
        return userRepository.findByEmail(principal);
    }

    private String buildMessage(String methodName, String tableName) {
        return switch (methodName) {
            case "createTopic", "createHadith", "createBook", "createMuhaddith", "createRawi", "createRuling", "createQuestion", "createNotification", "createSimilarAhadith", "createUpgradeRequest" -> "created " + tableName;
            case "updateTopic", "updateHadith", "updateBook", "updateMuhaddith", "updateRawi", "updateRuling", "answerQuestion", "updateQuestionStatus", "updateNotification", "updateSimilarAhadith", "updateUpgradeRequest" -> "updated " + tableName;
            case "deleteTopic", "deleteHadith", "deleteBook", "deleteMuhaddith", "deleteRawi", "deleteRuling", "deleteQuestion", "deleteNotification", "deleteSimilarAhadith", "deleteUpgradeRequest" -> "deleted " + tableName;
            default -> "performed action on " + tableName;
        };
    }
}
