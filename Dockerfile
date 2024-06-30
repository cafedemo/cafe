FROM openjdk:21-jdk-slim as build
COPY . /app
RUN cd /app && /app/gradlew -x test build

FROM openjdk:21-slim
COPY --from=build /app/build/libs/cafe*SNAPSHOT.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]