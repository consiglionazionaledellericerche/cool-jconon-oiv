# DOCKER-VERSION 1.12
FROM anapsix/alpine-java:jdk8
MAINTAINER Francesco Uliana <francesco.uliana@cnr.it>

COPY target/*.war /opt/jconon.war

EXPOSE 8080

# https://spring.io/guides/gs/spring-boot-docker/#_containerize_it
CMD ["java", "-Dspring.profiles.active=fp", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/opt/jconon.war"]
