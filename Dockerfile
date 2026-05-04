# Build stage
FROM eclipse-temurin:25-jdk-jammy AS builder

WORKDIR /build

# Copy Maven wrapper and pom.xml
COPY mvnw pom.xml ./
COPY .mvn .mvn/

# Copy source code
COPY src src/
COPY frontend frontend/
COPY package.json package-lock.json ./

# Build application
RUN chmod +x mvnw && \
    ./mvnw clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:25-jre-jammy

WORKDIR /app

# Copy WAR from builder
COPY --from=builder /build/target/*.war app.war

# Create non-root user
RUN useradd -m -u 1000 appuser && \
    chown -R appuser:appuser /app
    
USER appuser

EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/health || exit 1

ENTRYPOINT ["java"]
CMD ["-jar", "app.war"]
