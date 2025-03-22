CREATE TABLE protest_cheer_count
(
    protest_id  BIGINT  NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE,
    updated_at  TIMESTAMP WITH TIME ZONE,
    cheer_count INTEGER NOT NULL,
    CONSTRAINT pk_protestCheerCount PRIMARY KEY (protest_id)
);