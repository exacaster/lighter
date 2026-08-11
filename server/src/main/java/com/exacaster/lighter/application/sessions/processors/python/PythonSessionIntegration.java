package com.exacaster.lighter.application.sessions.processors.python;

import static org.slf4j.LoggerFactory.getLogger;

import com.exacaster.lighter.application.Application;
import com.exacaster.lighter.application.sessions.Statement;
import com.exacaster.lighter.application.sessions.processors.Output;
import com.exacaster.lighter.application.sessions.processors.StatementHandler;
import com.exacaster.lighter.configuration.AppConfiguration;
import com.exacaster.lighter.storage.StatementStorage;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.scheduling.annotation.Async;
import jakarta.inject.Singleton;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import py4j.GatewayServer;

@Singleton
public class PythonSessionIntegration implements StatementHandler {

    private static final Logger LOG = getLogger(PythonSessionIntegration.class);

    private final Integer gatewayPort;
    private final String gatewayAuthToken;
    private final StatementStorage statementStorage;

    public PythonSessionIntegration(AppConfiguration conf, StatementStorage statementStorage) {
        this.gatewayPort = conf.getPyGatewayPort();
        this.gatewayAuthToken = conf.hasPyGatewayAuthToken() ? conf.getPyGatewayAuthToken() : null;
        this.statementStorage = statementStorage;
    }

    // Used By Py4J
    public List<Statement> statementsToProcess(String id) {
        var statementQueue = statementStorage.findByState(id, "waiting");
        if (!statementQueue.isEmpty()) {
            LOG.info("Waiting: {}", statementQueue);
        }
        return statementQueue;
    }

    // Used By Py4J
    public void handleResponse(String sessionId, String statementId, Map<String, Object> result) {
        LOG.warn("Handling response for {} : {} --- {}", sessionId, statementId, result);
        var error = string(result.get("error"));
        var outputStatus = error != null ? "error" : "ok";
        var output = new Output(
                outputStatus,
                1,
                (Map<String, Object>) result.get("content"),
                error,
                string(result.get("traceback"))
        );
        var status = error != null ? "error" : "available";
        statementStorage.update(sessionId, statementId, status, output);
    }

    private String string(Object obj) {
        return obj != null ? String.valueOf(obj) : null;
    }

    @Override
    public Statement processStatement(String id, Statement statement) {
        return statementStorage.create(id, statement);
    }

    @Override
    public Statement getStatement(String id, String statementId) {
        var sessionStatements = statementStorage.find(id);
        if (sessionStatements != null) {
            return sessionStatements.stream()
                    .filter(st -> st.getId().equals(statementId))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    @Override
    public Optional<Statement> cancelStatement(String id, String statementId) {
        return statementStorage.updateState(id, statementId, "cancelled");
    }

    @Transactional
    @Override
    public void cancelStatements(String id) {
        statementStorage.findByState(id, "waiting").forEach(statement -> {
            cancelStatement(id, statement.getId());
        });
    }

    @EventListener
    @Async
    public void runServer(StartupEvent event) {
        var builder = new GatewayServer.GatewayServerBuilder(this)
                .javaAddress(new InetSocketAddress(0).getAddress())
                .javaPort(gatewayPort);
        if (gatewayAuthToken != null) {
            builder.authToken(gatewayAuthToken);
        } else {
            LOG.warn("Python gateway on port {} accepts unauthenticated connections. Py4J exposes this "
                    + "JVM to its clients, so anything able to reach the port can run code as Lighter. "
                    + "Set lighter.py-gateway-auth-token and keep the port reachable only from the "
                    + "Spark cluster.", gatewayPort);
        }
        builder.build().start();
    }

    @Override
    public boolean hasWaitingStatement(Application application) {
        var waitingStatements = statementStorage.findByState(application.getId(), "waiting");
        return !waitingStatements.isEmpty();
    }
}
