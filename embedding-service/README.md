# BGE-M3 embedding service

This service owns no application data. It loads `BAAI/bge-m3` once at startup and exposes dense 1024-dimensional embeddings over HTTP.

```bash
python3 -m venv .venv
. .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8001
```

CUDA is selected automatically when available; CPU uses full precision. Hugging Face uses `HF_HOME` for its model cache.
