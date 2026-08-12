# Securing a deployment

Lighter has no built-in authentication or authorization, and it does not restrict the
application file or the Spark properties a caller submits. Read [SECURITY.md](../SECURITY.md)
for the full trust model. This page covers what an operator has to put around Lighter.

## Authenticate the REST API

Port `8080` serves the REST API, the UI and Swagger UI. Nothing on it is protected, so place an
authenticating reverse proxy in front of it and let nothing else reach the port:

- an ingress controller with an authentication annotation, for example
  `nginx.ingress.kubernetes.io/auth-url` pointing at [oauth2-proxy](https://github.com/oauth2-proxy/oauth2-proxy)
- nginx `auth_request`, or a service mesh policy
- basic authentication, if the clients are scripts rather than people

Sparkmagic can authenticate against such a proxy: set `"auth": "Basic_Access"` or
`"auth": "Kerberos"` in the kernel configuration instead of the `"auth": "None"` shown in
[Configuring Sparkmagic](./sparkmagic.md).

A browser page on another origin cannot call the API: Micronaut refuses any request whose `Origin`
does not match the server host. Treat that as a property of browsers only — it constrains page
JavaScript, and does nothing about a client that reaches the port directly, which is what the proxy
above is for. `LIGHTER_CORS_ALLOWED_ORIGINS` lifts the refusal for origins you list, so every page
served from a listed origin can then submit applications on a visiting browser's behalf. Leave it
empty unless you serve the UI from another origin.

## Restrict the Python gateway

Port `25333` is a Py4J gateway. Spark drivers running interactive sessions connect back to it
to fetch statements and return results. Py4J gives its clients reflective access to the server
JVM, so anything that can open a TCP connection to this port can execute code inside the
Lighter process, with Lighter's own filesystem, environment and cluster credentials.

Expose it only to the network the Spark drivers run in — a `NetworkPolicy` restricted to the
Spark namespace on Kubernetes, or a firewall rule limited to the YARN node managers. It must
never be published to the internet or to a general-purpose office network. The `-p 25333:25333`
in [Installing Lighter on Docker](./docker.md) publishes it on all interfaces of the host and is
meant for a machine that is already isolated.

Set `LIGHTER_PY_GATEWAY_AUTH_TOKEN` as well. It makes the gateway require a shared secret, which
Lighter passes to session drivers so they can present it. It is unset by default, and Lighter logs
a warning at startup while it is. On Kubernetes, generate one into a `Secret` and reference it
rather than writing it into the `Deployment`:

```bash
kubectl -n spark create secret generic lighter-gateway \
  --from-literal=token="$(openssl rand -hex 24)"
```

```yaml
env:
  -   name: LIGHTER_PY_GATEWAY_AUTH_TOKEN
      valueFrom:
          secretKeyRef:
              name: lighter-gateway
              key: token
              optional: true
```

Three things to know before enabling it:

- Every replica must be given the same value, because a driver reaches whichever replica the
  Service sends it to.
- Changing the value rejects sessions that are already running, since their driver still presents
  the old one. Permanent sessions are re-provisioned automatically; ad-hoc sessions are lost. Set
  or rotate it when no session matters, and avoid tooling that regenerates the secret on every
  deploy.
- The token reaches the driver as an environment variable, so anyone able to read the driver's pod
  spec or YARN application environment can read it. It closes the port to clients without that
  access; it does not replace the network restriction above.

## Limit what a submitted application can do

Every caller controls the code that runs and most of the Spark properties it runs with, so the
identity that code gets is the real boundary:

- Keep the Spark driver's service account (`LIGHTER_KUBERNETES_SERVICE_ACCOUNT`) separate from
  Lighter's own, and grant it no Kubernetes API permissions unless the applications need them.
- Run applications in a dedicated namespace. `LIGHTER_KUBERNETES_NAMESPACE`,
  `LIGHTER_KUBERNETES_SERVICE_ACCOUNT` and the Kubernetes master are applied after the submitted
  `conf` and cannot be overridden by a caller.
- The driver and executor pod template files *can* be overridden by a caller, as can the
  container image, so do not rely on the templates in [`k8s/`](../k8s) to constrain pods. Enforce
  that with Pod Security Admission or an admission webhook instead.
- Scope cloud credentials to the workload rather than the node. Anything reachable from the
  driver pod — instance metadata, mounted secrets, IRSA or workload identity tokens — is
  reachable by every caller.
- The `Role` in [`quickstart/lighter.yml`](../quickstart/lighter.yml) grants `*` on pods,
  services and configmaps to Lighter itself. Narrow it before using the quickstart as a
  production base.

## Note on the local backend

With both `LIGHTER_KUBERNETES_ENABLED` and `LIGHTER_YARN_ENABLED` unset or `false`, Lighter falls
back to the local backend: it runs `spark-submit` as a child process in `local[*]` mode inside its
own container. Submitted code then runs as the Lighter process, with Lighter's service account,
cluster credentials and access to the Python gateway on localhost — including the gateway token,
which is handed to the process it launches. This backend is intended for development and testing;
deployments that serve real users should enable a cluster backend.
