create table users (
    id              bigserial primary key,
    username        varchar(50) not null unique,
    email           varchar(255) not null unique,
    password_hash   varchar(255) not null,
    created_at      timestamp with time zone default now() not null,
    updated_at      timestamp with time zone default now() not null
);