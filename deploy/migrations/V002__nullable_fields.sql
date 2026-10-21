-- Apply once, transactionally. Preserve all existing rows and references.
BEGIN;
DO $$ BEGIN
 IF EXISTS (SELECT 1 FROM tickets WHERE y < -2147483648 OR y > 2147483647) THEN
  RAISE EXCEPTION 'tickets.y contains values outside the approved int32 range';
 END IF;
END $$;
ALTER TABLE persons ALTER COLUMN weight DROP NOT NULL;
ALTER TABLE tickets ALTER COLUMN y DROP NOT NULL;
ALTER TABLE tickets ALTER COLUMN y TYPE integer USING y::integer;
COMMIT;
