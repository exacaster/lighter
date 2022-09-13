# Running Lighter on Kubernetes

You need to create multiple resources to add Lighter to your Kubernetes cluster.
On these examples we assume that you are running your spark related services on `spark` namespace.

## ServiceAccount and RoleBinding

```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: spark
  namespace: spark
---
kind: Role
apiVersion: rbac.authorization.k8s.io/v1
metadata:
  name: lighter-spark
  namespace: spark
rules:
- apiGroups: [""]
  resources: ["pods", "services", "configmaps", "pods/log"]
  verbs: ["*"]
---
kind: RoleBinding
apiVersion: rbac.authorization.k8s.io/v1
metadata:
  name: lighter-spark
  namespace: spark
subjects:
- kind: ServiceAccount
  name: spark
  namespace: spark
roleRef:
  kind: Role
  name: lighter-spark
  apiGroup: rbac.authorization.k8s.io
```

## Service
```yaml
apiVersion: v1
kind: Service
metadata:
    name: lighter
    namespace: spark
    labels:
        run: lighter
spec:
    ports:
        -   name: api
            port: 8080
            protocol: TCP
        -   name: javagw
            port: 25333
            protocol: TCP
    selector:
        run: lighter
```

## Deployment

Make sure to change `env` values to valid ones.
[Click here](./configuration.md) to see all possible configuration options.

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
    namespace: spark
    name: lighter
spec:
    selector:
        matchLabels:
            run: lighter
    replicas: 1
    strategy:
        rollingUpdate:
            maxUnavailable: 0
            maxSurge: 1
    template:
        metadata:
            labels:
                run: lighter
        spec:
            containers:
                -   image: ghcr.io/exacaster/lighter:0.0.38-spark3.3.0
                    name: lighter
                    readinessProbe:
                        httpGet:
                            path: /health/readiness
                            port: 8080
                        initialDelaySeconds: 15
                        periodSeconds: 15
                    resources:
                        requests:
                            cpu: "0.25"
                            memory: "512Mi"
                    ports:
                        -   containerPort: 8080
                    env:
                        -   name: LIGHTER_STORAGE_JDBC_USERNAME
                            value: postgres_user
                        -   name: LIGHTER_STORAGE_JDBC_PASSWORD
                            value: postgres_password
                        -   name: LIGHTER_STORAGE_JDBC_URL
                            value: jdbc:postgresql://postgres_host_name:5432/lighter
                        -   name: LIGHTER_STORAGE_JDBC_DRIVER_CLASS_NAME
                            value: org.postgresql.Driver
                        -   name: LIGHTER_SPARK_HISTORY_SERVER_URL
                            value: https://address_to_spark_history/spark-history
                        -   name: LIGHTER_MAX_RUNNING_JOBS
                            value: "15"
            serviceAccountName: spark
```

## Ingress

To make your Lighter UI accessible, you also need to add an Ingress component.
For example, if you are using Traefik Ingress controller, something like this should work:

```yaml
apiVersion: traefik.containo.us/v1alpha1
kind: IngressRoute
metadata:
  name: lighter-ingress-route
  namespace: spark
spec:
  entryPoints:
    - web
  routes:
    - match: PathPrefix(`/lighter`)
      kind: Rule
      services:
        - name: lighter
          port: 8080
      middlewares:
        - name: lighter-custom-headers
---
apiVersion: traefik.containo.us/v1alpha1
kind: Middleware
metadata:
  name: lighter-custom-headers
  namespace: spark
spec:
  headers:
    customRequestHeaders:
      X-Forwarded-Prefix: /lighter
      X-Forwarded-Proto: https
      X-Forwarded-Port: "443"
```
