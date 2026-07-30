package com.jamil.ahadith.core;

import jakarta.persistence.Entity;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class DtoEntityFieldArchitectureTest {

    @Test
    void dtoFieldsShouldNotExposeJpaEntities() throws Exception {
        List<String> violations = new ArrayList<>();

        for (Class<?> dtoClass : dtoClasses()) {
            for (Field field : dtoClass.getDeclaredFields()) {
                collectEntityFieldViolations(dtoClass, field.getGenericType(), field.getName(), violations);
            }
        }

        assertThat(violations).isEmpty();
    }

    private List<Class<?>> dtoClasses() throws IOException, ClassNotFoundException {
        Path classesRoot = Path.of("target/classes");
        Path packageRoot = classesRoot.resolve("com/jamil/ahadith");
        try (Stream<Path> paths = Files.walk(packageRoot)) {
            return paths
                    .filter(path -> path.toString().endsWith("Dto.class"))
                    .map(classesRoot::relativize)
                    .map(path -> path.toString()
                            .replace('\\', '.')
                            .replace('/', '.')
                            .replaceAll("\\.class$", ""))
                    .map(this::loadClass)
                    .collect(Collectors.toList());
        }
    }

    private Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private void collectEntityFieldViolations(
            Class<?> dtoClass,
            Type type,
            String fieldName,
            List<String> violations) {
        if (type instanceof Class<?> fieldClass) {
            if (fieldClass.isAnnotationPresent(Entity.class)) {
                violations.add(dtoClass.getName() + "." + fieldName + " -> " + fieldClass.getName());
            }
            return;
        }

        if (type instanceof ParameterizedType parameterizedType) {
            for (Type argument : parameterizedType.getActualTypeArguments()) {
                collectEntityFieldViolations(dtoClass, argument, fieldName, violations);
            }
        }
    }
}
