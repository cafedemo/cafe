create sequence if not exists customer_seq no maxvalue start with 1 increment by 1;

create table if not exists customer (
    id integer primary key default nextval('customer_seq'),
    name varchar(255) not null,
    email varchar(255) not null,
    unique (email)
);

