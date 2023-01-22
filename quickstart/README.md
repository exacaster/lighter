# Minikube quickstart

### Install Minikube
Follow instructions in https://minikube.sigs.k8s.io/docs/start/ to install the minikube.

### Start Minikube
```
minikube start
```

### Enable Minikube `ingress` addon
```
minikube addons enable ingress
```

### Apply quickstart `.yml` configuration
Go to the location where you cloned the repository i.e.: `cd ~/git/lighter`:
1. Create main resources
```
minikube kubectl -- apply -f quickstart/lighter.yml
minikube kubectl -- apply -f quickstart/jupyterlab.yml
```
2. Create `ingress` resources
```
minikube kubectl -- apply -f quickstart/nginx/lighter-nginx.yml
minikube kubectl -- apply -f quickstart/nginx/jupyterlab-nginx.yml
```

### Verify deployment
**_NOTE:_** Jupyterlab can take few minutes to start because the container is being altered on runtime. 
```
minikube kubectl -- -n spark get all
```
```
NAME                              READY   STATUS    RESTARTS   AGE
pod/jupyterlab-5685dcf5c7-ggcpl   1/1     Running   0          4m34s
pod/lighter-5484769777-7r9ln      1/1     Running   0          5m12s

NAME                 TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)              AGE
service/jupyterlab   ClusterIP   10.100.99.140   <none>        8888/TCP             4m34s
service/lighter      ClusterIP   10.98.245.141   <none>        8080/TCP,25333/TCP   5m12s

NAME                         READY   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/jupyterlab   1/1     1            1           4m34s
deployment.apps/lighter      1/1     1            1           5m12s

NAME                                    DESIRED   CURRENT   READY   AGE
replicaset.apps/jupyterlab-5685dcf5c7   1         1         1       4m34s
replicaset.apps/lighter-5484769777      1         1         1       5m12s
```
CDRs (from `ingress` addon) needs separate call:
```
minikube kubectl -- -n spark get ingress
```
```
NAME         CLASS   HOSTS         ADDRESS        PORTS   AGE
jupyterlab   nginx   spark.local   192.168.49.2   80      87s
lighter      nginx   spark.local   192.168.49.2   80      87s
```

### Optional: Windows with WSL2 with Ubuntu
1. Run this in WSL2
```
minikube tunnel
```
2. Add this entry to your `C:\Windows\System32\Drivers\etc\hosts` file on Windows:
```
127.0.0.1 spark.local
```

### Try it out!

Lighter URL: http://spark.local/lighter/

Jupyterlab URL: http://spark.local/jupyterlab/

**_NOTE:_** Jupyterlab will already have `quickstart.ipynb` notebook with two cells. Execute them and that's it. 