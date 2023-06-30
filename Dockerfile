FROM openjdk:8-jdk-alpine

WORKDIR /app

COPY target/*.jar /app/validation-service.jar

EXPOSE 8080

CMD ["java", "-jar", "/app/validation-service.jar"]
