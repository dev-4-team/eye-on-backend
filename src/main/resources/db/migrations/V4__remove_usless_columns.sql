ALTER TABLE protests
    DROP COLUMN description;

ALTER TABLE protests
    DROP COLUMN status;

-- 컬럼명 변경 (create_time -> created_at)
ALTER TABLE protests
    RENAME COLUMN create_time TO created_at;

ALTER TABLE protests
    ADD updated_at TIMESTAMP(6) WITH TIME ZONE;

-- 초기 updated_at 값을 created_at과 동일하게 설정
UPDATE protests
SET updated_at = created_at;


ALTER TABLE organizers
    ADD created_at TIMESTAMP(6) WITH TIME ZONE;

ALTER TABLE organizers
    ADD updated_at TIMESTAMP(6) WITH TIME ZONE;