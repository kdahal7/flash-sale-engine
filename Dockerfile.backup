FROM eclipse-temurin:17-jre-alpine

# Set working directory
WORKDIR /app

# Copy the built JAR file
COPY target/flash-sale-engine-*.jar app.jar

# Expose port
EXPOSE 8080

# Set environment variables with defaults
ENV JAVA_OPTS="-Xms512m -Xmx1024m"
ENV DATABASE_URL="jdbc:postgresql://postgres:5432/flashsale"
ENV DATABASE_USER="postgres"
ENV DATABASE_PASSWORD="postgres"
ENV REDIS_HOST="redis"
ENV REDIS_PORT="6379"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
