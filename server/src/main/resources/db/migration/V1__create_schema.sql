create table application(
    id varchar not null primary key,
    type varchar not null,
    state varchar not null,
    app_id varchar,
    app_info varchar,
    submit_params text not null,
    created_at timestamp not null
);

create index application_type_state_idx on application(type, state);
create index application_created_at_idx on application(created_at);

create table application_log(
    application_id varchar not null references application(id) on delete cascade,
    log text not null
);

create unique index application_log_idx on application_log(application_id);
