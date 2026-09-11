create table outbox_events (id uuid primary key, topic varchar(200) not null, event_key varchar(200) not null, payload text not null, created_at timestamp with time zone not null, published_at timestamp with time zone);
create index idx_outbox_unpublished on outbox_events(published_at,created_at);
