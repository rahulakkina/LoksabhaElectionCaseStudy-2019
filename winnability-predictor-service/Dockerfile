FROM maven:3.5.3-jdk-8 AS builder
COPY pom.xml pom.xml
COPY src/ src/


RUN mvn package

FROM openjdk:8-jdk-alpine
VOLUME /tmp
RUN mkdir -p ../datasets
COPY ../datasets ../datasets
RUN unzip ../datasets/model/LS-2019-prediction.zip ../datasets/model
COPY --from=builder target/winnability-predictor-svc-1.0-SNAPSHOT.jar Application.jar
ENTRYPOINT ["java","-jar","/Application.jar"]