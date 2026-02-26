FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

ARG JAR_FILE=build/libs/*-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java",
            "-XX:+UseContainerSupport",
            "-XX:MaxRAMPercentage=75.0",
            "-Duser.timezone=Asia/Seoul",
            "-jar",
            "app.jar"]