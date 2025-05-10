FROM openjdk:17-jdk-alpine

WORKDIR /app

COPY build/libs/follow-service-0.0.1-SNAPSHOT.jar /app/follow-service.jar

EXPOSE 8082

CMD ["java", "-jar", "/app/follow-service.jar"]