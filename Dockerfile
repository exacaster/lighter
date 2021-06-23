ARG SERVER_IMAGE=exacaster/lighter/server
ARG SERVER_TAG=latest
FROM node:alpine3.13

WORKDIR /home/app/

ENV REACT_APP_API_BASE_URL='/lighter'
COPY frontend/ ./frontend/
RUN cd frontend && yarn install && yarn build && cd ../

RUN wget "https://downloads.apache.org/spark/spark-3.0.3/spark-3.0.3-bin-hadoop2.7.tgz" -O - | tar -xz

FROM ${SERVER_IMAGE}:${SERVER_TAG}
WORKDIR /home/app/
ENV FRONTEND_PATH=/home/app/frontend/
ENV SPARK_HOME=/home/app/spark/
COPY --from=0 /home/app/frontend/build/ ./frontend/
COPY --from=0 /home/app/spark-3.0.3-bin-hadoop2.7/ ./spark/
COPY k8s/ ./k8s/
