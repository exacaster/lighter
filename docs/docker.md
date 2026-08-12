# Installing Lighter on Docker

If you're thinking of running Lighter on docker, you are probably using it with YARN backend. When you are running it, you should expose two ports (REST API port and Python Gateway port), as shown in this example:

```bash
docker run ghcr.io/exacaster/lighter:0.1.6-spark3.5.8 \
  -p 8080:8080 \
  -p 25333:25333 \
  -e LIGHTER_KUBERNETES_ENABLED=false \
  -e LIGHTER_YARN_ENABLED=true \
  -e LIGHTER_YARN_URL=http://your_yarn.local
```

The REST API port is unauthenticated, and the Python Gateway port grants code execution inside the
Lighter process to anything that can connect to it unless `LIGHTER_PY_GATEWAY_AUTH_TOKEN` is set.
Bind both to interfaces that only the Spark cluster and your users' network can reach — see
[Securing a deployment](./security.md).

[Click here](./configuration.md) to see all possible configuration options.
