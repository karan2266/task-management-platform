FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY . .
RUN mvn package -pl task-service -am -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/task-service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
