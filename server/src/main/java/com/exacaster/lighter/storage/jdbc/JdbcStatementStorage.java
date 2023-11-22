package com.exacaster.lighter.storage.jdbc;

import com.exacaster.lighter.application.sessions.Statement;
import com.exacaster.lighter.application.sessions.processors.Output;
import com.exacaster.lighter.storage.StatementStorage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import javax.transaction.Transactional;

import javax.sql.DataSource;

@Singleton
@Requires(beans = DataSource.class)
public class JdbcStatementStorage implements StatementStorage, RowMapper<Statement> {

    private final Jdbi jdbi;
    private final ObjectMapper objectMapper;

    public JdbcStatementStorage(Jdbi jdbi, ObjectMapper objectMapper) {
        this.jdbi = jdbi;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public List<Statement> find(String sessionId) {
        return jdbi.withHandle(handle -> handle
                .createQuery("SELECT * FROM application_statement WHERE application_id=:application_id ORDER BY created_at ASC")
                .bind("application_id", sessionId)
                .map(this)
                .list());
    }

    @Override
    @Transactional
    public List<Statement> findByState(String sessionId, String state) {
        return jdbi.withHandle(handle -> handle
                .createQuery("SELECT * FROM application_statement WHERE application_id=:application_id AND state=:state ORDER BY created_at ASC")
                .bind("application_id", sessionId)
                .bind("state", state)
                .map(this)
                .list());
    }

    @Override
    @Transactional
    public Optional<Statement> findLatest(String sessionId) {
        return jdbi.withHandle(handle -> handle
                .createQuery("SELECT * FROM application_statement WHERE application_id=:application_id ORDER BY created_at DESC LIMIT 1")
                .bind("application_id", sessionId)
                .map(this)
                .findOne());

    }

    @Override
    @Transactional
    public Optional<Statement> updateState(String sessionId, String statementId, String state) {
        return jdbi.withHandle(handle -> {
            handle.createUpdate("UPDATE application_statement SET state=:state WHERE application_id=:application_id AND id=:id")
                    .bind("state", state)
                    .bind("application_id", sessionId)
                    .bind("id", statementId)
                    .execute();
            return findOne(sessionId, statementId);
        });
    }

    @Override
    @Transactional
    public void update(String sessionId, String statementId, String state, Output output) {
        jdbi.withHandle(handle -> handle.createUpdate("UPDATE application_statement SET state=:state, output=:output WHERE application_id=:application_id AND id=:id")
                .bind("application_id", sessionId)
                .bind("id", statementId)
                .bind("state", state)
                .bind("output", outputToString(output))
                .execute());
    }

    @Override
    @Transactional
    public Statement create(String sessionId, Statement statement) {
        var id = UUID.randomUUID().toString();
        return jdbi.withHandle(handle -> {
            handle.createCall("INSERT INTO application_statement"
                            + "(id, application_id, state, code, created_at) VALUES "
                            + "(:id, :application_id, :state, :code, :created_at)")
                    .bind("id", id)
                    .bind("application_id", sessionId)
                    .bind("state", "waiting")
                    .bind("code", statement.getCode())
                    .bind("created_at", Timestamp.valueOf(LocalDateTime.now()))
                    .invoke();
            return findOne(sessionId, id).get();
        });
    }

    private Optional<Statement> findOne(String sessionId, String statementId) {
        return jdbi.withHandle(handle -> handle
                .createQuery("SELECT * FROM application_statement WHERE application_id=:application_id AND id=:id LIMIT 1")
                .bind("application_id", sessionId)
                .bind("id", statementId)
                .map(this)
                .findOne());
    }

    @Override
    public Statement map(ResultSet rs, StatementContext ctx) throws SQLException {
        var outputStr = rs.getString("output");
        Output output = null;
        if (outputStr != null) {
            try {
                output = objectMapper.readValue(outputStr, Output.class);
            } catch (JsonProcessingException e) {
                // ignore
            }
        }
        return new Statement(
                rs.getString("id"),
                rs.getString("code"),
                output,
                rs.getString("state"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }

    private String outputToString(Output output) {
        try {
            return objectMapper.writeValueAsString(output);
        } catch (JsonProcessingException e) {
            // ignore
            e.printStackTrace();
            return null;
        }
    }
}
