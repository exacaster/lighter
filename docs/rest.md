# Lighter REST API

## Configuration
**GET** */lighter/api/configuration*

Response Example:
```json
{
  "sparkHistoryServerUrl":"https://localhost/spark-history","sessionConfiguration": {
    "timeoutMinutes":90
  }
}
```

## Batch
**POST** */lighter/api/batches*

Request Exapmple:
```json
{
  "name": "App name",
  "file": "submitted/file/path",
  "numExecutors": 4,
  "executorCores": 2,
  "executorMemory": "2G",
  "driverCores": 2,
  "driverMemory": "1G",
  "args": ["arg1", "arg2"],
  "pyFiles": ["https://something/python_package.zip"],
  "files": ["https://something/something.zip"],
  "conf": {
    "spark.eventLog.enabled": true,
    "spark.eventLog.dir": "s3a://your_bucket/spark-hs/"
  }
}
```

**GET** */lighter/api/batches*

Response example:
```json
{
  "from": 0,
  "total": 20,
  "applications":[
    {
      "id":"8f7e216c-170c-48de-9598-4912b3d54ad7",
      "type":"BATCH",
      "state":"dead",
      "appId":"spark-309b52606e984696a5205a2244e756b9",
      "appInfo":null,
      "kind":"pyspark",
      "submitParams": {
        "name":"TEST_5eb9e358-c0d9-4483-8252-0be0c5269982",
        "file":"local:///opt/spark/work-dir/app.py",
        "numExecutors":3,
        "executorCores":5,
        "executorMemory":"13G",
        "driverCores":3,
        "driverMemory":"10G",
        "args": [
          "--dateFrom",
          "2021-01-04",
          "--dateTo",
          "2021-01-07"
        ],
        "pyFiles":[],
        "files":[],
        "conf":{
          "spark.hadoop.mapreduce.fileoutputcommitter.algorithm.version":"2",
        }
      },
      "createdAt":"2021-10-11T08:12:43.50739",
      "contactedAt":"2021-10-11T08:15:23.335098",
      "finishedAt":"2021-10-11T08:20:15.123456"
    }
  ]
}
```

**GET** */lighter/api/batches/{id}*

Response example:
```json
{
  "id":"8f7e216c-170c-48de-9598-4912b3d54ad7",
  "type":"BATCH",
  "state":"dead",
  "appId":"spark-309b52606e984696a5205a2244e756b9",
  "kind":"pyspark",
  "submitParams": {
    "name":"TEST_5eb9e358-c0d9-4483-8252-0be0c5269982",
    "file":"local:///opt/spark/work-dir/app.py",
    "numExecutors":3,
    "executorCores":5,
    "executorMemory":"13G",
    "driverCores":3,
    "driverMemory":"10G",
    "args": [
      "--dateFrom",
      "2021-01-04",
      "--dateTo",
      "2021-01-07"
    ],
    "pyFiles":[],
    "files":[],
    "conf":{
      "spark.hadoop.mapreduce.fileoutputcommitter.algorithm.version":"2",
    }
  },
  "createdAt":"2021-10-11T08:12:43.50739",
  "contactedAt":"2021-10-11T08:15:23.335098",
  "finishedAt":"2021-10-11T08:20:15.123456"
}
```

**DELETE** */lighter/api/batches/{id}*

Terminates & deletes application.

**GET** */lighter/api/batches/{id}/log*

Response example:
```json
{
  "id": "8f7e216c-170c-48de-9598-4912b3d54ad7",
  "log": "line1\nline2\nline3"
}
```

## Sessions

*/lighter/api/sessions*
Undocumented. Should be somewhat compatible with [Livy Sessions API](https://livy.incubator.apache.org/docs/latest/rest-api.html) when consumed with `X-Compatibility-Mode: sparkmagic` HTTP header.
