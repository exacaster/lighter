# Security Policy

## Reporting a vulnerability

Report suspected vulnerabilities privately through GitHub's
[private vulnerability reporting](https://github.com/exacaster/lighter/security/advisories/new).
Please do not open a public issue for an unfixed vulnerability.

A useful report contains the Lighter version, the backend in use (Kubernetes, YARN or local),
the request or configuration needed to trigger the issue, and what an attacker gains that
they would not have through normal use of the API.

## Trust model

Lighter is a job submission service. Its purpose is to accept application code and Spark
configuration over HTTP and run them on a cluster. It ships **no authentication and no
authorization**, and it applies no restrictions to the `conf` map or the application file
a caller supplies.

Any client able to reach the REST API can therefore:

- run arbitrary code on the cluster, with whatever identity the Spark driver is configured to use
- set almost any Spark property for that application, including properties that control the
  driver image, its environment and the interpreter used to run the submitted file
- read the logs and statement output of every application, and kill any of them

Any client able to reach the Python gateway port (`25333` by default) can additionally drive
the Py4J gateway that Lighter uses to talk to interactive sessions. Py4J exposes the server
JVM to its clients, so reaching that port grants code execution inside the Lighter process
itself, not inside a Spark driver.

Network access to Lighter is therefore equivalent to shell access on the Spark cluster, and
access to the gateway port is equivalent to shell access on the Lighter host. Both ports must
be reachable only from networks and clients you trust. See
[Securing a deployment](./docs/security.md) for the deployment side of this.

## Out of scope

Code execution reached by submitting an application through the API — for example a `POST` to
`/lighter/api/batches` that names an attacker-controlled Python file, or a statement posted to
an interactive session — is the documented behaviour of the service and is not treated as a
vulnerability. The same applies to arbitrary Spark properties in `conf` influencing how that
application runs.

Reports that go beyond this are in scope, including: bypassing an authenticating proxy placed
in front of Lighter, one caller reading or affecting another caller's applications in a way the
API does not otherwise allow, leaking Lighter's own credentials or configuration secrets
through the API, and injection or deserialization flaws in Lighter's own request handling.
