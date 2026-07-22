-- Fix UpgradeRequest user relation and add partial unique index
-- We drop the existing foreign key first if needed, but here we just modify the column and add index

ALTER TABLE upgrade_requests 
ALTER COLUMN user_id SET NOT NULL;

-- Remove unique constraint on user_id if it exists (it was OneToOne)
-- Usually named upgrade_requests_user_id_key or similar
DO $$ 
BEGIN 
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'upgrade_requests_user_id_key') THEN
        ALTER TABLE upgrade_requests DROP CONSTRAINT upgrade_requests_user_id_key;
    END IF;
END $$;

-- Add partial unique index for open requests
CREATE UNIQUE INDEX idx_upgrade_requests_user_open ON upgrade_requests(user_id) 
WHERE status IN ('pending_documents', 'under_review');
