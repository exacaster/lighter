# How Lighter works

The idea idea is based off [Apache Livy](https://livy.incubator.apache.org/). Lighter supports two types of Spark applications: batch and live sessions.

## Batch applications

Client submits batch application by using REST API. Lighter saves provided application to its internal storage for later execution. Execution process periodically checks for new applications and submits them to configured backend (YARN or K8s). Tracking process periodicly checks the status of running applications and syncs it to Lighter internal storage.

Simplified ilustration of the workflow:

```
                            ┌─────────────────────────────────────────────────────────────────────────────────┐
                            │ Lighter                                                                         │
                            │                              ┌─────────────────┐                                │
                            │                 Store app    │                 │                                │
                            │                ┌────────────►│   Internal      │   Sync status                  │
                            │                │ Check status│   storage       │◄───────────────┐               │
                            │                │   ┌────────►│                 │                │               │
                            │                │   │         └─────┬───────────┘                │               │
                            │                │   │               │ Get new apps               │               │
                            │                │   │               │                            │               │
┌──────────────┐  Submit    │        ┌───────┴───┴────┐          ▼                            ▼               │
│              ├────────────┴───────►│                │    ┌────────────────┐      ┌─────────────────┐        │
│    Client    │  Check status       │    REST API    │    │                │      │                 │        │
│              │◄───────────┬───────►│                │    │  App Executor  │      │  Status Tracker │        │
└──────────────┘            │        └────────────────┘    │                │      │                 │        │
                            │                              │                │      │                 │        │
                            │                              └─────┬──────────┘      └──────────┬──────┘        │
                            │                                    │                            │               │
                            │                                    │                            │               │
                            │                            Execute │    ┌──────────────────┐    │Get status     │
                            │                                    │    │                  │    │               │
                            │                                    │    │   Backend        │    │               │
                            │                                    └───►│   (YARN/K8s)     │◄───┘               │
                            │                                         │                  │                    │
                            │                                         └──────────────────┘                    │
                            │                                                                                 │
                            │                                                                                 │
                            │                                                                                 │
                            │                                                                                 │
                            └─────────────────────────────────────────────────────────────────────────────────┘
```

## Live sessions

Live sessions works in a similar way. The mayn difference is, that when starting a live session, Lighter submits a special PySpark application, which contains infinite loop and accepts command statements from Lighter through Py4J Gateway.
