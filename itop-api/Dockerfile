# Multi-stage build for iTop Java API
# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Optional proxy support: pass --build-arg HTTP_PROXY=http://host.docker.internal:7897 if needed
ARG HTTP_PROXY=
ARG HTTPS_PROXY=
ENV HTTP_PROXY=${HTTP_PROXY} \
    HTTPS_PROXY=${HTTPS_PROXY}

# Generate Maven settings.xml with proxy config only when HTTP_PROXY is provided
RUN mkdir -p /root/.m2 && \
    if [ -n "$HTTP_PROXY" ]; then \
      PROXY_HOST=$(echo "$HTTP_PROXY" | sed -n 's|^https\?://||p' | cut -d: -f1); \
      PROXY_PORT=$(echo "$HTTP_PROXY" | sed -n 's|^https\?://||p' | cut -d: -f2); \
      echo "<settings xmlns=\"http://maven.apache.org/SETTINGS/1.0.0\" \
            xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \
            xsi:schemaLocation=\"http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd\"> \
              <proxies> \
                <proxy><id>http-proxy</id><active>true</active><protocol>http</protocol><host>${PROXY_HOST}</host><port>${PROXY_PORT}</port></proxy> \
                <proxy><id>https-proxy</id><active>true</active><protocol>https</protocol><host>${PROXY_HOST}</host><port>${PROXY_PORT}</port></proxy> \
              </proxies> \
            </settings>" > /root/.m2/settings.xml; \
    else \
      echo '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" \
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" \
            xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd"> \
            </settings>' > /root/.m2/settings.xml; \
    fi

# Copy pom files first (better layer caching)
COPY pom.xml .
COPY itop-common/pom.xml itop-common/
COPY itop-core/pom.xml itop-core/
COPY itop-cmdb/pom.xml itop-cmdb/
COPY itop-api/pom.xml itop-api/
COPY itop-web/pom.xml itop-web/

# Download dependencies with cache mount (faster rebuilds)
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn dependency:go-offline -B -s /root/.m2/settings.xml

# Copy source code
COPY itop-common/src itop-common/src
COPY itop-core/src itop-core/src
COPY itop-cmdb/src itop-cmdb/src
COPY itop-api/src itop-api/src

# Build application with cache mount
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn clean package -DskipTests -pl '!itop-web' -B -s /root/.m2/settings.xml

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="iTop Java Team"
LABEL version="1.0.0"
LABEL description="iTop Java CMDB API Server"

# Install necessary packages
RUN apk add --no-cache curl tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    apk del tzdata

# Create non-root user
RUN addgroup -S itop && adduser -S itop -G itop

WORKDIR /app

# Copy jar from builder
COPY --from=builder /build/itop-api/target/itop-api-1.0.0-SNAPSHOT.jar app.jar

# Prepare persistent application storage and grant the runtime user write access.
RUN mkdir -p /app/uploads && chown -R itop:itop /app

# Switch to non-root user
USER itop

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/api/actuator/health || exit 1

# JVM options
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Entry point
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]
