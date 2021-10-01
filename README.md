# Lighter

Lighter is an opensource application for interacting with [Apache Spark](https://spark.apache.org/) on [Kubernetes](https://kubernetes.io/) or [Apache Hadoop YARN](https://hadoop.apache.org/docs/current/hadoop-yarn/hadoop-yarn-site/YARN.html). It is hevily inspired by [Apache Livy](https://livy.incubator.apache.org/) and has some overlaping features.

Lighter supports:
- Interactive Python Sessions through [Sparkmagic](https://github.com/jupyter-incubator/sparkmagic) kernel
- Batch job submissions through the REST API

## Using Lighter
- [Installation on Kubernetes](./docs/kubernetes.md)
- [Installation on Docker](./docs/docker.md)
- [Configuration Properties](./docs/configuration.md)

## Developing Lighter

### Building

To build Docker image, containing application, you need to run

```
docker build -t lighter .
```

### Contributing

See [Contribution guide](./docs/CONTRIBUTING.md)

## License

Lighter is [MIT](./LICENSE.txt) liceansed.
