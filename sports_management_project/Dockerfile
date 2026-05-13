# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Cache deps
COPY pom.xml .
RUN mvn -q -B -e -DskipTests dependency:go-offline

# Copy sources and build
COPY src ./src
RUN mvn -q -B -DskipTests package

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jdk
WORKDIR /app

# Copy the fat/boot jar from the build stage
COPY --from=build /workspace/target/*.jar /app/app.jar

# Render will inject PORT; your app already has server.port=${PORT:8091}
EXPOSE 8091

# (Optional) print effective datasource envs for one deploy to verify
# RUN /bin/sh -c 'env | sort | grep -E "DB_|SPRING_DATASOURCE_" || true'

ENTRYPOINT ["java","-jar","/app/app.jar"]