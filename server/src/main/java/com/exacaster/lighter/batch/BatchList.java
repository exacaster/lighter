package com.exacaster.lighter.batch;

import java.util.List;

public record BatchList(Integer from, Integer total, List<Batch> batches) {

}
