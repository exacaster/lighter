package com.exacaster.lighter.application.sessions;

import static org.slf4j.LoggerFactory.getLogger;

import com.exacaster.lighter.application.Application;
import com.exacaster.lighter.application.ApplicationBuilder;
import com.exacaster.lighter.application.ApplicationInfo;
import com.exacaster.lighter.application.ApplicationState;
import com.exacaster.lighter.backend.Backend;
import com.exacaster.lighter.log.Log;
import com.exacaster.lighter.log.LogService;
import com.exacaster.lighter.spark.SparkApp;
import io.micronaut.scheduling.annotation.Scheduled;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Consumer;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import py4j.GatewayServer.GatewayServerBuilder;

@Singleton
public class SessionHandler {
    private static final Logger LOG = getLogger(SessionHandler.class);

    private final SessionService sessionService;
    private final Backend backend;
    private final LogService logService;

    public SessionHandler(SessionService sessionService, Backend backend, LogService logService) {
        this.sessionService = sessionService;
        this.backend = backend;
        this.logService = logService;
    }

    public void launch(Application application, Consumer<Throwable> errorHandler) {
        var app = new SparkApp(application.getSubmitParams(), errorHandler);
        app.launch(backend.getSubmitConfiguration(application));
    }


    @Scheduled(fixedRate = "1m")
    @Transactional
    public void processScheduledBatches() {
        sessionService.fetchByState(ApplicationState.NOT_STARTED, 10)
                .forEach(batch -> {
                    LOG.info("Launching {}", batch);
                    sessionService.update(ApplicationBuilder.builder(batch)
                            .setState(ApplicationState.STARTING)
                            .setContactedAt(LocalDateTime.now())
                            .build());


                    var secret = UUID.randomUUID().toString();
                    var server = new GatewayServerBuilder()
                            .authToken(secret)
                            .javaPort(0)
                            .javaAddress(InetAddress.getLoopbackAddress())
                            .build();
                    server.start();


                    launch(batch, error -> {
                        var appId = backend.getInfo(batch).map(ApplicationInfo::getApplicationId)
                                .orElse(null);
                        sessionService.update(
                                ApplicationBuilder.builder(batch)
                                        .setState(ApplicationState.ERROR)
                                        .setAppId(appId)
                                        .setContactedAt(LocalDateTime.now())
                                        .build());

                        backend.getLogs(batch).ifPresentOrElse(
                                logService::save,
                                () -> logService.save(new Log(batch.getId(), error.toString()))
                        );
                    });
                });
    }
}
