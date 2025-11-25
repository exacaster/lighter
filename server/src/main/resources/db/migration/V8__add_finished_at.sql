alter table application add column finished_at timestamp;
create index application_finished_at_idx on application(finished_at);
