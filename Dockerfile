# Build stage
FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
ARG SPRING_PROFILES_ACTIVE=dev
RUN mvn clean package -DskipTests -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd -m appuser
USER appuser
COPY --from=build /app/target/digitalWallet-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "-jar", "app.jar"]