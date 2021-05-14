package com.exacaster.lighter.batch;

public enum BatchState {
    not_started,
    starting,
    idle,
    busy,
    shutting_down,
    error,
    dead,
    killed,
    success
}
