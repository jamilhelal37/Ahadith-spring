CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS public.hadith_embeddings (
    hadith_id uuid PRIMARY KEY,
    embedding vector(1024) NOT NULL,
    model_name varchar(200) NOT NULL,
    model_version varchar(100),
    content_hash varchar(64) NOT NULL,
    embedded_at timestamptz NOT NULL DEFAULT current_timestamp,
    CONSTRAINT fk_hadith_embeddings_hadith
        FOREIGN KEY (hadith_id)
        REFERENCES public.ahadith(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_hadith_embeddings_embedding_hnsw
    ON public.hadith_embeddings
    USING hnsw (embedding vector_cosine_ops);
