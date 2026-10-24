FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /source
COPY pom.xml .
COPY src src
RUN mvn --batch-mode --no-transfer-progress verify

FROM eclipse-temurin:21-jre-jammy
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --gid 10001 mudocs \
    && useradd --uid 10001 --gid mudocs --no-create-home mudocs \
    && mkdir -p /app/letsencrypt && chown -R mudocs:mudocs /app \
    && chmod 700 /app/letsencrypt
WORKDIR /app
COPY --from=build /source/target/docs-1.0-SNAPSHOT.jar /app/docs.jar
COPY deployment/logback-container.xml /app/logback-container.xml
COPY --chmod=0555 deployment/entrypoint.sh /app/entrypoint.sh
USER 10001:10001
EXPOSE 8080 8443
ENTRYPOINT ["/app/entrypoint.sh"]
