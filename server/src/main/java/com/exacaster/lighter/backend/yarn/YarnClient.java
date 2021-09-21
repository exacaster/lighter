package com.exacaster.lighter.backend.yarn;

import static io.micronaut.http.HttpHeaders.ACCEPT;

import com.exacaster.lighter.backend.yarn.resources.State;
import com.exacaster.lighter.backend.yarn.resources.YarnApplicationListResponse;
import com.exacaster.lighter.backend.yarn.resources.YarnApplicationResponse;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.client.annotation.Client;

@Requires(beans = YarnProperties.class)
@Client("${lighter.yarn.url}/ws/v1/cluster/")
@Header(name = ACCEPT, value = "application/json")
public interface YarnClient {

    @Put("/apps/{appId}/state")
    State setState(String appId, @Body State state);

    @Get("/apps/{appId}")
    YarnApplicationResponse getApplication(String appId);

    @Get("/apps{?applicationTags}")
    YarnApplicationListResponse getApps(@QueryValue String applicationTags);
}
