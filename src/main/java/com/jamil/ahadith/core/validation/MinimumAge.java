package com.jamil.ahadith.core.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MinimumAgeValidator.class)
public @interface MinimumAge {
    String message() default "User must be older than the minimum age";
    int value();
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
