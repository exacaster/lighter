package com.exacaster.lighter.rest;

import com.exacaster.lighter.application.Application;
import com.exacaster.lighter.application.ApplicationList;
import com.exacaster.lighter.application.ApplicationState;
import com.exacaster.lighter.application.batch.BatchService;
import com.exacaster.lighter.log.Log;
import com.exacaster.lighter.log.LogService;
import com.exacaster.lighter.application.SubmitParams;
import com.exacaster.lighter.storage.SortOrder;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.validation.Validated;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;

import java.util.Optional;
import jakarta.validation.Valid;

@Tags(@Tag(name = "Batches"))
@Validated
@Controller("/lighter/api/batches")
public class BatchController {

    private final BatchService batchService;
    private final LogService logService;

    public BatchController(BatchService batchService, LogService logService) {
        this.batchService = batchService;
        this.logService = logService;
    }

    @Post
    public Application create(@Valid @Body SubmitParams batch) {
        return batchService.create(batch).withRedactedConf();
    }

    @Get
    public ApplicationList get(@QueryValue(defaultValue = "0") Integer from,
            @QueryValue(defaultValue = "100") Integer size,
            @Nullable @QueryValue String state) {
        var batches = ApplicationState.from(state)
                .map(st -> batchService.fetchByState(st, SortOrder.DESC, from, size))
                .orElseGet(() -> batchService.fetch(from, size))
                .stream().map(Application::withRedactedConf)
                .toList();
        return new ApplicationList(from, batches.size(), batches);
    }

    @Get("/{id}")
    public Optional<Application> get(@PathVariable String id) {
        return batchService.fetchOne(id).map(Application::withRedactedConf);
    }

    @Delete("/{id}")
    public void delete(@PathVariable String id) {
        batchService.deleteOne(id);
    }

    // For backwards compatibility with livy
    @Get("/{id}/state")
    public Optional<Application> getState(@PathVariable String id) {
        return batchService.fetchOne(id).map(Application::withRedactedConf);
    }

    @Get("/{id}/log")
    public Optional<Log> getLog(@PathVariable String id) {
        return batchService.fetchOne(id).flatMap(batch -> {
            if (batch.getState().isComplete()) {
                return logService.fetch(id);
            }

            return logService.fetchLive(batch);
        });
    }
}
