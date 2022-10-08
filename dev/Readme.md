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