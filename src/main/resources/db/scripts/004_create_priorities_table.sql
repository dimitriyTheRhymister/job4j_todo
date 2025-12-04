-- liquibase formatted sql

-- changeset author:todo id:004_create_priorities_table
CREATE TABLE priorities (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    position INTEGER NOT NULL
);

INSERT INTO priorities (name, position) VALUES ('urgently', 1);
INSERT INTO priorities (name, position) VALUES ('normal', 2);
-- rollback DROP TABLE priorities;