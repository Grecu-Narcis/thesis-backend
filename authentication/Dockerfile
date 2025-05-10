FROM openjdk:17-jdk-alpine

WORKDIR /app

COPY target/authentication-microservice-0.0.1-SNAPSHOT.jar /app/authentication-microservice.jar

EXPOSE 8080

CMD ["java", "-jar", "/app/authentication-microservice.jar"]