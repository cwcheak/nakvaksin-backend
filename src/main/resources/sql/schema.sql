CREATE TABLE IF NOT EXISTS nv_user (
    id bigserial NOT NULL PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    username VARCHAR(100) NOT NULL,
    display_name VARCHAR(200),
    phone_number VARCHAR(20),
    token VARCHAR(2048) NOT NULL,
    token_created_date TIMESTAMP WITHOUT TIME ZONE,
    token_last_keep_alive TIMESTAMP WITHOUT TIME ZONE
);

CREATE UNIQUE INDEX IF NOT EXISTS nv_user_1 ON nv_user (user_id);

CREATE TABLE IF NOT EXISTS nv_user_vaccination_status (
    id bigserial NOT NULL PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    vac_previous_json JSON,
    vac_previous_date TIMESTAMP WITHOUT TIME ZONE,
    vac_current_json JSON,
    vac_current_date TIMESTAMP WITHOUT TIME ZONE
);

CREATE UNIQUE INDEX IF NOT EXISTS nv_user_vaccination_status_1 ON nv_user_vaccination_status (user_id);

CREATE TABLE IF NOT EXISTS nv_subscription (
    id bigserial NOT NULL PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    is_opt_in BOOLEAN DEFAULT FALSE,
    is_opt_in_family BOOLEAN DEFAULT FALSE,
    family_phone_number VARCHAR(20),
    created_date TIMESTAMP WITHOUT TIME ZONE,
    last_modified_date TIMESTAMP WITHOUT TIME ZONE
);

CREATE UNIQUE INDEX IF NOT EXISTS nv_subscription_1 ON nv_subscription (user_id);
