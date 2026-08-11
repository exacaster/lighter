# Lighter

Lighter is an opensource application for interacting with [Apache Spark](https://spark.apache.org/) on [Kubernetes](https://kubernetes.io/) or [Apache Hadoop YARN](https://hadoop.apache.org/docs/current/hadoop-yarn/hadoop-yarn-site/YARN.html).
It is heavily inspired by [Apache Livy](https://livy.incubator.apache.org/) and has some overlapping features.

Lighter support:
- Interactive Python Sessions through [Sparkmagic](https://github.com/jupyter-incubator/sparkmagic) kernel
- Batch application submissions through the REST API

You can read a description on how Lighter works [here](./docs/architecture.md).

## Using Lighter
- [Quickstart with Minikube](./quickstart/README.md)
- [Installation on Kubernetes](./docs/kubernetes.md)
- [Installation on Docker](./docs/docker.md)
- [Configuration Properties](./docs/configuration.md)
- [Securing a deployment](./docs/security.md)
- [Configuring Sparkmagic](./docs/sparkmagic.md)
- [Using REST API](./docs/rest.md)

> [!IMPORTANT]
> Lighter has no built-in authentication: anyone who can reach its REST API can run code on
> your Spark cluster. Read [Securing a deployment](./docs/security.md) before exposing it.

## Developing Lighter

### Building

To build Docker image, containing application, you need to run

```
docker build -t lighter .
```

### Spark versions

Lighter will always try to support the latest patch version for all officially recommended [Apache Spark](https://spark.apache.org/) releases i.e.: 3.5.8 and 4.1.1.

### Contributing

See [Contribution guide](./docs/CONTRIBUTING.md)

## License

Lighter is [MIT](./LICENSE.txt) licensed.
