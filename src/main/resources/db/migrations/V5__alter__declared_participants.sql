ALTER TABLE protests
    DROP CONSTRAINT protests_declared_participants_check;

ALTER TABLE protests
    ADD CONSTRAINT protests_declared_participants_check
        CHECK ((declared_participants <= 5000000) AND (declared_participants >= 0));