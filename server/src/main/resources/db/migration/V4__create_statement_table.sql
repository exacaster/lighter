create table application_statement(
    id varchar not null primary key,
    application_id varchar not null references application(id) on delete cascade,
    state varchar not null,
    code text,
    output text,
    created_at timestamp not null
);

create unique index application_statement_idx on application_statement(application_id);
