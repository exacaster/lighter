package com.exacaster.lighter.backend;

import com.exacaster.lighter.spark.SubmitParams;
import com.exacaster.lighter.storage.Entity;
import io.soabase.recordbuilder.core.RecordBuilder;

@RecordBuilder
public record Application(String id, ApplicationType type, ApplicationState state, String appId, String appInfo, SubmitParams submitParams) implements Entity {

}
