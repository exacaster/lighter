package com.exacaster.lighter;

import io.micronaut.context.annotation.Value;
import io.micronaut.core.io.ResourceResolver;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.types.files.StreamedFile;
import java.util.Optional;

@Controller
public class IndexController {

    private final ResourceResolver res;
    private final String path;

    public IndexController(ResourceResolver res, @Value("${micronaut.router.static-resources.default.paths}") String path) {
        this.res = res;
        this.path = path;
    }


//    @Get("/{[path:[^\\.]*}")
//    @Produces(MediaType.TEXT_HTML)
//    public HttpResponse<?> refresh(HttpRequest<?> request) {
//        StreamedFile indexFile = new StreamedFile(res.getResource(path + "/index.html").get());
//        return HttpResponse.ok(indexFile);
//    }

    @Get(value = "/{path:[^\\.]*}", produces = MediaType.TEXT_HTML)
    public Optional<StreamedFile> forward(String path) {
        return res.getResource(this.path + "/index.html")
                .map(StreamedFile::new);
    }
}