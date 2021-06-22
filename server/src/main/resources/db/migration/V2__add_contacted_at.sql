alter table application add column contacted_at timestamp;
create index application_contacted_at_idx on application(contacted_at);
