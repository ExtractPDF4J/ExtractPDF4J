FROM maven:3.9.2-eclipse-temurin-17 AS build
WORKDIR /app

# Override at build time if targeting a different supported platform.
ARG JAVACPP_PLATFORM=linux-x86_64

COPY pom.xml .

# Copy all the essential modules to the build's /app
COPY extractpdf4j-bom ./extractpdf4j-bom
COPY extractpdf4j-service ./extractpdf4j-service
COPY extractpdf4j-cli ./extractpdf4j-cli
COPY extractpdf4j-core ./extractpdf4j-core

# Build the JARs and avoid the 'extractpdf4j-bom' module
RUN mvn -Djavacpp.platform=${JAVACPP_PLATFORM} \
    -pl '!extractpdf4j-bom' -am clean package -DskipTests

# Copy the essential .jar that runs the app to app.jar
RUN set -eu; \
    jar="$(find /app/extractpdf4j-cli/target -maxdepth 1 -type f \
      -name 'extractpdf4j-cli-*.jar' \
      ! -name '*-sources.jar' \
      ! -name '*-javadoc.jar' \
      ! -name 'original-*' \
      -print -quit)"; \
    test -n "$jar"; \
    cp "$jar" /app/app.jar

FROM gcr.io/distroless/java17-debian12:nonroot
WORKDIR /app

COPY --from=build /app/app.jar ./app.jar
# Optional
#COPY examples ./examples

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
