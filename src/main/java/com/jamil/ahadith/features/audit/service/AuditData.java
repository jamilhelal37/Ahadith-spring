package com.jamil.ahadith.features.audit.service;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.lang.reflect.InvocationTargetException;
import java.time.temporal.TemporalAccessor;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class AuditData {
    private AuditData() {
    }

    public static Map<String, Object> snapshot(Object source) {
        if (source == null) {
            return Map.of();
        }
        Map<String, Object> values = new LinkedHashMap<>();
        try {
            for (var descriptor : Introspector.getBeanInfo(source.getClass(), Object.class).getPropertyDescriptors()) {
                String name = descriptor.getName();
                if (descriptor.getReadMethod() == null || isSensitive(name)) {
                    continue;
                }
                Object value = descriptor.getReadMethod().invoke(source);
                putValue(values, name, value);
            }
        } catch (IntrospectionException | IllegalAccessException | InvocationTargetException ignored) {
            return Map.of("snapshot", source.getClass().getSimpleName());
        }
        return values;
    }

    private static void putValue(Map<String, Object> values, String name, Object value) {
        if (value == null || isSimple(value)) {
            values.put(name, value);
            return;
        }
        if (value instanceof Collection<?> || value instanceof Map<?, ?> || value.getClass().isArray()) {
            return;
        }
        UUID id = extractId(value);
        if (id != null) {
            values.put(name + "Id", id.toString());
        }
    }

    private static boolean isSimple(Object value) {
        return value instanceof String
                || value instanceof Number
                || value instanceof Boolean
                || value instanceof Enum<?>
                || value instanceof UUID
                || value instanceof TemporalAccessor;
    }

    private static UUID extractId(Object value) {
        try {
            Object id = value.getClass().getMethod("getId").invoke(value);
            return id instanceof UUID uuid ? uuid : null;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            return null;
        }
    }

    private static boolean isSensitive(String name) {
        String normalized = name.toLowerCase();
        return normalized.contains("password")
                || normalized.contains("token")
                || normalized.contains("secret")
                || normalized.contains("apikey")
                || normalized.contains("api_key")
                || normalized.contains("jwt")
                || normalized.contains("credential");
    }
}
