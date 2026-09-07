package com.jamil.ahadith.features.audit.service;

import com.jamil.ahadith.features.audit.event.AuditActorSnapshot;
import com.jamil.ahadith.features.audit.event.AuditEvent;
import com.jamil.ahadith.features.audit.event.AuditOperation;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final CurrentUserService currentUserService;

    public void publishCreate(
            String tableName,
            UUID recordId,
            Map<String, Object> newData
    ) {
        publish(
                AuditOperation.CREATE,
                tableName,
                recordId,
                Map.of(),
                newData,
                buildMessage(AuditOperation.CREATE, tableName)
        );
    }

    public void publishUpdate(
            String tableName,
            UUID recordId,
            Map<String, Object> oldData,
            Map<String, Object> newData
    ) {
        publish(
                AuditOperation.UPDATE,
                tableName,
                recordId,
                oldData,
                newData,
                buildMessage(AuditOperation.UPDATE, tableName)
        );
    }

    public void publishDelete(
            String tableName,
            UUID recordId,
            Map<String, Object> oldData
    ) {
        publish(
                AuditOperation.DELETE,
                tableName,
                recordId,
                oldData,
                Map.of(),
                buildMessage(AuditOperation.DELETE, tableName)
        );
    }

    public void publishUpdateAs(
            User actor,
            String tableName,
            UUID recordId,
            Map<String, Object> oldData,
            Map<String, Object> newData,
            String message
    ) {
        publish(
                new AuditEvent(
                        AuditOperation.UPDATE,
                        tableName,
                        recordId,
                        snapshot(actor),
                        safe(oldData),
                        safe(newData),
                        message
                )
        );
    }

    private void publish(
            AuditOperation operation,
            String tableName,
            UUID recordId,
            Map<String, Object> oldData,
            Map<String, Object> newData,
            String message
    ) {
        currentUserService.getCurrentUser()
                .map(this::snapshot)
                .ifPresent(actor ->
                        publish(
                                new AuditEvent(
                                        operation,
                                        tableName,
                                        recordId,
                                        actor,
                                        safe(oldData),
                                        safe(newData),
                                        message
                                )
                        )
                );
    }

    private void publish(AuditEvent event) {
        applicationEventPublisher.publishEvent(event);
    }

    private AuditActorSnapshot snapshot(User user) {
        return new AuditActorSnapshot(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAvatarUrl()
        );
    }

    private Map<String, Object> safe(
            Map<String, Object> value
    ) {
        return value == null
                ? Map.of()
                : value;
    }

    private String buildMessage(
            AuditOperation operation,
            String tableName
    ) {
        return switch (tableName) {

            case "ahadith" -> switch (operation) {
                case CREATE -> "تمت إضافة حديث نبوي جديد إلى الموسوعة";
                case UPDATE -> "تم تعديل بيانات حديث نبوي";
                case DELETE -> "تم حذف حديث نبوي من الموسوعة";
            };

            case "fake_ahadith" -> switch (operation) {
                case CREATE -> "تمت إضافة حديث منتشر لا يصح";
                case UPDATE -> "تم تعديل بيانات حديث منتشر لا يصح";
                case DELETE -> "تم حذف حديث منتشر لا يصح";
            };

            case "books" -> switch (operation) {
                case CREATE -> "تمت إضافة كتاب جديد إلى الموسوعة";
                case UPDATE -> "تم تعديل بيانات كتاب";
                case DELETE -> "تم حذف كتاب من الموسوعة";
            };

            case "rawis" -> switch (operation) {
                case CREATE -> "تمت إضافة راوٍ جديد إلى الموسوعة";
                case UPDATE -> "تم تعديل بيانات راوٍ";
                case DELETE -> "تم حذف راوٍ من الموسوعة";
            };

            case "muhaddiths" -> switch (operation) {
                case CREATE -> "تمت إضافة محدّث جديد إلى الموسوعة";
                case UPDATE -> "تم تعديل بيانات محدّث";
                case DELETE -> "تم حذف محدّث من الموسوعة";
            };

            case "topics" -> switch (operation) {
                case CREATE -> "تمت إضافة موضوع جديد إلى الموسوعة";
                case UPDATE -> "تم تعديل بيانات موضوع";
                case DELETE -> "تم حذف موضوع من الموسوعة";
            };

            case "ruling" -> switch (operation) {
                case CREATE -> "تمت إضافة حكم جديد على حديث";
                case UPDATE -> "تم تعديل حكم على حديث";
                case DELETE -> "تم حذف حكم من أحكام الأحاديث";
            };

            case "explaining" -> switch (operation) {
                case CREATE -> "تمت إضافة شرح لحديث";
                case UPDATE -> "تم تعديل شرح حديث";
                case DELETE -> "تم حذف شرح حديث";
            };

            case "similar_ahadith" -> switch (operation) {
                case CREATE -> "تمت إضافة حديث مشابه";
                case UPDATE -> "تم تعديل بيانات حديث مشابه";
                case DELETE -> "تم حذف حديث مشابه";
            };

            case "questions" -> switch (operation) {
                case CREATE -> "تم طرح سؤال جديد";
                case UPDATE -> "تم تعديل بيانات سؤال";
                case DELETE -> "تم حذف سؤال";
            };

            case "notifications" -> switch (operation) {
                case CREATE -> "تم إنشاء إشعار جديد";
                case UPDATE -> "تم تعديل بيانات إشعار";
                case DELETE -> "تم حذف إشعار";
            };

            case "upgrade_requests" -> switch (operation) {
                case CREATE -> "تم تقديم طلب ترقية إلى رتبة عالم";
                case UPDATE -> "تمت مراجعة طلب ترقية إلى رتبة عالم";
                case DELETE -> "تم حذف طلب ترقية";
            };

            case "users" -> switch (operation) {
                case CREATE -> "تم إنشاء حساب مستخدم جديد";
                case UPDATE -> "تم تحديث بيانات مستخدم";
                case DELETE -> "تم حذف حساب مستخدم";
            };

            default -> switch (operation) {
                case CREATE -> "تمت إضافة سجل جديد";
                case UPDATE -> "تم تعديل بيانات سجل";
                case DELETE -> "تم حذف سجل";
            };
        };
    }
}