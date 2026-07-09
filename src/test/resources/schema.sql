CREATE TABLE IF NOT EXISTS "users" (
    id UUID PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255),
    password VARCHAR(255),
    avatar_url VARCHAR(255),
    avatar_public_id VARCHAR(255),
    status VARCHAR(50),
    gender VARCHAR(50),
    type VARCHAR(50),
    birth_date DATE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "topics" (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_by UUID,
    updated_by UUID,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "muhaddiths" (
    id UUID PRIMARY KEY,
    name VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "books" (
    id UUID PRIMARY KEY,
    name VARCHAR(255),
    muhaddith UUID,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "rawis" (
    id UUID PRIMARY KEY,
    name VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "rulings" (
    id UUID PRIMARY KEY,
    name VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "ahadith" (
    id UUID PRIMARY KEY,
    sub_valid UUID,
    explaining UUID,
    type VARCHAR(50),
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
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "topic_classes" (
    id UUID PRIMARY KEY,
    hadith UUID,
    topic UUID,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
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
    old_data TEXT,
    new_data TEXT,
    created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "search_history" (
    id UUID PRIMARY KEY,
    user_id UUID,
    search_text VARCHAR(1000),
    search_source VARCHAR(50),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
