# Lighter on local environment
Besides K8s and YARN cluster modes, it is also possible to run Lighter in local mode.
Local mode is meant for local testing and demo purposes. You can start Lighter locally 
by executing `docker-compose up` command inside `./dev/` folder.

When lighter is running you can execute example applications provided by Apache Spark.
For example `Spark PI` application can be started by executing this `curl` command:

```bash
curl -X 'POST' \
  'http://localhost:8080/lighter/api/batches' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "name": "Spark PI",
  "file": "/home/app/spark/examples/jars/spark-examples_2.12-3.3.0.jar",
  "mainClass": "org.apache.spark.examples.SparkPi",
  "args": ["100"]
}'
```

To set custom environment variables when running lighter in local mode, add `conf` value
with `lighter.local.env.` prefix (ex: `lighter.local.env.FOO`) on your submitted json.

Lighter UI can be accessed on: [http://localhost:8080/lighter](http://localhost:8080/lighter).\
You can also explore Lighter API by visiting Swagger UI on [http://localhost:8080/swagger-ui/](http://localhost:8080/swagger-ui/).