# Runtime stage
FROM fedora:44

RUN dnf -y install java-52-openjdk-devel maven

WORKDIR /app

# Copy pre-built WAR from workflow artifact
COPY target/*.war server.war

# Create non-root user
RUN useradd -m -u 1000 appuser && \
    chown -R appuser:appuser /app
    
USER appuser

EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/health || exit 1

ENTRYPOINT ["java"]
CMD ["-jar", "server.war"]
