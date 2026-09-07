FROM python:3.11-slim

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    HF_HOME=/models/huggingface

WORKDIR /service

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY app app

RUN useradd --create-home --uid 10001 appuser \
    && mkdir -p /models/huggingface \
    && chown -R appuser:appuser /service /models

USER appuser
EXPOSE 8001

HEALTHCHECK --interval=30s --timeout=10s --start-period=5m --retries=5 \
  CMD python -c "import urllib.request; urllib.request.urlopen('http://127.0.0.1:8001/health', timeout=5)" || exit 1

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8001"]
