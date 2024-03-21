FROM openjdk:17
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} lotd.jar
ENTRYPOINT ["java", "-Dspring.profiles.active=dev", "-jar", "lotd.jar"]