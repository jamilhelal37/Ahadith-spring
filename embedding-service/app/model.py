import importlib.metadata
import os
from threading import Lock

import torch
from FlagEmbedding import BGEM3FlagModel


MODEL_NAME = os.getenv("EMBEDDING_MODEL", "BAAI/bge-m3")
MODEL_VERSION = importlib.metadata.version("FlagEmbedding")
DIMENSION = 1024


class EmbeddingModel:
    def __init__(self) -> None:
        self.device = "cuda" if torch.cuda.is_available() else "cpu"
        self.batch_size = int(os.getenv("EMBEDDING_BATCH_SIZE", "16"))
        self._lock = Lock()
        self._model = BGEM3FlagModel(
            MODEL_NAME,
            use_fp16=self.device == "cuda",
            device=self.device,
        )

    def encode(self, texts: list[str]) -> list[list[float]]:
        with self._lock:
            result = self._model.encode(
                texts,
                batch_size=self.batch_size,
                max_length=8192,
                return_dense=True,
                return_sparse=False,
                return_colbert_vecs=False,
            )
        vectors = result["dense_vecs"].tolist()
        if any(len(vector) != DIMENSION for vector in vectors):
            raise RuntimeError("BGE-M3 returned an unexpected embedding dimension")
        return vectors
