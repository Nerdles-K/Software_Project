# Root-level Dockerfile for Render.
# The Render service builds from the repository root, so it looks for a
# Dockerfile here. The Spring Boot app lives in backend/; paths below are
# prefixed accordingly. (backend/Dockerfile is the equivalent for a build
# whose context/root directory is backend/, e.g. local `docker build`.)

# ---- Build stage ----
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY backend/gradlew backend/settings.gradle backend/build.gradle ./
COPY backend/gradle ./gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true
COPY backend/src ./src
RUN ./gradlew bootJar -x test --no-daemon

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
# Render injects PORT; Spring reads it via application.yml (${PORT:8080}).
ENTRYPOINT ["sh", "-c", "java -jar app.jar"]
