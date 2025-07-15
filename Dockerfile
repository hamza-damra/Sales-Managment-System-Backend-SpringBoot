# Multi-stage build for Spring Boot Sales Management System
# Optimized for Render.com deployment
# Stage 1: Build the application
FROM openjdk:17-jdk-slim AS build

# Install Maven and necessary tools
RUN apt-get update && apt-get install -y \
    maven \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy Maven configuration files first for better layer caching
COPY pom.xml .
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn

# Make mvnw executable
RUN chmod +x mvnw

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (skip tests for faster builds)
RUN mvn clean package -DskipTests -B

# Stage 2: Create the runtime image optimized for Render.com
FROM openjdk:17-jre-slim

# Install necessary packages and create app user for security
RUN apt-get update && apt-get install -y \
    curl \
    dumb-init \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd -r appuser && useradd -r -g appuser appuser

# Set working directory
WORKDIR /app

# Create necessary directories
RUN mkdir -p /app/logs /tmp/versions && \
    chown -R appuser:appuser /app /tmp/versions

# Copy the built JAR from the build stage
COPY --from=build /app/target/sales-management-backend-*.jar app.jar

# Change ownership to appuser
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose the application port (Render.com uses PORT environment variable)
EXPOSE ${PORT:-8081}

# Health check optimized for Render.com
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
    CMD curl -f http://localhost:${PORT:-8081}/actuator/health || exit 1

# Environment variables optimized for Aiven MySQL Database
ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-aiven}
ENV JAVA_OPTS="${JAVA_OPTS:--Xmx512m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0}"

# Use dumb-init to handle signals properly in containers
ENTRYPOINT ["dumb-init", "--"]

# Run the application with optimized JVM settings for containers
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
