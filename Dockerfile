# ===== Stage 1: Build the JAR =====
FROM maven:3.9.2-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .

COPY extractpdf4j-core ./extractpdf4j-core
COPY extractpdf4j-cli ./extractpdf4j-cli
COPY extractpdf4j-service ./extractpdf4j-service

RUN mvn clean package -DskipTests

# ===== Stage 2: Final Distroless image =====
FROM gcr.io/distroless/java17-debian11
WORKDIR /app
# =====COPY --from=build /app/target/extractpdf4j-parser-0.1.1.jar ./app.jar=====
COPY --from=build /app/extractpdf4j-service/target/*.jar ./app.jar
ENTRYPOINT ["java","-jar","app.jar"]
