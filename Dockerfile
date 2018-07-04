# DOCKER-VERSION 1.12
FROM anapsix/alpine-java:jdk8
RUN apt-get install language-pack-it
MAINTAINER Francesco Uliana <francesco.uliana@cnr.it>

COPY target/*.war /opt/jconon.war

# HEALTHCHECK --interval=5s --timeout=3s CMD curl --fail http://localhost:8080/health || exit 1

EXPOSE 8080

# https://spring.io/guides/gs/spring-boot-docker/#_containerize_it
CMD ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/opt/jconon.war"]
