# Lighter configuration

Lighter can be configured by using environment variables. Currently, Lighter supports only one of backends on single deployment: Yarn or Kubernetes.

## Global properties

| Property                               | Description                                                    | Default                         |
|----------------------------------------|----------------------------------------------------------------|---------------------------------|
| LIGHTER_MAX_RUNNING_JOBS               | Max running Batch jobs in parallel                             | 5                               |
| LIGHTER_MAX_STARTING_JOBS              | Max starting Batch jobs in parallel                            | 5                               |
| LIGHTER_SPARK_HISTORY_SERVER_URL       | Spark history server URL used on frontend                      | http://localhost/spark-history/ |
| LIGHTER_EXTERNAL_LOGS_URL_TEMPLATE     | Template for link to external logs                             |                                 |
| LIGHTER_PY_GATEWAY_PORT                | Port for live Spark session communication                      | 25333                           |
| LIGHTER_URL                            | URL which can be used to access Lighter form Spark Job         | http://lighter.spark:8080       |
| LIGHTER_SESSION_TIMEOUT_MINUTES        | Session lifetime in minutes                                    | 90                              |
| LIGHTER_STORAGE_JDBC_URL               | JDBC url for lighter storage                                   | jdbc:h2:mem:lighter             |
| LIGHTER_STORAGE_JDBC_USERNAME          | JDBC username                                                  | sa                              |
| LIGHTER_STORAGE_JDBC_PASSWORD          | JDBC password                                                  |                                 |
| LIGHTER_STORAGE_JDBC_DRIVER_CLASS_NAME | JDBC driver class name                                         | org.h2.Driver                   |
| LIGHTER_BATCH_DEFAULT_CONF             | Default `conf` props for batch applications (JSON)<sup>*</sup> |                                 |
| LIGHTER_SESSION_DEFAULT_CONF           | Default `conf` props for session applications (JSON)           |                                 |

<sup>*</sup> default confs will be merged with confs provided in submit request, if property is defined in submit request, default will be ignored.
Example of `LIGHTER_BATCH_DEFAULT_CONF`: `{"spark.kubernetes.driverEnv.TEST1":"test1"}`.

## Kubernetes configuration

| Property                           | Description                                          | Default                                        |
| ---------------------------------- | ---------------------------------------------------- |------------------------------------------------|
| LIGHTER_KUBERNETES_ENABLED         | Kubernetes enabled                                   | false                                          |
| LIGHTER_KUBERNETES_MASTER          | Kubernetes master URL                                | k8s://kubernetes.default.svc.cluster.local:443 |
| LIGHTER_KUBERNETES_NAMESPACE       | Kubernetes namespace                                 | spark                                          |
| LIGHTER_KUBERNETES_MAX_LOG_SIZE    | Max lines of log to store on DB                      | 500                                            |
| LIGHTER_KUBERNETES_SERVICE_ACCOUNT | Kubernetes service account                           | spark                                          |


## YARN configuration

| Property                        | Description                                                       | Default                           |
|---------------------------------|-------------------------------------------------------------------| --------------------------------- |
| LIGHTER_YARN_ENABLED            | Yarn enabled (Kubernetes should be disabled)                      | false                             |
| LIGHTER_YARN_URL                | Yarn API URL, `/ws/v1/cluster/` will be appended                  |                                   |
| HADOOP_CONF_DIR                 | Path to `core-site.xml`,`hdfs-site.xml` and `yarn-site.xml` files |                                   |
| LIGHTER_YARN_KERBEROS_PRINCIPAL | Kerberos principal used for job management<sup>*</sup>            |                                   |
| LIGHTER_YARN_KERBEROS_KEYTAB    | Kerberos keytab used for job management                           |                                   |

<sup>*</sup> Principal & Keytab provided in `LIGHTER_YARN_KERBEROS_PRINCIPAL` and `LIGHTER_YARN_KERBEROS_KEYTAB` will be used by spark job
as well, if `spark.kerberos.keytab` is not explicitly declared in `LIGHTER_BATCH_DEFAULT_CONF` or provided on submit request.
