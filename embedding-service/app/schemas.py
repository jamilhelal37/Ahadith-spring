from pydantic import BaseModel, Field, field_validator


class EmbedRequest(BaseModel):
    texts: list[str] = Field(min_length=1, max_length=256)

    @field_validator("texts")
    @classmethod
    def validate_texts(cls, texts: list[str]) -> list[str]:
        if any(not text or not text.strip() for text in texts):
            raise ValueError("texts must contain only non-blank strings")
        if any(len(text) > 20_000 for text in texts):
            raise ValueError("each text must be at most 20000 characters")
        return texts


class EmbedResponse(BaseModel):
    model: str
    modelVersion: str
    dimension: int
    embeddings: list[list[float]]


class HealthResponse(BaseModel):
    status: str
    model: str
    modelVersion: str
    dimension: int
    device: str
