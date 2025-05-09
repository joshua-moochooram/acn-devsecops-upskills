FROM openjdk:17-ea-jdk-buster
MAINTAINER devsahamerlin

RUN groupadd -g 999 appgroup && \
    useradd -r -u 999 -g appgroup merlin
RUN mkdir /usr/app && chown merlin:appgroup /usr/app
WORKDIR /usr/app

COPY --chown=merlin:appgroup target/acn-taskmanger-upskills-1.0-SNAPSHOT.jar /usr/app/acn-taskmanger-upskills.jar

USER merlin
EXPOSE 8082

ENTRYPOINT ["java","-jar","/usr/app/acn-taskmanger-upskills.jar"]