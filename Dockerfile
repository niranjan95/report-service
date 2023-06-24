FROM openjdk:8-jdk-alpine

WORKDIR /app

COPY target/my-application.jar /app/my-application.jar

EXPOSE 8080

CMD ["java", "-jar", "/app/my-application.jar"]
