package com.exacaster.lighter.batch;

import com.exacaster.lighter.backend.ApplicationState;

public record LaunchResult(ApplicationState state, Exception exception) {

}
