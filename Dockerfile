# ---- Stage 1: Build ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy POMs first for dependency caching
COPY pom.xml .
COPY bddtrader-domain/pom.xml bddtrader-domain/
COPY bddtrader-app/pom.xml bddtrader-app/

# Download dependencies (cached unless POMs change)
RUN mvn dependency:go-offline -B

# Copy source and build
COPY . .
RUN mvn package -DskipTests -B

# ---- Stage 2: Run ----
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/bddtrader-app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
