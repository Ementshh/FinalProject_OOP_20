# Build stage
FROM gradle:jdk17-alpine AS build
WORKDIR /app
COPY . .
RUN ./gradlew build -x test --no-daemon

# Run stage
FROM openjdk:17-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]