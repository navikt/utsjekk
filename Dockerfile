FROM ghcr.io/navikt/baseimages/temurin:17
COPY dp-iverksett-api/target/dp-iverksett-api.jar "app.jar"
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
