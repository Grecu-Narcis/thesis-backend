FROM openjdk:17-jdk-alpine

WORKDIR /app

COPY target/posts-service-0.0.1-SNAPSHOT.jar /app/posts-service.jar

EXPOSE 8081

CMD ["java", "-jar", "/app/posts-service.jar"]