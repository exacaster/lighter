package com.exacaster.lighter.storage.jdbc;

import com.exacaster.lighter.application.Application;
import com.exacaster.lighter.application.ApplicationBuilder;
import com.exacaster.lighter.application.ApplicationState;
import com.exacaster.lighter.application.ApplicationType;
import com.exacaster.lighter.application.SubmitParams;
import com.exacaster.lighter.storage.ApplicationStorage;
import com.exacaster.lighter.storage.ApplicationAlreadyExistsException;
import com.exacaster.lighter.storage.SortOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.slf4j.Logger;

import javax.sql.DataSource;
import javax.transaction.Transactional;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

@Singleton
@Requires(beans = DataSource.class)
public class JdbcApplicationStorage implements ApplicationStorage, RowMapper<Application> {
    private static final Logger LOG = getLogger(JdbcApplicationStorage.class);

    private final Jdbi jdbi;
    private final ObjectMapper objectMapper;

    public JdbcApplicationStorage(Jdbi jdbi, ObjectMapper objectMapper) {
        this.jdbi = jdbi;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public Optional<Application> findApplication(
            String internalApplicationId) {
        return jdbi.withHandle(handle -> handle
                .createQuery("SELECT * FROM application WHERE id=:id and deleted = false LIMIT 1")
                .bind("id", internalApplicationId)
                .map(this)
                .stream().findFirst()
        );
    }

    @Override
    @Transactional
    public List<Application> findApplications(EnumSet<ApplicationType> types,
                                              Integer from, Integer size) {
        return jdbi.withHandle(handle -> handle
                .createQuery(
                        "SELECT * FROM application WHERE type IN (<types>) and deleted = false ORDER BY created_at DESC LIMIT :limit OFFSET :from")
                .bindList("types", types.stream().map(ApplicationType::name).collect(Collectors.toList()))
                .bind("from", from)
                .bind("limit", size)
                .map(this)
                .list()
        );
    }

    @Override
    @Transactional
    public void deleteApplication(String internalApplicationId) {
        jdbi.withHandle(handle -> handle.createCall("UPDATE application set deleted = true WHERE id=:id")
                .bind("id", internalApplicationId).invoke());
    }

    @Override
    @Transactional
    public Application saveApplication(Application application) {
        return jdbi.withHandle(handle -> {
                    final var updated = update(handle, application);
                    // Not all SQL databases support ON CONFLICT syntax, so doing fallback if nothing updated
                    if (updated == 0) {
                        insert(handle, application);
                    }
                    return application;
                }
        );
    }

    @Override
    @Transactional
    public Application insertApplication(Application application) {
        try {
            return jdbi.withHandle(handle -> {
                insert(handle, application);
                return application;
            });
        } catch (UnableToExecuteStatementException e) {
            //can not make it better till https://github.com/jdbi/jdbi/issues/566 is implemented
            throw new ApplicationAlreadyExistsException(application.getId());
        }

    }

    private static int update(Handle handle, Application application) {
        var updated = handle.createUpdate("UPDATE application SET "
                        + "app_id=:app_id, "
                        + "app_info=:app_info, "
                        + "state=:state, "
                        + "contacted_at=:contacted_at WHERE id=:id")
                .bind("state", application.getState().name())
                .bind("app_id", application.getAppId())
                .bind("app_info", application.getAppInfo())
                .bind("contacted_at", application.getContactedAt())
                .bind("id", application.getId())
                .execute();
        return updated;
    }

    private void insert(Handle handle, Application application) {
        String conf = null;
        try {
            conf = objectMapper.writeValueAsString(application.getSubmitParams());
        } catch (JsonProcessingException e) {
            LOG.warn("Failed serializing submit params", e);
        }
        handle
                .createCall(
                        "INSERT INTO application (id, type, state, app_id, app_info, submit_params, created_at, contacted_at, deleted) "
                                + "VALUES (:id, :type, :state, :app_id, :app_info, :submit_params, :created_at, :contacted_at, :deleted)")
                .bind("id", application.getId())
                .bind("type", application.getType().name())
                .bind("state", application.getState().name())
                .bind("app_id", application.getAppId())
                .bind("app_info", application.getAppInfo())
                .bind("submit_params", conf)
                .bind("created_at", application.getCreatedAt())
                .bind("contacted_at", application.getContactedAt())
                .bind("deleted", false)
                .invoke();
    }

    @Override
    @Transactional
    public List<Application> findApplicationsByStates(ApplicationType type,
                                                      List<ApplicationState> states, SortOrder order, Integer from, Integer size) {
        return jdbi.withHandle(handle -> handle
                .createQuery("SELECT * FROM application WHERE type=:type AND state IN (<states>) and deleted = false ORDER BY created_at "
                        + order.name() + " LIMIT :limit OFFSET :offset")
                .bind("type", type.name())
                .bindList("states", states.stream().map(ApplicationState::name).collect(Collectors.toList()))
                .bind("limit", size)
                .bind("offset", from)
                .map(this)
                .list()
        );
    }

    @Override
    @Transactional
    public List<Application> findAllApplications(ApplicationType type) {
        return jdbi.withHandle(handle -> handle
                .createQuery("SELECT * FROM application  WHERE type=:type")
                .bind("type", type.name())
                .map(this)
                .list());
    }

    @Override
    @Transactional
    public void hardDeleteApplication(String internalApplicationId) {
        jdbi.withHandle(handle -> handle.createCall("DELETE FROM application WHERE id=:id")
                .bind("id", internalApplicationId).invoke());
    }


    @Override
    public Application map(ResultSet rs, StatementContext ctx) throws SQLException {
        SubmitParams params = null;
        try {
            params = objectMapper.readValue(rs.getString("submit_params"), SubmitParams.class);
        } catch (JsonProcessingException e) {
            LOG.warn("Failed deserializing submit params", e);
        }

        var contactedAtTs = rs.getTimestamp("contacted_at");
        var contactedAt = contactedAtTs != null ? contactedAtTs.toLocalDateTime() : null;
        return ApplicationBuilder.builder()
                .setId(rs.getString("id"))
                .setType(ApplicationType.valueOf(rs.getString("type")))
                .setAppId(rs.getString("app_id"))
                .setState(ApplicationState.valueOf(rs.getString("state")))
                .setAppInfo(rs.getString("app_info"))
                .setSubmitParams(params)
                .setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime())
                .setContactedAt(contactedAt)
                .setDeleted(rs.getBoolean("deleted"))
                .build();
    }
}
