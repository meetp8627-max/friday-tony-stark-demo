#!/usr/bin/env bash
# Runs FRIDAY's three backend pieces together in one container:
#   - friday        : the MCP tool server (news, web search, etc.) on :8000
#   - friday_voice   : the LiveKit voice agent that joins rooms and talks
#   - friday_token   : mints LiveKit join tokens for the Android app, on $PORT
#
# If any one of them dies, the whole container exits so your host (Railway,
# Fly.io, etc.) restarts it rather than silently running in a half-broken state.
set -e

uv run friday &
PID1=$!

uv run friday_voice &
PID2=$!

uv run friday_token &
PID3=$!

wait -n "$PID1" "$PID2" "$PID3"
exit_code=$?
echo "One of the FRIDAY processes exited (code $exit_code) — stopping the rest."
kill "$PID1" "$PID2" "$PID3" 2>/dev/null || true
exit "$exit_code"
