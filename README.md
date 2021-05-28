# Lighter

REST API for submitting Apache Spark applications on Kubernetes.

## Building

To build Docker image, containing application, you need to run

```
./build_docker.sh
```

## Structure

Project consists of two modules:
- [server](./server/) - REST API for submitting Apache Spark applications
- [frontend](./frontend/) - frontend application for reading Apache Spark application logs and tracking history.