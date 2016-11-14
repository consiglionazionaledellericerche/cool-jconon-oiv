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
  volumes:
  - ./webapp_logs:/logs
  - /tmp
