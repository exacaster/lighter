# Lighter contribution guide

Contributions are welcome and appreciated. This document aims to briefly explain how to contribute to the Lighter project.

## Project structure

Project consists of two main modules: frontend and server. Frontend module contains React SPA for Lighters frontend and server module contains backend code.

## Frontend

To run frontend application locally, you need to  set `LIGHTER_DEV_PROXY_URL` environment variable, so that it points to your production Lighter server. You can create `.env` file in your `frontend` directory and add env variables there, to make it easier to start a frontend application next time.

To install frontend application requirements, run:
```
yarn install
```

To start your application, run:
```
yarn start
```

To run linter:
```
yarn lint
```

To format your code:
```
yarn format
```

Make sure to format your code by using `format` command and check if `lint` returns no errors, before contributing your code.

## Server

You can run server application locally, it will start with in-memory database. But since it requires YARN, or K8s for job coordination, it is not recommended to run server application locally.

Server application runs on Java 11.

To build, navigate to `server` folder and run:
```
./gradlew build [-PSPARK_VERSION=x.y.z]
```

To test:
```
./gradlew test
```

Make sure that there are no failing tests, before contributing your code. While we have no test coverage for fronted application, we require all new backend features to be covered by unit tests.
