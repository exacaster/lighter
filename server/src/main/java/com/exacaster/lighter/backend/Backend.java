package com.exacaster.lighter.backend;

import com.exacaster.lighter.batch.Batch;
import com.exacaster.lighter.spark.SubmitParams;
import java.util.Map;

public interface Backend {

    void configure(Map<String, String> configs);


    SubmitParams getSubmitParamas(Batch batch);
}
