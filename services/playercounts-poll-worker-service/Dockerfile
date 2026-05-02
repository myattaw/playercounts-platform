FROM maven:3.9.7-eclipse-temurin-21 AS build
LABEL authors="Michael"

WORKDIR /app
COPY . .

RUN mvn clean package -DskipTests -pl services/playercounts-poll-worker-service -am

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/services/playercounts-poll-worker-service/target/playercounts-poll-worker-service-1.0-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
