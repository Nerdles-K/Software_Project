# ---- Build stage ----
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
# Warm the dependency cache (best-effort; ignore failure if no offline cache).
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true
COPY src ./src
RUN ./gradlew bootJar -x test --no-daemon

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
# Railway/Fly inject PORT; Spring reads it via application.yml (${PORT:8080}).
ENTRYPOINT ["sh", "-c", "java -jar app.jar"]
