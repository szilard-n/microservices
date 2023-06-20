CREATE TABLE t_products
(
    id          uuid primary key,
    seller_id   uuid    not null,
    name        varchar not null,
    description varchar not null,
    price       float   not null,

    unique (id, seller_id)
);