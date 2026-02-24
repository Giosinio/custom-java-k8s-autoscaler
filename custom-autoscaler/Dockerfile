FROM eclipse-temurin:17-jre-alpine

ARG JAR_FILE=target/custom-autoscaler-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} /app/custom-autoscaler.jar

ENTRYPOINT ["java","-jar","/app/custom-autoscaler.jar"]
