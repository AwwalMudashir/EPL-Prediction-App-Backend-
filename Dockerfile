# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:17-jre
WORKDIR /app
# We take the JAR from the build stage and rename it to app.jar
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
# Now we use the fixed name 'app.jar'
ENTRYPOINT ["java", "-jar", "app.jar"]