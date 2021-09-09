package com.exacaster.lighter;

import io.micronaut.context.annotation.Value;
import io.micronaut.core.io.ResourceResolver;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.server.types.files.StreamedFile;
import java.util.Optional;

@Controller("/lighter")
public class IndexController {

    private final ResourceResolver res;
    private final String path;

    public IndexController(ResourceResolver res, @Value("${lighter.frontend-path}") String path) {
        this.res = res;
        this.path = path;
    }

    @Get(value = "/{+path*}", consumes = MediaType.ALL)
    public Optional<StreamedFile> forward(Optional<String> path) {
        var strPath = path.filter(p -> p.contains(".")).orElse("index.html");
        return res.getResource(this.path + "/" + strPath)
                .map(StreamedFile::new);
    }

    @Get(value = "/jobs/shell_wrapper.py", consumes = MediaType.ALL)
    public Optional<StreamedFile> job() {
        return res.getResource("classpath:shell_wrapper.py")
                .map(StreamedFile::new);
    }

    @Get(consumes = MediaType.ALL)
    public Optional<StreamedFile> forward() {
        return forward(Optional.empty());
    }
}