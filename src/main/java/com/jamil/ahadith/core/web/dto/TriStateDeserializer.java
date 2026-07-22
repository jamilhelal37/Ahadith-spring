package com.jamil.ahadith.core.web.dto;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.util.AccessPattern;

public class TriStateDeserializer extends StdDeserializer<TriState<?>> {
    private final JavaType valueType;

    public TriStateDeserializer() {
        super(TriState.class);
        this.valueType = null;
    }

    private TriStateDeserializer(JavaType valueType) {
        super(TriState.class);
        this.valueType = valueType;
    }

    @Override
    public ValueDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        if (property == null) {
            return this;
        }
        return new TriStateDeserializer(property.getType().containedTypeOrUnknown(0));
    }

    @Override
    public TriState<?> deserialize(JsonParser parser, DeserializationContext ctxt) throws JacksonException {
        JavaType targetType = valueType == null ? ctxt.constructType(Object.class) : valueType;
        return TriState.defined(ctxt.readValue(parser, targetType));
    }

    @Override
    public Object getNullValue(DeserializationContext ctxt) {
        return TriState.defined(null);
    }

    @Override
    public AccessPattern getNullAccessPattern() {
        return AccessPattern.DYNAMIC;
    }
}
