package com.exacaster.lighter.storage.jdbc;

import com.exacaster.lighter.log.Log;
import com.exacaster.lighter.storage.LogStorage;
import io.micronaut.context.annotation.Requires;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import javax.inject.Singleton;
import javax.sql.DataSource;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

@Singleton
@Requires(beans = DataSource.class)
public class JdbcLogStorage implements LogStorage, RowMapper<Log> {

    private final Jdbi jdbi;

    public JdbcLogStorage(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public Optional<Log> findApplicationLog(String applicationId) {
        return jdbi.withHandle(handle -> handle
                .createQuery("SELECT * FROM application_log WHERE application_id=:id LIMIT 1")
                .bind("id", applicationId)
                .map(this)
                .stream().findFirst()
        );
    }

    @Override
    public Log saveApplicationLog(Log log) {
        return jdbi.withHandle(handle -> {

                    var updated = handle.createUpdate("UPDATE application_log SET log=:log WHERE application_id=:id")
                            .bind("log", log.getLog())
                            .bind("id", log.getId())
                            .execute();
                    // Not all SQL databases support ON CONFLICT syntax, so doing fallback if nothing updated
                    if (updated == 0) {
                        handle
                                .createCall(
                                        "INSERT INTO application_log (application_id, log) "
                                                + "VALUES (:id, :log)")
                                .bind("log", log.getLog())
                                .bind("id", log.getId())
                                .invoke();
                    }
                    return log;
                }
        );
    }

    @Override
    public Log map(ResultSet rs, StatementContext ctx) throws SQLException {
        return new Log(rs.getString("application_id"), rs.getString("log"));
    }
}
