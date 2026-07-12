FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./

RUN chmod +x mvnw
RUN ./mvnw --batch-mode dependency:go-offline

COPY src src

# CI runs ./mvnw --batch-mode verify before the Docker build job; this stage only packages the verified source.
RUN ./mvnw --batch-mode clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S app && adduser -S app -G app

WORKDIR /app

COPY --from=builder --chown=app:app /workspace/target/ahadith-*.jar /app/app.jar

USER app

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget -qO- "http://127.0.0.1:${PORT:-8080}/actuator/health/readiness" >/dev/null || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
