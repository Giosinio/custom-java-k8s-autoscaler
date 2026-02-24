FROM eclipse-temurin:17-jre-alpine

ARG JAR_FILE=target/task-service-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} /app/task-service.jar

ENTRYPOINT ["java","-jar","/app/task-service.jar"]
