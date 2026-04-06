create table if not exists payment_intent (
    id varchar(64) primary key,
    trace_id varchar(64) not null,
    end_to_end_id varchar(64) not null,
    amount_in_minor bigint not null,
    currency varchar(3) not null,
    status varchar(32) not null,
    provider varchar(32),
    external_provider_payment_id varchar(128),
    external_bank_trace_id varchar(128),
    redirect_url text,
    created_at timestamp not null,
    updated_at timestamp not null
);