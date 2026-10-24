-- Additive migration for an existing tuvi_ai schema.
-- This is a manual, one-time migration (not wired to Flyway).
-- Review the preflight query and back up the database before applying.
-- Do not apply if the preflight query returns rows.

USE tuvi_ai;

-- These rows must be reconciled with their owners before adding the constraint.
SELECT c.id AS chart_id, c.user_id AS chart_user_id,
       bp.user_id AS profile_user_id
FROM astrology_charts c
JOIN birth_profiles bp ON bp.id = c.birth_profile_id
WHERE c.user_id <> bp.user_id;

-- The following indexes and constraint are additive; no rows or tables are deleted.
ALTER TABLE birth_profiles
    ADD UNIQUE INDEX uq_birth_profiles_id_user (id, user_id);

ALTER TABLE astrology_charts
    ADD UNIQUE INDEX uq_astrology_charts_id_user (id, user_id);

ALTER TABLE astrology_charts
    ADD CONSTRAINT fk_charts_profile_owner
        FOREIGN KEY (birth_profile_id, user_id)
        REFERENCES birth_profiles (id, user_id)
        ON DELETE CASCADE;
