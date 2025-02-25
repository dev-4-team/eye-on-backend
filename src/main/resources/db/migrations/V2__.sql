ALTER TABLE participants_verifications
    ADD CONSTRAINT uc_participants_verifications_protest UNIQUE (protest_id);

ALTER TABLE protest_location_mappings
    ADD CONSTRAINT unique_protest_location_id UNIQUE (protest_id, location_id);

ALTER TABLE organizers
    ADD title VARCHAR(255);

ALTER TABLE organizers
    ALTER COLUMN description DROP NOT NULL;