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
  "priority": 100,
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

`priority` is optional and controls the order in which *waiting* batches are started:
**a higher value is picked sooner**. It defaults to `0` (normal); negative values are started after
normal ones. Batches sharing a priority keep first-come-first-served order, so omitting the field
leaves ordering exactly as it was before priorities existed.

Note that `priority` is accepted at the top level of the request but is **not** part of
`submitParams`: it is stored separately because it can be changed after submission (see below), so
responses report it at the top level only and never inside `submitParams`.

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
      "priority":0,
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
  "priority":0,
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

**POST** */lighter/api/batches/{id}/priority*

Changes the priority of a batch that has not started yet. **A higher value is picked sooner**; `0`
is normal.

Request example:
```json
{
  "priority": -50
}
```

Responds with the batch, in the same form as `GET /lighter/api/batches/{id}`. If the batch has
already started (or has finished), the request is a no-op: the response is still `200` with the
batch unchanged, never an error, because a batch can be picked up at any moment while the change is
in flight. An unknown or deleted batch id responds `404`.

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

Sessions share their response model with batches, so session responses also carry a `priority`
field. Sessions are not prioritized — the field is always `0` there and is ignored.
