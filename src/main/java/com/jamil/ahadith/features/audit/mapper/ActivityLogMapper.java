package com.jamil.ahadith.features.audit.mapper;

import com.jamil.ahadith.features.audit.dto.response.ActivityLogResponseDto;
import com.jamil.ahadith.features.audit.entity.ActivityLog;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface ActivityLogMapper {

    ActivityLogResponseDto toResponseDto(ActivityLog entity);

    @AfterMapping
    default void translateActivityLog(
            ActivityLog entity,
            @MappingTarget ActivityLogResponseDto dto
    ) {
        if (entity.getTableName() == null) {
            dto.setChanges(List.of());
            return;
        }

        String tableName = entity.getTableName();

        dto.setTableName(
                translateTableName(tableName)
        );

        String operation = getOperation(entity);

        switch (operation) {

            case "UPDATE" -> {
                dto.setChanges(
                        buildChanges(
                                tableName,
                                entity.getOldData(),
                                entity.getNewData()
                        )
                );

                dto.setMessage(
                        buildUpdateMessage(
                                tableName,
                                entity.getOldData(),
                                entity.getNewData(),
                                entity.getMessage()
                        )
                );
            }

            case "CREATE" -> {
                dto.setChanges(List.of());

                dto.setMessage(
                        buildCreateMessage(
                                tableName,
                                entity.getNewData(),
                                entity.getMessage()
                        )
                );
            }

            case "DELETE" -> {
                dto.setChanges(List.of());

                dto.setMessage(
                        buildDeleteMessage(
                                tableName,
                                entity.getOldData(),
                                entity.getMessage()
                        )
                );
            }

            default -> dto.setChanges(List.of());
        }
    }

    default String translateTableName(String tableName) {
        if (tableName == null) {
            return null;
        }

        return switch (tableName) {
            case "ahadith" -> "الأحاديث";
            case "fake_ahadith" -> "الأحاديث المنتشرة التي لا تصح";
            case "books" -> "الكتب";
            case "rawis" -> "الرواة";
            case "muhaddiths" -> "المحدثون";
            case "topics" -> "المواضيع";
            case "ruling" -> "الأحكام";
            case "explaining" -> "الشروحات";
            case "similar_ahadith" -> "الأحاديث المشابهة";
            case "questions" -> "الأسئلة";
            case "notifications" -> "الإشعارات";
            case "upgrade_requests" -> "طلبات الترقية";
            case "users" -> "المستخدمون";
            default -> tableName;
        };
    }

    default String getOperation(ActivityLog entity) {

        if (entity.getNewData() != null) {
            Object operation =
                    entity.getNewData().get("operation");

            if (operation != null) {
                return String.valueOf(operation);
            }
        }

        if (entity.getOldData() != null) {
            Object operation =
                    entity.getOldData().get("operation");

            if (operation != null) {
                return String.valueOf(operation);
            }
        }

        return "";
    }

    default List<String> findChangedFields(
            Map<String, Object> oldData,
            Map<String, Object> newData
    ) {
        List<String> changedFields =
                new ArrayList<>();

        if (oldData == null || newData == null) {
            return changedFields;
        }

        Set<String> fields =
                new LinkedHashSet<>();

        fields.addAll(oldData.keySet());
        fields.addAll(newData.keySet());

        for (String field : fields) {

            if (shouldIgnoreField(field)) {
                continue;
            }

            if (isRelationShadowField(
                    field,
                    oldData,
                    newData
            )) {
                continue;
            }

            Object oldValue =
                    oldData.get(field);

            Object newValue =
                    newData.get(field);

            if (!Objects.equals(
                    oldValue,
                    newValue
            )) {
                changedFields.add(field);
            }
        }

        return changedFields;
    }

    default boolean isRelationShadowField(
            String field,
            Map<String, Object> oldData,
            Map<String, Object> newData
    ) {
        if (field.endsWith("Id")
                || field.endsWith("Display")) {
            return false;
        }

        String idField =
                field + "Id";

        String displayField =
                field + "Display";

        return oldData.containsKey(idField)
                || newData.containsKey(idField)
                || oldData.containsKey(displayField)
                || newData.containsKey(displayField);
    }

    default boolean shouldIgnoreField(String field) {

        if (field.endsWith("Display")) {
            return true;
        }

        return switch (field) {
            case "operation",
                 "table",
                 "event",
                 "id",
                 "createdAt",
                 "updatedAt",
                 "createdById",
                 "updatedById",
                 "reviewedById",
                 "reviewedAt",
                 "tokenVersion",
                 "normalText",
                 "searchText",
                 "searchVector" -> true;

            default -> false;
        };
    }

    default List<String> buildChanges(
            String tableName,
            Map<String, Object> oldData,
            Map<String, Object> newData
    ) {
        List<String> changes =
                new ArrayList<>();

        for (String field :
                findChangedFields(
                        oldData,
                        newData
                )) {

            Object oldValue =
                    oldData.get(field);

            Object newValue =
                    newData.get(field);

            if (field.endsWith("Id")) {

                String relationName =
                        field.substring(
                                0,
                                field.length() - 2
                        );

                Object oldDisplay =
                        oldData.get(
                                relationName
                                        + "Display"
                        );

                Object newDisplay =
                        newData.get(
                                relationName
                                        + "Display"
                        );

                if (oldDisplay != null) {
                    oldValue = oldDisplay;
                }

                if (newDisplay != null) {
                    newValue = newDisplay;
                }
            }

            String fieldName =
                    translateFieldName(
                            tableName,
                            field
                    );

            if (oldValue == null
                    && newValue != null) {

                changes.add(
                        "تمت إضافة "
                                + fieldName
                                + " \""
                                + formatAuditValue(
                                tableName,
                                field,
                                newValue
                        )
                                + "\""
                );

                continue;
            }

            if (oldValue != null
                    && newValue == null) {

                changes.add(
                        "تمت إزالة "
                                + fieldName
                                + " \""
                                + formatAuditValue(
                                tableName,
                                field,
                                oldValue
                        )
                                + "\""
                );

                continue;
            }

            changes.add(
                    "تم تعديل "
                            + fieldName
                            + " من \""
                            + formatAuditValue(
                            tableName,
                            field,
                            oldValue
                    )
                            + "\" إلى \""
                            + formatAuditValue(
                            tableName,
                            field,
                            newValue
                    )
                            + "\""
            );
        }

        return changes;
    }

    default String buildCreateMessage(
            String tableName,
            Map<String, Object> data,
            String fallback
    ) {
        String value =
                getDisplayValue(
                        tableName,
                        data
                );

        if (value == null) {
            return fallback;
        }

        return switch (tableName) {

            case "books" ->
                    "تمت إضافة الكتاب \""
                            + value + "\"";

            case "rawis" ->
                    "تمت إضافة الراوي \""
                            + value + "\"";

            case "muhaddiths" ->
                    "تمت إضافة المحدث \""
                            + value + "\"";

            case "topics" ->
                    "تمت إضافة الموضوع \""
                            + value + "\"";

            case "ruling" ->
                    "تمت إضافة الحكم \""
                            + value + "\"";

            case "ahadith" ->
                    "تمت إضافة الحديث \""
                            + value + "\"";

            case "fake_ahadith" ->
                    "تمت إضافة الحديث المنتشر الذي لا يصح \""
                            + value + "\"";

            case "explaining" ->
                    "تمت إضافة الشرح \""
                            + value + "\"";

            case "notifications" ->
                    "تم إنشاء الإشعار \""
                            + value + "\"";

            case "questions" ->
                    "تمت إضافة السؤال \""
                            + value + "\"";

            default -> fallback;
        };
    }

    default String buildDeleteMessage(
            String tableName,
            Map<String, Object> data,
            String fallback
    ) {
        String value =
                getDisplayValue(
                        tableName,
                        data
                );

        if (value == null) {
            return fallback;
        }

        return switch (tableName) {

            case "books" ->
                    "تم حذف الكتاب \""
                            + value + "\"";

            case "rawis" ->
                    "تم حذف الراوي \""
                            + value + "\"";

            case "muhaddiths" ->
                    "تم حذف المحدث \""
                            + value + "\"";

            case "topics" ->
                    "تم حذف الموضوع \""
                            + value + "\"";

            case "ruling" ->
                    "تم حذف الحكم \""
                            + value + "\"";

            case "ahadith" ->
                    "تم حذف الحديث \""
                            + value + "\"";

            case "fake_ahadith" ->
                    "تم حذف الحديث المنتشر الذي لا يصح \""
                            + value + "\"";

            case "explaining" ->
                    "تم حذف الشرح \""
                            + value + "\"";

            case "notifications" ->
                    "تم حذف الإشعار \""
                            + value + "\"";

            case "questions" ->
                    "تم حذف السؤال \""
                            + value + "\"";

            default -> fallback;
        };
    }

    default String buildUpdateMessage(
            String tableName,
            Map<String, Object> oldData,
            Map<String, Object> newData,
            String fallback
    ) {

        if ("users".equals(tableName)) {

            String event =
                    newData != null
                            && newData.get("event") != null
                            ? String.valueOf(
                            newData.get("event")
                    )
                            : "";

            Object userNameValue =
                    firstNonNull(
                            newData != null
                                    ? newData.get("name")
                                    : null,
                            oldData != null
                                    ? oldData.get("name")
                                    : null
                    );

            String userName =
                    userNameValue != null
                            ? formatValue(userNameValue)
                            : "المستخدم";

            if ("admin user status changed"
                    .equals(event)) {

                Object status =
                        newData != null
                                ? newData.get("status")
                                : null;

                if (status != null) {

                    return switch (
                            String.valueOf(status)
                            ) {
                        case "active" ->
                                "تم تفعيل المستخدم \""
                                        + userName
                                        + "\"";

                        case "disabled" ->
                                "تم تعطيل المستخدم \""
                                        + userName
                                        + "\"";

                        default ->
                                "تم تعديل حالة المستخدم \""
                                        + userName
                                        + "\"";
                    };
                }
            }

            if ("admin user type changed"
                    .equals(event)) {

                Object type =
                        newData != null
                                ? newData.get("type")
                                : null;

                if (type != null) {

                    String translatedType =
                            switch (
                                    String.valueOf(type)
                                    ) {
                                case "member" ->
                                        "عضو";

                                case "scholar" ->
                                        "عالم";

                                case "admin" ->
                                        "مدير";

                                default ->
                                        formatValue(type);
                            };

                    return "تم تغيير رتبة المستخدم \""
                            + userName
                            + "\" إلى \""
                            + translatedType
                            + "\"";
                }
            }

            return "تم تعديل بيانات المستخدم \""
                    + userName
                    + "\"";
        }

        if ("questions".equals(tableName)) {

            Object oldAnswer =
                    oldData != null
                            ? oldData.get("answerText")
                            : null;

            Object newAnswer =
                    newData != null
                            ? newData.get("answerText")
                            : null;

            if (!Objects.equals(
                    oldAnswer,
                    newAnswer
            )) {

                if (oldAnswer == null
                        && newAnswer != null) {

                    return "تمت الإجابة عن السؤال";
                }

                return "تم تعديل إجابة السؤال";
            }

            Object oldActive =
                    oldData != null
                            ? oldData.get("isActive")
                            : null;

            Object newActive =
                    newData != null
                            ? newData.get("isActive")
                            : null;

            if (!Objects.equals(
                    oldActive,
                    newActive
            )) {

                if (Boolean.parseBoolean(
                        String.valueOf(newActive)
                )) {
                    return "تم تفعيل السؤال";
                }

                return "تم تعطيل السؤال";
            }

            return "تم تعديل بيانات السؤال";
        }

        if ("upgrade_requests"
                .equals(tableName)) {

            Object status =
                    newData != null
                            ? newData.get("status")
                            : null;

            if (status != null) {

                return switch (
                        String.valueOf(status)
                        ) {
                    case "approved" ->
                            "تم قبول طلب الترقية";

                    case "rejected" ->
                            "تم رفض طلب الترقية";

                    case "under_review" ->
                            "تم وضع طلب الترقية قيد المراجعة";

                    case "pending_documents" ->
                            "تم تحويل طلب الترقية إلى انتظار المستندات";

                    default ->
                            "تمت مراجعة طلب الترقية";
                };
            }

            return "تمت مراجعة طلب الترقية";
        }

        return switch (tableName) {

            case "ahadith" ->
                    "تم تعديل بيانات الحديث";

            case "fake_ahadith" ->
                    "تم تعديل بيانات الحديث المنتشر الذي لا يصح";

            case "books" ->
                    "تم تعديل بيانات الكتاب";

            case "rawis" ->
                    "تم تعديل بيانات الراوي";

            case "muhaddiths" ->
                    "تم تعديل بيانات المحدث";

            case "topics" ->
                    "تم تعديل بيانات الموضوع";

            case "ruling" ->
                    "تم تعديل الحكم";

            case "explaining" ->
                    "تم تعديل الشرح";

            case "similar_ahadith" ->
                    "تم تعديل الحديث المشابه";

            case "notifications" ->
                    "تم تعديل بيانات الإشعار";

            default -> fallback;
        };
    }

    default String getDisplayValue(
            String tableName,
            Map<String, Object> data
    ) {
        if (data == null) {
            return null;
        }

        Object value =
                switch (tableName) {

                    case "books",
                         "rawis",
                         "muhaddiths" ->
                            firstNonNull(
                                    data.get("name")
                            );

                    case "topics" ->
                            firstNonNull(
                                    data.get("name"),
                                    data.get("title")
                            );

                    case "ruling" ->
                            firstNonNull(
                                    data.get("name"),
                                    data.get("ruling")
                            );

                    case "ahadith",
                         "fake_ahadith",
                         "explaining" ->
                            firstNonNull(
                                    data.get("text")
                            );

                    case "notifications" ->
                            firstNonNull(
                                    data.get("title"),
                                    data.get("body")
                            );

                    case "questions" ->
                            firstNonNull(
                                    data.get("askerText"),
                                    data.get("text"),
                                    data.get("questionText"),
                                    data.get("question")
                            );

                    default -> null;
                };

        if (value == null) {
            return null;
        }

        return formatValue(value);
    }

    default Object firstNonNull(
            Object... values
    ) {
        for (Object value : values) {
            if (value != null) {
                return value;
            }
        }

        return null;
    }

    default String translateFieldName(
            String tableName,
            String field
    ) {
        return switch (field) {

            case "name" ->
                    switch (tableName) {
                        case "books" ->
                                "اسم الكتاب";

                        case "rawis" ->
                                "اسم الراوي";

                        case "muhaddiths" ->
                                "اسم المحدث";

                        case "topics" ->
                                "اسم الموضوع";

                        case "ruling" ->
                                "اسم الحكم";

                        case "users" ->
                                "اسم المستخدم";

                        default ->
                                "الاسم";
                    };

            case "text" ->
                    switch (tableName) {
                        case "ahadith" ->
                                "نص الحديث";

                        case "fake_ahadith" ->
                                "نص الحديث المنتشر الذي لا يصح";

                        case "explaining" ->
                                "نص الشرح";

                        case "questions" ->
                                "نص السؤال";

                        default ->
                                "النص";
                    };

            case "about" ->
                    switch (tableName) {
                        case "rawis" ->
                                "نبذة الراوي";

                        case "muhaddiths" ->
                                "نبذة المحدث";

                        default ->
                                "النبذة";
                    };

            case "gender" ->
                    "الجنس";

            case "description" ->
                    "الوصف";

            case "askerText" ->
                    "نص السؤال";

            case "answerText" ->
                    "الإجابة";

            case "questionText" ->
                    "نص السؤال";

            case "title" ->
                    "العنوان";

            case "body" ->
                    "محتوى الإشعار";

            case "status" ->
                    switch (tableName) {
                        case "users" ->
                                "حالة المستخدم";

                        case "upgrade_requests" ->
                                "حالة طلب الترقية";

                        default ->
                                "الحالة";
                    };

            case "type" ->
                    switch (tableName) {
                        case "ahadith" ->
                                "نوع الحديث";

                        case "users" ->
                                "رتبة المستخدم";

                        default ->
                                "النوع";
                    };

            case "isActive" ->
                    "questions".equals(tableName)
                            ? "حالة السؤال"
                            : "حالة التفعيل";

            case "notes" ->
                    "الملاحظات";

            case "reviewNotes" ->
                    "ملاحظات المراجعة";

            case "rejectionReason" ->
                    "سبب الرفض";

            case "hadithNumber" ->
                    "رقم الحديث";

            case "sanad" ->
                    "السند";

            case "birthDate" ->
                    "تاريخ الميلاد";

            case "email" ->
                    "البريد الإلكتروني";

            case "bookId" ->
                    "الكتاب";

            case "rawiId" ->
                    "الراوي";

            case "muhaddithId" ->
                    "المحدث";

            case "rulingId" ->
                    "الحكم";

            case "explainingId" ->
                    "الشرح";

            case "hadithId" ->
                    "الحديث";

            case "subValidId" ->
                    "الحديث المرتبط";

            case "askerId" ->
                    "صاحب السؤال";

            case "userId" ->
                    "المستخدم";

            default -> field;
        };
    }

    default String formatAuditValue(
            String tableName,
            String field,
            Object value
    ) {
        if (value == null) {
            return "فارغ";
        }

        String text =
                String.valueOf(value);

        if ("gender".equals(field)) {

            return switch (text) {
                case "male" ->
                        "ذكر";

                case "female" ->
                        "أنثى";

                default ->
                        formatValue(value);
            };
        }

        if ("type".equals(field)
                && "ahadith".equals(tableName)) {

            return switch (text) {
                case "marfu" ->
                        "مرفوع";

                case "mawquf" ->
                        "موقوف";

                case "qudsi" ->
                        "قدسي";

                case "atharSahaba" ->
                        "أثر صحابي";

                default ->
                        formatValue(value);
            };
        }

        if ("status".equals(field)
                && "users".equals(tableName)) {

            return switch (text) {
                case "pending_confirmation" ->
                        "بانتظار تأكيد البريد";

                case "active" ->
                        "نشط";

                case "disabled" ->
                        "معطل";

                default ->
                        formatValue(value);
            };
        }

        if ("status".equals(field)
                && "upgrade_requests"
                .equals(tableName)) {

            return switch (text) {
                case "pending_documents" ->
                        "بانتظار المستندات";

                case "under_review" ->
                        "قيد المراجعة";

                case "approved" ->
                        "مقبول";

                case "rejected" ->
                        "مرفوض";

                default ->
                        formatValue(value);
            };
        }

        if ("type".equals(field)
                && "users".equals(tableName)) {

            return switch (text) {
                case "member" ->
                        "عضو";

                case "scholar" ->
                        "عالم";

                case "admin" ->
                        "مدير";

                default ->
                        formatValue(value);
            };
        }

        if ("isActive".equals(field)) {

            return Boolean.parseBoolean(text)
                    ? "فعال"
                    : "غير فعال";
        }

        return formatValue(value);
    }

    default String formatValue(Object value) {

        if (value == null) {
            return "فارغ";
        }

        String text =
                String.valueOf(value).trim();

        if (text.length() > 120) {
            return text.substring(
                    0,
                    120
            ) + "...";
        }

        return text;
    }
}