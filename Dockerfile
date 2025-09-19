FROM amazoncorretto:17-alpine-jdk

MAINTAINER seonrizee <seonrizee@gmail.com>

WORKDIR /app

RUN mkdir -p /app/logs

ARG JAR_FILE=build/libs/*.jar

COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java","-jar","app.jar"]

EXPOSE 8080