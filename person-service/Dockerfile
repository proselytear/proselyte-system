# syntax=docker/dockerfile:1
FROM gradle:8.14.2-jdk24 AS build
ARG NEXUS_URL=http://host.docker.internal:8081/repository/maven-snapshots
ARG NEXUS_USERNAME=admin
ARG NEXUS_PASSWORD=admin

ENV NEXUS_URL=${NEXUS_URL}
ENV NEXUS_USERNAME=${NEXUS_USERNAME}
ENV NEXUS_PASSWORD=${NEXUS_PASSWORD}

WORKDIR /home/gradle/project

# Copy only dependency definition files first (for Docker layer caching)
COPY build.gradle.kts settings.gradle.kts gradle.properties ./

# Download dependencies (this layer will be cached if build files don't change)
RUN --mount=type=cache,target=/home/gradle/.gradle/caches \
    --mount=type=cache,target=/home/gradle/.gradle/wrapper \
    gradle --no-daemon dependencies --refresh-dependencies || true

# Now copy source code
COPY src ./src
COPY openapi ./openapi

# Build without clean (incremental build)
RUN --mount=type=cache,target=/home/gradle/.gradle/caches \
    --mount=type=cache,target=/home/gradle/.gradle/wrapper \
    gradle --no-daemon build -x test --build-cache

FROM eclipse-temurin:24-jre

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=build /home/gradle/project/build/libs/*.jar app.jar

EXPOSE 8092
CMD ["java", "-jar", "app.jar"]
