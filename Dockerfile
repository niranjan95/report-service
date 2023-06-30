FROM openjdk:8-jdk-alpine

WORKDIR /app

RUN echo 'printing disc size'

RUN df -h

COPY target/*.jar /app/validation-service.jar

EXPOSE 8080

CMD ["java", "-jar", "/app/validation-service.jar"]
