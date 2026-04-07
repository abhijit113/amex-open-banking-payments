create table if not exists payment_intent_event (
    event_id varchar(128) primary key,
    payment_intent_id varchar(64) not null,
    external_provider_payment_id varchar(128) not null,
    event_type varchar(64) not null,
    provider_status varchar(64),
    payload text not null,
    created_at timestamp not null
);