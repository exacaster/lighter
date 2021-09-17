# Lighter configuration

Lighter can be configurated by using environment variables. Currently Lighter supports only one of backends on single deployment: Yarn or Kubernetes.

## Global properties

| Property                             | Description                                            | Default                          |
| ------------------------------------ | ------------------------------------------------------ | -------------------------------- |
| LIGHTER_MAX_RUNNING_JOBS             | Max running Batch jobs in parallel                     | 5                                |
| LIGHTER_SPARK_HISTORY_SERVER_URL     | Spark history server URL used on frontend              | http://localhost/spark-history/  |
| LIGHTER_PY_GATEWAY_PORT              | Port for live Spark session communication              | 25333                            |
| LIGHETR_URL                          | URL which can be used to access Lighter form Spark Job | http://lighter.spark:8080        |
| LIGHTER_SESSION_TIMEOUT_MINUTES      | Session lifetime in minutes                            | 90                               |
| LIGHTER_STORAGE_JDBC_URL             | JDBC url for lighter storage                           | jdbc:h2:mem:lighter              |
| LIGHTER_STORAGE_JDBC_USERNAME        | JDBC username                                          | sa                               |
| LIGHTER_STORAGE_JDBC_PASSWORD        | JDBC password                                          |                                  |
| LIGHTER_STORAGE_JDBC_DRIVERCLASSNAME | JDBC driver class name                                 | org.h2.Driver                    |


## Kubernetes configuration

| Property                           | Description                                          | Default                                        |
| ---------------------------------- | ---------------------------------------------------- | ---------------------------------------------- |
| LIGHTER_KUBERNETES_ENABLED         | Kubernetes enabled                                   | true                                           |
| LIGHTER_KUBERNETES_MASTER          | Kubernetes master URL                                | k8s://kubernetes.default.svc.cluster.local:443 |
| LIGHTER_KUBERNETES_NAMESPACE       | Kubernetes namespace                                 | spark                                          |
| LIGHTER_KUBERNETES_MAX_LOG_SIZE    | Max lines of log to store on DB                      | 500                                            |
| LIGHTER_KUBERNETES_SERVICE_ACCOUNT | Kubernetes service account                           | spark                                          |
| LIGHTER_KUBERNETES_CONTAINER_IMAGE | Container image to use for Spark driver and executor | TODO                                           |


## YARN configuration

| Property                           | Description                                          | Default                                        |
| ---------------------------------- | ---------------------------------------------------- | ---------------------------------------------- |
| LIGHTER_YARN_ENABLED               | Yarn enabled (Kubernetes should be disabled)         | false                                          |
| LIGHTER_YARN_URL                   | Yarn API URL, `/ws/v1/cluster/` will be appended     |                                                |
