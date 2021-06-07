#!/bin/bash

GREEN="32"
BOLDGREEN="\e[1;${GREEN}m"
ENDCOLOR="\e[0m"

TAG=${TAG:-latest}
SERVER_IMAGE=${SERVER_IMAGE:-exacaster/lighter/server}

echo -e "${BOLDGREEN}Building server${ENDCOLOR}"
cd server
./gradlew dockerBuild -PTAG=${TAG} -PIMAGE=${SERVER_IMAGE}
cd ..

if [ "$1" == "full" ]; then
  FULL_IMAGE=${FULL_IMAGE:-exacaster/lighter}
  echo -e "${BOLDGREEN}Building docker with frontend${ENDCOLOR}"
  docker build . -t ${FULL_IMAGE}:${TAG} --network host --build-arg SERVER_IMAGE=${SERVER_IMAGE} --build-arg SERVER_TAG=${TAG}
fi;