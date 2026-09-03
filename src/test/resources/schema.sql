CREATE TABLE IF NOT EXISTS "users" (
    id UUID PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255),
    google_subject VARCHAR(255),
    avatar_url VARCHAR(255),
    avatar_public_id VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'pending_confirmation',
    gender VARCHAR(50),
    type VARCHAR(50) NOT NULL DEFAULT 'member',
    birth_date DATE,
    token_version INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP

);

CREATE UNIQUE INDEX IF NOT EXISTS "uq_users_google_subject"
ON "users" ("google_subject");

CREATE TABLE IF NOT EXISTS "refresh_token_sessions" (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    token_id VARCHAR(255) NOT NULL UNIQUE,
    family_id UUID NOT NULL,
    issued_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP,
    replaced_by_token_id VARCHAR(255),
    user_agent VARCHAR(1000),
    ip_address VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "email_verification_tokens" (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    consumed_at TIMESTAMP,
    last_sent_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "password_reset_tokens" (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    consumed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "login_attempts" (
    id UUID PRIMARY KEY,
    email_key VARCHAR(255) NOT NULL,
    ip_address VARCHAR(255),
    failed_count INTEGER NOT NULL DEFAULT 0,
    locked_until TIMESTAMP,
    last_failed_at TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE (email_key, ip_address)
);

CREATE TABLE IF NOT EXISTS "topics" (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    created_by UUID,
    updated_by UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "muhaddiths" (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    gender VARCHAR(50) NOT NULL,
    about VARCHAR(4000) NOT NULL,
    created_by UUID,
    updated_by UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "books" (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    muhaddith UUID,
    created_by UUID,
    updated_by UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "rawis" (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    gender VARCHAR(50) NOT NULL,
    about VARCHAR(4000) NOT NULL,
    created_by UUID,
    updated_by UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "ruling" (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    created_by UUID,
    updated_by UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "explaining" (
    id UUID PRIMARY KEY,
    text CLOB NOT NULL,
    normal_text CLOB,
    search_text CLOB,
    created_by UUID,
    updated_by UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "ahadith" (
    id UUID PRIMARY KEY,
    sub_valid UUID,
    explaining UUID,
    type VARCHAR(50) NOT NULL DEFAULT 'marfu',
    text CLOB NOT NULL,
    normal_text CLOB,
    search_text CLOB,
    search_vector VARCHAR(255),
    hadith_number INTEGER NOT NULL,
    ruling UUID,
    rawi UUID,
    book UUID,
    sanad VARCHAR(1000),
    created_by UUID,
    updated_by UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (hadith_number > 0),
    UNIQUE (book, hadith_number)
);

CREATE TABLE IF NOT EXISTS "fake_ahadith" (
    id UUID PRIMARY KEY,
    sub_valid UUID,
    text CLOB NOT NULL,
    normal_text CLOB,
    search_text CLOB,
    ruling UUID,
    created_by UUID,
    updated_by UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "topic_classes" (
    id UUID PRIMARY KEY,
    hadith UUID NOT NULL,
    topic UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (topic, hadith)
);

CREATE TABLE IF NOT EXISTS "activity_log" (
    id UUID PRIMARY KEY,
    actor_user_id UUID,
    actor_name VARCHAR(255),
    actor_email VARCHAR(255),
    actor_avatar_url VARCHAR(255),
    message VARCHAR(1000) NOT NULL,
    table_name VARCHAR(255),
    record_id UUID,
    old_data JSON,
    new_data JSON,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "search_history" (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    search_text VARCHAR(1000) NOT NULL,
    search_source VARCHAR(50) NOT NULL DEFAULT 'Hadith',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "comments" (
    id UUID PRIMARY KEY,
    hadith UUID NOT NULL,
    user_id UUID NOT NULL,
    text VARCHAR(2000) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "favorites" (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    hadith UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, hadith)
);

CREATE TABLE IF NOT EXISTS "questions" (
    id UUID PRIMARY KEY,
    hadith_id UUID,
    asker UUID NOT NULL,
    asker_text VARCHAR(2000) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    answer_text VARCHAR(4000),
    updated_by UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "upgrade_requests" (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'pending_documents',
    file_path VARCHAR(1000),
    reviewed_by UUID,
    notes VARCHAR(4000),
    review_notes VARCHAR(4000),
    rejection_reason VARCHAR(4000),
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    document_asset_id VARCHAR(255),
    document_public_id VARCHAR(1000),
    document_resource_type VARCHAR(50),
    document_delivery_type VARCHAR(50),
    document_format VARCHAR(50),
    document_original_name VARCHAR(255),
    document_size_bytes BIGINT,
    CHECK (document_size_bytes IS NULL OR document_size_bytes >= 0)
);

CREATE TABLE IF NOT EXISTS "notifications" (
    id UUID PRIMARY KEY,
    title VARCHAR(1000) NOT NULL,
    body VARCHAR(4000) NOT NULL,
    type VARCHAR(50) NOT NULL,
    hadith_id UUID,
    fake_hadith_id UUID,
    created_by UUID,
    user_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "user_fcm_tokens" (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    fcm_token VARCHAR(4096) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_seen TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (fcm_token)
);
