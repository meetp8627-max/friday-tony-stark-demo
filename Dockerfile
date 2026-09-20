FROM python:3.12-slim

WORKDIR /app

# uv = fast Python package manager, matches this project's pyproject.toml/uv.lock
RUN pip install --no-cache-dir uv

COPY . .
RUN uv sync --no-dev || uv sync

RUN chmod +x start_all.sh

# Railway/Render/Fly.io inject PORT; friday_token reads it, the others don't need it.
ENV PORT=8080
EXPOSE 8080

CMD ["./start_all.sh"]
