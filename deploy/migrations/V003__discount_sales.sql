CREATE TABLE discount_sales (
    operation_key uuid PRIMARY KEY,
    source_id bigint NOT NULL,
    person_id bigint NOT NULL,
    discount real NOT NULL,
    response text
);
