CREATE TABLE user_details
(
    id    BIGINT NOT NULL,
    name  VARCHAR(255),
    email VARCHAR(255),
    CONSTRAINT pk_user_details PRIMARY KEY (id)
);

CREATE SEQUENCE IF NOT EXISTS user_details_seq START WITH 1 INCREMENT BY 50;