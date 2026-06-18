FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY . .
RUN mvn package -pl auth-service -am

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/auth-service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
