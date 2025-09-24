CREATE TABLE person.countries
(
    id      SERIAL PRIMARY KEY,
    active  boolean                     NOT NULL DEFAULT TRUE,
    created TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    name    VARCHAR(128)                NOT NULL,
    code    VARCHAR(3)                  NOT NULL
);

CREATE TABLE person.addresses
(
    id         UUID PRIMARY KEY                     DEFAULT uuid_generate_v4(),
    active     boolean                     NOT NULL DEFAULT TRUE,
    created    TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated    TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    country_id INTEGER                     NOT NULL REFERENCES person.countries (id),
    address    VARCHAR(128)                NOT NULL,
    zip_code   VARCHAR(32)                 NOT NULL,
    city       VARCHAR(64)                 NOT NULL

);

CREATE TABLE person.users
(
    id         UUID PRIMARY KEY                     DEFAULT uuid_generate_v4(),
    active     boolean                     NOT NULL DEFAULT TRUE,
    created    TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated    TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    email      VARCHAR(1024)               NOT NULL,
    first_name VARCHAR(64)                 NOT NULL,
    last_name  VARCHAR(64)                 NOT NULL,
    address_id UUID                        NOT NULL REFERENCES person.addresses (id)
);

CREATE TABLE person.individuals
(
    id              UUID PRIMARY KEY                     DEFAULT uuid_generate_v4(),
    active          boolean                     NOT NULL DEFAULT TRUE,
    created         TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated         TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    passport_number VARCHAR(64)                 NOT NULL,
    phone_number    VARCHAR(64)                 NOT NULL,
    user_id         UUID                        NOT NULL REFERENCES person.users (id)
);
