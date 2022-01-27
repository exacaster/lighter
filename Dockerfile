FROM openjdk:11-jre-slim-stretch as server

ARG SPARK_VERSION=3.2.1

WORKDIR /home/app/
COPY server/ ./server/

WORKDIR /home/app/server/
RUN ./gradlew build -PSPARK_VERSION=${SPARK_VERSION}

FROM node:lts-alpine3.14 as frontend

ARG SPARK_VERSION=3.2.1

ENV REACT_APP_API_BASE_URL='/lighter'

WORKDIR /home/app/
COPY frontend/ ./frontend/
RUN wget "https://downloads.apache.org/spark/spark-${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-hadoop3.2.tgz" -O - | tar -xz

WORKDIR /home/app/frontend/
RUN yarn install && yarn build

FROM openjdk:11-jre-slim-stretch

ARG SPARK_VERSION=3.2.1

ENV FRONTEND_PATH=/home/app/frontend/
ENV SPARK_HOME=/home/app/spark/

# Add symlinks so that after deployment of CM configs symlinks are still in tact
RUN ln -s /etc/hadoop/conf.cloudera.yarn /etc/alternatives/hadoop-conf \
  && ln -s /etc/hive/conf.cloudera.hive /etc/alternatives/hive-conf

WORKDIR /home/app/
COPY --from=server /home/app/server/build/docker/main/layers/libs /home/app/libs
COPY --from=server /home/app/server/build/docker/main/layers/resources /home/app/resources
COPY --from=server /home/app/server/build/docker/main/layers/application.jar /home/app/application.jar

COPY --from=frontend /home/app/frontend/build/ ./frontend/
COPY --from=frontend /home/app/spark-${SPARK_VERSION}-bin-hadoop3.2/ ./spark/

COPY k8s/ ./k8s/

EXPOSE 8080
EXPOSE 25333

ENTRYPOINT ["java", "-jar", "/home/app/application.jar"]
