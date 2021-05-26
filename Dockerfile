FROM node:alpine3.13

WORKDIR /home/app/

COPY frontend/ ./frontend/
RUN cd frontend && yarn install && yarn build

FROM exacaster/lighter-server:${TAG:-latest}
WORKDIR /home/app/
ENV FRONTEND_PATH=/home/app/frontend/
COPY --from=0 /home/app/frontend/build/ ./frontend/
