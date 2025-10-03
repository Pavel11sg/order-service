FROM maven:3.9.11-eclipse-temurin-21-alpine AS build

# Now these paths are relative to parent directory
COPY shared-kafka /shared-kafka
WORKDIR /shared-kafka

# Install shared-kafka to local maven repository
RUN mvn clean install -DskipTests

# Copy order-service
WORKDIR /app
COPY order-service/pom.xml .
COPY order-service/src ./src

# Build order-service
RUN mvn package -DskipTests

# Stage 2: Runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar order-service.jar

EXPOSE 8083
ENTRYPOINT ["java", "-jar", "order-service.jar"]