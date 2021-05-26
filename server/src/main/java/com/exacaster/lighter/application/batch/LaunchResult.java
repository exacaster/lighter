package com.exacaster.lighter.application.batch;

import com.exacaster.lighter.application.ApplicationState;

public record LaunchResult(ApplicationState state, Exception exception) {

}
