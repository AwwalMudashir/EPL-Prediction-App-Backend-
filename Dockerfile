FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY . .

RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

EXPOSE 8080

# Look for your COPY line and change the destination to app.jar
COPY target/*.jar app.jar

# Then change your ENTRYPOINT to:
ENTRYPOINT ["java", "-jar", "app.jar"]