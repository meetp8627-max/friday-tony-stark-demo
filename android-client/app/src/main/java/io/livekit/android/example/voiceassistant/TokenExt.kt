package io.livekit.android.example.voiceassistant

// Not used — we're using our own self-hosted token server instead (see below).
const val tokenServerId = ""

// NOTE: for quick local testing only (tokens expire in ~1hr): paste values from
// https://cloud.livekit.io/projects/p_/settings/keys here to skip the token server entirely.
const val hardcodedUrl = ""
const val hardcodedToken = ""

// FRIDAY's own token_server.py (see friday-backend/token_server.py), deployed alongside
// server.py and agent_friday.py. Replace with your VPS's address, e.g.:
//   "http://203.0.113.10:8080/api/connection-details"
// While testing on the same Wi-Fi as your dev machine, your machine's LAN IP works too, e.g.:
//   "http://192.168.1.23:8080/api/connection-details"
const val homepageAgentEndpoint = "https://just-gentleness-production-2cec.up.railway.app/api/connection-details"
