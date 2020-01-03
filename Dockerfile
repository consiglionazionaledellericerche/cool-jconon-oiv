# DOCKER-VERSION 1.12
FROM anapsix/alpine-java:8u162b12_server-jre

WORKDIR /app/
ENV OIV_SLEEP 0
ENV JAVA_OPTS="-Xmx1024m -Djava.security.egd=file:/dev/./urandom -Dlogback.configurationFile=logback-fp.xml"

COPY --chown=nobody:nobody target/*.war elenco-oiv.war
COPY --chown=nobody:nobody application-prod.properties .
COPY --chown=nobody:nobody logback-fp.xml .
COPY --chown=nobody:nobody smtpout.palazzochigi.it.crt .

RUN $JAVA_HOME/bin/keytool -import -keystore $JAVA_HOME/jre/lib/security/cacerts -storepass changeit -noprompt -file /app/smtpout.palazzochigi.it.crt -alias smtpout.palazzochigi.it

CMD echo "The application will start in ${OIV_SLEEP}s...with OPTS ${JAVA_OPTS}" && \
    sleep ${OIV_SLEEP} && \
    java ${JAVA_OPTS} -jar elenco-oiv.war

EXPOSE 8080
USER nobody
