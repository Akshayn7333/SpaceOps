# Build the JAR first:  mvn clean package
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/spaceops.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
