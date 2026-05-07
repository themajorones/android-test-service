FROM fedora:44

ARG WAR_FILE=server.war

RUN set -eux; \
    dnf -y upgrade --refresh; \
    curl -fsSL https://rpm.nodesource.com/setup_22.x | bash -; \
    dnf -y install \
      java-25-openjdk-headless \
      nodejs \
      curl \
      unzip \
      findutils; \
    dnf clean all; \
    rm -rf /var/cache/dnf

WORKDIR /app

RUN set -eux; \
    useradd -r -u 1000 -m -d /app appuser

COPY ${WAR_FILE} /app/server.war

RUN set -eux; \
    chown -R appuser:appuser /app; \
    test -f /app/server.war

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -fsS http://localhost:8080/health || exit 1

ENTRYPOINT ["java", "--add-opens", "java.base/java.util.concurrent=ALL-UNNAMED", "-Xmx1g", "-Dserver.port=8080", "-jar", "/app/server.war"]
