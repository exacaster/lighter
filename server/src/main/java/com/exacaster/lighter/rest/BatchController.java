package com.exacaster.lighter.rest;

import com.exacaster.lighter.batch.Batch;
import com.exacaster.lighter.batch.BatchList;
import com.exacaster.lighter.batch.BatchService;
import com.exacaster.lighter.log.Log;
import com.exacaster.lighter.log.LogService;
import com.exacaster.lighter.spark.SubmitParams;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.validation.Validated;
import javax.validation.Valid;

@Validated
@Controller("/api/batches")
public class BatchController {

    private final BatchService batchService;
    private final LogService logService;

    public BatchController(BatchService batchService, LogService logService) {
        this.batchService = batchService;
        this.logService = logService;
    }

    @Post
    public Batch create(@Valid @Body SubmitParams batch) {
        return batchService.create(batch);
    }

    @Get
    public BatchList get(@QueryValue(defaultValue = "0") Integer from, @QueryValue(defaultValue = "100") Integer size) {
        var batches = batchService.fetch(from, size);
        return new BatchList(from, batches.size(), batches);
    }

    @Get("/{id}")
    public Batch get(@PathVariable String id) {
        return batchService.fetchOne(id);
    }

    @Delete("/{id}")
    public void delete(@PathVariable String id) {
        batchService.deleteOne(id);
    }

    // For backwards copatibility with livy
    @Get("/{id}/state")
    public Batch getState(@PathVariable String id) {
        return batchService.fetchOne(id);
    }

    @Get("/{id}/log")
    public Log getLog(@PathVariable Long id, @QueryValue(defaultValue = "0") Integer from, @QueryValue(defaultValue = "100") Integer size) {
        return logService.fetch("batch", id, from, size);
    }
}
