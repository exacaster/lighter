package com.exacaster.lighter.batch;

import com.exacaster.lighter.spark.SubmitParams;
import com.exacaster.lighter.storage.Entity;
import io.soabase.recordbuilder.core.RecordBuilder;

@RecordBuilder
public record Batch(String id, String appId, String appInfo, BatchState state, SubmitParams submitParams) implements Entity {

}
