FROM openjdk:8u151-jdk-alpine

MAINTAINER Gaston Tulipani <gtulipani@hotmail.com>

RUN addgroup -g 1000 reservations-service && \
    adduser -S -u 1000 -g reservations-service reservations-service

USER reservations-service

COPY ./target/reservations-service.jar /opt/reservations-service/
WORKDIR /opt/reservations-service
EXPOSE 8090

CMD ["java", "-jar", "reservations-service.jar"]
