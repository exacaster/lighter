FROM eclipse-temurin:17-jre-jammy as server

ARG SPARK_VERSION=3.5.0
ARG HADOOP_VERSION=3

WORKDIR /home/app/
COPY server/ ./server/

WORKDIR /home/app/server/
RUN ./gradlew build -x test -PSPARK_VERSION=${SPARK_VERSION}

FROM node:lts-alpine3.18 as frontend

ARG SPARK_VERSION=3.5.0
ARG HADOOP_VERSION=3

ENV APP_BASE_URL='/lighter'

WORKDIR /home/app/
COPY frontend/ ./frontend/
RUN wget "https://downloads.apache.org/spark/spark-${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz" -O - | tar -xz

WORKDIR /home/app/frontend/
RUN yarn install && yarn build

FROM eclipse-temurin:11-jre-jammy

ARG SPARK_VERSION=3.5.0
ARG HADOOP_VERSION=3

ENV FRONTEND_PATH=/home/app/frontend/
ENV SPARK_HOME=/home/app/spark/

# Add symlinks so that after deployment of CM configs symlinks are still in tact
RUN ln -s /etc/hadoop/conf.cloudera.yarn /etc/alternatives/hadoop-conf \
  && ln -s /etc/hive/conf.cloudera.hive /etc/alternatives/hive-conf

WORKDIR /home/app/
COPY --from=server /home/app/server/build/docker/main/layers/libs /home/app/libs
COPY --from=server /home/app/server/build/docker/main/layers/resources /home/app/resources
COPY --from=server /home/app/server/build/docker/main/layers/application.jar /home/app/application.jar

COPY --from=frontend /home/app/frontend/dist/ ./frontend/
COPY --from=frontend /home/app/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}/ ./spark/

COPY k8s/ ./k8s/

ARG spark_uid=10000
ARG spark_gid=10001
RUN groupadd -g ${spark_gid} spark && useradd spark -u ${spark_uid} -g ${spark_gid} -m -s /bin/bash
RUN chown -R spark:spark ${SPARK_HOME} && \
    chmod -R go+rX ${SPARK_HOME}
RUN apt-get update && apt-get upgrade -y && \
    apt-get autoremove --purge -y curl wget && \
    apt-get install -y --no-install-recommends --allow-downgrades -y atop procps && \
    apt-get clean && rm -rf /var/lib/apt/lists /var/cache/apt/*

EXPOSE 8080
EXPOSE 25333

ENTRYPOINT ["java", "-jar", "/home/app/application.jar"]
# Specify the User that the actual main process will run as
USER ${spark_uid}
