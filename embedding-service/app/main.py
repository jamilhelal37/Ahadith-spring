from contextlib import asynccontextmanager
from typing import AsyncIterator

from fastapi import FastAPI, HTTPException, Request
from fastapi.concurrency import run_in_threadpool

from .model import DIMENSION, MODEL_NAME, MODEL_VERSION, EmbeddingModel
from .schemas import EmbedRequest, EmbedResponse, HealthResponse


@asynccontextmanager
async def lifespan(app: FastAPI) -> AsyncIterator[None]:
    app.state.embedding_model = await run_in_threadpool(EmbeddingModel)
    yield


app = FastAPI(title="Ahadith BGE-M3 Embedding Service", version="1.0.0", lifespan=lifespan)


@app.get("/health", response_model=HealthResponse)
async def health(request: Request) -> HealthResponse:
    model: EmbeddingModel = request.app.state.embedding_model
    return HealthResponse(
        status="UP",
        model=MODEL_NAME,
        modelVersion=MODEL_VERSION,
        dimension=DIMENSION,
        device=model.device,
    )


@app.post("/embed", response_model=EmbedResponse)
async def embed(payload: EmbedRequest, request: Request) -> EmbedResponse:
    model: EmbeddingModel = request.app.state.embedding_model
    try:
        vectors = await run_in_threadpool(model.encode, payload.texts)
    except Exception as exc:
        raise HTTPException(status_code=500, detail="Embedding generation failed") from exc
    return EmbedResponse(
        model=MODEL_NAME,
        modelVersion=MODEL_VERSION,
        dimension=DIMENSION,
        embeddings=vectors,
    )
