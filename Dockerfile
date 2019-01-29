FROM openjdk:8u151-jdk-alpine

MAINTAINER Gaston Tulipani <gtulipani@hotmail.com>

ARG JAR_FILE
ENV JAR_NAME=${JAR_FILE}
ENV JAVA_OPTS="-Xms128M -Xmx256M -XX:MaxMetaspaceSize=128M"

RUN addgroup -g 1000 reservations-service && \
    adduser -S -u 1000 -g reservations-service reservations-service

USER reservations-service

COPY ./target/$JAR_NAME /opt/reservations-service/
WORKDIR /opt/reservations-service
EXPOSE 8090

CMD ["java", "-jar", "reservations-service.jar", "-Xms128M -Xmx256M -XX:MaxMetaspaceSize=128M"]
