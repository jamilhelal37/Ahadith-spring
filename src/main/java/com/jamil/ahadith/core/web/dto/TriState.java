package com.jamil.ahadith.core.web.dto;

public final class TriState<T> {
    private static final TriState<?> UNDEFINED = new TriState<>(false, null);

    private final boolean defined;
    private final T value;

    private TriState(boolean defined, T value) {
        this.defined = defined;
        this.value = value;
    }

    @SuppressWarnings("unchecked")
    public static <T> TriState<T> undefined() {
        return (TriState<T>) UNDEFINED;
    }

    public static <T> TriState<T> defined(T value) {
        return new TriState<>(true, value);
    }

    public boolean isDefined() {
        return defined;
    }

    public T getValue() {
        return value;
    }
}
