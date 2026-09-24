# Build stage - compiles the jar, discarded from the final image.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src ./src
# Skips *running* the tests, not compiling them - they need a live Postgres
# and Redis to actually run (see the various @SpringBootTest classes), which
# this build stage doesn't have. That's what CI is for, not the image build.
RUN ./mvnw clean package -DskipTests -B

# Run stage - just a JRE and the built jar.
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
