package com.exacaster.lighter.batch;

import com.exacaster.lighter.storage.Entity;

public record BatchData(String id, String appId, String appInfo, BatchState state, BatchConfiguration batch) implements Entity {

}
