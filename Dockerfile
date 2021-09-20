FROM dckregistry.exacaster.com:5000/docker/java/11-jre-slim:master as server

WORKDIR /home/app/
COPY server/ ./server/

WORKDIR /home/app/server/
RUN ./gradlew build

FROM node:alpine3.13 as frontend

ARG SPARK_VERSION=3.0.3
ARG HADOOP_VERSION=2.7

ENV REACT_APP_API_BASE_URL='/lighter'

WORKDIR /home/app/
COPY frontend/ ./frontend/
RUN wget "https://downloads.apache.org/spark/spark-${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz" -O - | tar -xz

WORKDIR /home/app/frontend/
RUN yarn install && yarn build

FROM dckregistry.exacaster.com:5000/docker/java/11-jre-slim:master

ARG SPARK_VERSION=3.0.3
ARG HADOOP_VERSION=2.7

ENV FRONTEND_PATH=/home/app/frontend/
ENV SPARK_HOME=/home/app/spark/

WORKDIR /home/app/
COPY --from=server /home/app/server/build/layers/libs /home/app/libs
COPY --from=server /home/app/server/build/layers/resources /home/app/resources
COPY --from=server /home/app/server/build/layers/application.jar /home/app/application.jar

COPY --from=frontend /home/app/frontend/build/ ./frontend/
COPY --from=frontend /home/app/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}/ ./spark/

COPY k8s/ ./k8s/

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/home/app/application.jar"]
