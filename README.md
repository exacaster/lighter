# Lighter

Lighter is an opensource application for interacting with [Apache Spark](https://spark.apache.org/) on [Kubernetes](https://kubernetes.io/) or [Apache Hadoop YARN](https://hadoop.apache.org/docs/current/hadoop-yarn/hadoop-yarn-site/YARN.html). It is hevily inspired by [Apache Livy](https://livy.incubator.apache.org/) and has some overlaping features.

Lighter supports:
- Interactive Python Sessions through [Sparkmagic](https://github.com/jupyter-incubator/sparkmagic) kernel
- Batch application submissions through the REST API

> :warning: **If you are using interactive sessions**: While we have tested batch applications quite extensively, there might be some problems with interactive sessions, consider current release of Lighter as alpha.

You can read a breaf description on how Lighter works [here](./docs/architecture.md).

## Using Lighter
- [Quickstart with Minikube](./quickstart/README.md)
- [Installation on Kubernetes](./docs/kubernetes.md)
- [Installation on Docker](./docs/docker.md)
- [Configuration Properties](./docs/configuration.md)
- [Configuring Sparkmagic](./docs/sparkmagic.md)
- [Using REST API](./docs/rest.md)

## Developing Lighter

### Building

To build Docker image, containing application, you need to run

```
docker build -t lighter .
```

### Spark versions

Lighter will always try to support the latest patch version for all officially recommended [Apache Spark](https://spark.apache.org/) releases i.e.: 3.3.3, 3.4.1 and 3.5.0.

### Contributing

See [Contribution guide](./docs/CONTRIBUTING.md)

## License

Lighter is [MIT](./LICENSE.txt) licensed.
