create table t_inventory
(
    id         uuid primary key,
    product_id uuid    not null,
    quantity   integer not null
)