# Use Eclipse Temurin JDK 17 (official replacement for OpenJDK)
FROM eclipse-temurin:17-jdk-alpine

# Install Maven
RUN apk update && \
    apk add --no-cache maven && \
    rm -rf /var/cache/apk/*

# Set working directory
WORKDIR /app

# Copy pom.xml first for better layer caching
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Expose port
EXPOSE 8080

# Run the application
CMD ["java", "-Dserver.port=${PORT:-8080}", "-jar", "target/flash-sale-engine-1.0.0.jar"]
