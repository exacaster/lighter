package com.exacaster.lighter.application.sessions.processors.python;

import static org.slf4j.LoggerFactory.getLogger;

import com.exacaster.lighter.application.Application;
import com.exacaster.lighter.application.sessions.Statement;
import com.exacaster.lighter.application.sessions.processors.Output;
import com.exacaster.lighter.application.sessions.processors.StatementStatusChecker;
import com.exacaster.lighter.configuration.AppConfiguration;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.scheduling.annotation.Async;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import org.slf4j.Logger;
import py4j.GatewayServer;

@Singleton
public class SessionIntegration implements StatementStatusChecker {

    private static final Logger LOG = getLogger(SessionIntegration.class);

    private final Map<String, List<Statement>> statements = new HashMap<>();
    private final Integer gatewayPort;

    public SessionIntegration(AppConfiguration conf) {
        this.gatewayPort = conf.getPyGatewayPort();
    }

    // Used By Py4J
    public List<Statement> statementsToProcess(String id) {
        var result = statements.get(id);
        if (result == null) {
            return List.of();
        }
        return result.stream().filter(statement -> statement.getState().equals("waiting")).collect(Collectors.toList());
    }

    // Used By Py4J
    public void handleResponse(String sessionId, String statementId, Map<String, Object> result) {
        LOG.debug("Handling response for {}:{} -- {}", sessionId, statementId, result);
        var sessionStatements = statementsToProcess(sessionId);
        sessionStatements.stream()
                .filter(st -> statementId.equals(st.getId()))
                .findFirst()
                .ifPresent(st ->{
                    var index = sessionStatements.indexOf(st);
                    var error = result.get("error");
                    var status = error != null ? "error" : "available";
                    var outputStatus = error != null ? "error" : "ok";
                    var output = new Output(outputStatus, 1, (Map<String, Object>) result.get("content"));
                    var newSt = st.withStateAndOutput(status, output);
                    sessionStatements.set(index, newSt);
                });
    }

    public Statement processStatement(String id, Statement statement) {
        // cleanup statements, keep only the last one.
        var sessionStatements = new ArrayList<Statement>();
        var newStatement = statement.withIdAndState(UUID.randomUUID().toString(), "waiting");
        sessionStatements.add(newStatement);
        statements.put(id, sessionStatements);

        return newStatement;
    }

    public Statement getStatement(String id, String statementId) {
        var sessionStatements = statements.get(id);
        if (sessionStatements != null) {
            return sessionStatements.stream()
                    .filter(st -> st.getId().equals(statementId))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    public Statement cancelStatement(String id, String statementId) {
        // TODO: Not sure what to do. Send interrupt?
        return null;
    }

    @EventListener
    @Async
    public void runServer(StartupEvent event) {
        var server = new GatewayServer.GatewayServerBuilder(this)
                .javaAddress(new InetSocketAddress(0).getAddress())
                .javaPort(gatewayPort)
                .build();
        server.start();
    }

    @Override
    public boolean hasWaitingStatement(Application application) {
        var appStatements = statements.get(application.getAppId());
        if (appStatements == null) {
            return false;
        }
        return appStatements.stream().allMatch(st -> "waiting".equals(st.getState()));
    }
}
