# ---- Stage 1: Dependency cache ----
FROM maven:3.9-eclipse-temurin-17 AS deps
WORKDIR /app

# Copy POM first for dependency caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# ---- Stage 2: Build & test ----
FROM maven:3.9-eclipse-temurin-17
WORKDIR /app

# Reuse cached dependencies
COPY --from=deps /root/.m2 /root/.m2
COPY pom.xml .
COPY src ./src
COPY serenity.properties ./

# Run Serenity acceptance tests
ENTRYPOINT ["mvn", "verify"]