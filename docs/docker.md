# Installing Lighter on Docker

If you're thinking of running Lighter on docker, you are probably using it with YARN backend. When you are running it, you should expose two ports (REST API port and Python Gateway port), as shown in this example:

```bash
docker run ghcr.io/exacaster/lighter:0.0.38-spark3.3.0 \
  -p 8080:8080 \
  -p 25333:25333 \
  -e LIGHTER_KUBERNETES_ENABLED=false \
  -e LIGHTER_YARN_ENABLED=true \
  -e LIGHTER_YARN_URL=http://your_yarn.local
```

[Click here](./configuration.md) to see all possible configuration options.
