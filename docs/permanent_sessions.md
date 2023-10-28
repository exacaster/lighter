# Permanent sessions

The Lighter has a feature for permanent interactive sessions. These sessions are useful for cases where there is
a need for a session that runs indefinitely and where occasional session statements need to be submitted directly
through the REST API.

The Lighter takes care of maintaining the continuity of these sessions, ensuring they remain active and restarting
them in case of failures without altering the session identifier.

It's important to note that in some cases, when a session is restarted due to a failure, it may not restore the previous
state. Therefore, it's advisable to make your statements independent of the previous session state.

## Configuration

Permanent sessions can be configured through by setting `LIGHTER_SESSION_PERMANENT_SESSIONS` environment variable.
Example value:
```json
[
  {
    "id": "permanent-id-used-on-api-calls",
    "submit-params": {
      "name": "Session Name",
      "numExecutors": 4,
      "executorCores": 2,
      "executorMemory": "2G",
      "driverCores": 2,
      "driverMemory": "1G",
      "conf": {
        "spark.eventLog.enabled": true,
        "spark.eventLog.dir": "s3a://your_bucket/spark-hs/"
      }
    }
  }
]
```
