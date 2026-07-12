# syntax=docker/dockerfile:1.6
# ---------- Build stage ----------
# JDK 21 (NO 25): Board usa Spring Boot 3.2.4 que fija Lombok 1.18.30, el cual
# no soporta JDK 25 (el processor no genera getters/builders y la compilacion
# falla con "cannot find symbol"). El CI (setup-java) tambien usa Temurin 21.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Cache deps first
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B -q dependency:go-offline

# Now the source
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -q -DskipTests package

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S board && adduser -S board -G board

COPY --from=build /workspace/target/*.jar app.jar
RUN chown board:board app.jar

USER board

EXPOSE 8086

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
