version: "2"

services:
  jcononfunzionepubblica:
    image: docker.si.cnr.it/##{CONTAINER_ID}##
    mem_limit: 512m
    read_only: true
    environment:
    - LANG=en_US.UTF-8
    - LANGUAGE=en_US:en
    - LC_ALL=en_US.UTF-8
    - SERVICE_TAGS=webapp
    - SERVICE_NAME=##{SERVICE_NAME}##
    tmpfs: /tmp
    volumes:
    - ./webapp_logs:/logs
    command: java -Dspring.profiles.active=fp -Xmx256m -Xss512k -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8787 -Djava.security.egd=file:/dev/./urandom -jar /opt/jconon.war
