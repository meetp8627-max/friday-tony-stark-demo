"""
FRIDAY – Token Server
======================
A tiny HTTP endpoint that mints LiveKit access tokens for the Android app.

Why this exists: the Android client needs to prove it's allowed to join a
LiveKit room. It should never hold your LIVEKIT_API_SECRET itself (that
would let anyone extract it from the APK), so this server holds the secret
and hands out short-lived, room-scoped tokens instead.

Response shape matches LiveKit's standard "connection details" contract,
the same one used by the official example apps:
    { "serverUrl": "...", "roomName": "...", "participantName": "...", "participantToken": "..." }

Run:
  uv run friday_token           -> starts on http://0.0.0.0:8080
  (keep this running alongside `uv run friday` and `uv run friday_voice`)

Android side: point `homepageAgentEndpoint` in TokenExt.kt at
  http://<your-server-ip-or-domain>:8080/api/connection-details
"""

import logging
import os
import uuid

from dotenv import load_dotenv
from fastapi import FastAPI, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware
from livekit import api

load_dotenv()

logger = logging.getLogger("friday-token-server")
logger.setLevel(logging.INFO)

LIVEKIT_URL = os.getenv("LIVEKIT_URL", "")
LIVEKIT_API_KEY = os.getenv("LIVEKIT_API_KEY", "")
LIVEKIT_API_SECRET = os.getenv("LIVEKIT_API_SECRET", "")

app = FastAPI(title="FRIDAY Token Server")

# Loosened for a personal single-user app talking to your own phone.
# Tighten this (specific origins) if you ever expose it beyond your own device.
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/api/connection-details")
def connection_details(
    room: str | None = Query(default=None),
    identity: str | None = Query(default=None),
):
    if not (LIVEKIT_URL and LIVEKIT_API_KEY and LIVEKIT_API_SECRET):
        raise HTTPException(
            status_code=500,
            detail="LIVEKIT_URL / LIVEKIT_API_KEY / LIVEKIT_API_SECRET are not set on the server.",
        )

    room_name = room or f"friday-{uuid.uuid4().hex[:8]}"
    participant_identity = identity or f"boss-{uuid.uuid4().hex[:6]}"

    token = (
        api.AccessToken(LIVEKIT_API_KEY, LIVEKIT_API_SECRET)
        .with_identity(participant_identity)
        .with_name(participant_identity)
        .with_grants(
            api.VideoGrants(
                room_join=True,
                room=room_name,
                can_publish=True,
                can_subscribe=True,
            )
        )
        .with_ttl(seconds=60 * 60)  # 1 hour, plenty for a session
    )

    logger.info("Issued token for room=%s identity=%s", room_name, participant_identity)

    return {
        "serverUrl": LIVEKIT_URL,
        "roomName": room_name,
        "participantName": participant_identity,
        "participantToken": token.to_jwt(),
    }


@app.get("/healthz")
def healthz():
    return {"ok": True}


def main():
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=8080)


if __name__ == "__main__":
    main()
