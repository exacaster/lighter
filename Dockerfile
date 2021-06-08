ARG SERVER_IMAGE=exacaster/lighter/server
ARG SERVER_TAG=latest
FROM node:alpine3.13

WORKDIR /home/app/

ENV REACT_APP_API_BASE_URL='/lighter'
COPY frontend/ ./frontend/
RUN cd frontend && yarn install && yarn build

FROM ${SERVER_IMAGE}:${SERVER_TAG}
WORKDIR /home/app/
ENV FRONTEND_PATH=/home/app/frontend/
COPY --from=0 /home/app/frontend/build/ ./frontend/
