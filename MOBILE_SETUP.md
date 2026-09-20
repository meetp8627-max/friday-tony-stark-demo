# Running FRIDAY on your phone

Two pieces, both included in this download:

- `friday-backend/` — the original MCP tool server + voice agent, plus a new
  `token_server.py` I added so your phone can securely join a LiveKit room.
- `android-client/` — LiveKit's official Android voice-agent app, reskinned
  with a neon dark theme and renamed to F.R.I.D.A.Y.

## 1. Get LiveKit Cloud credentials

Sign up at https://cloud.livekit.io (free tier is enough). Create a project,
grab `LIVEKIT_URL`, `LIVEKIT_API_KEY`, `LIVEKIT_API_SECRET`.

## 2. Set up the backend on a machine that stays on

Could be a cheap VPS, a Raspberry Pi, or even your PC while testing.

```bash
cd friday-backend
cp .env.example .env
# fill in .env: LIVEKIT_* keys above, plus GOOGLE_API_KEY (Gemini),
# OPENAI_API_KEY (TTS), SARVAM_API_KEY (STT)
uv sync
```

Run all three processes (separate terminals, or as systemd services / tmux panes):

```bash
uv run friday          # MCP tool server (port 8000)
uv run friday_voice    # LiveKit voice agent
uv run friday_token    # token server for the Android app (port 8080)
```

## 3. Point the Android app at your token server

Open `android-client/app/src/main/java/io/livekit/android/example/voiceassistant/TokenExt.kt`
and replace `YOUR_SERVER_ADDRESS` with your backend's IP/domain, e.g.:

```kotlin
const val homepageAgentEndpoint = "http://203.0.113.10:8080/api/connection-details"
```

If your backend runs on your dev PC and your phone is on the same Wi-Fi,
use your PC's LAN IP instead (e.g. `192.168.1.23`) — check with `ip addr`
on Linux/WSL or `ipconfig` on Windows. Make sure port 8080 isn't blocked by
a firewall.

## 4. Build and run the Android app

Open `android-client/` in Android Studio, let Gradle sync, plug in your
phone (or use an emulator) and hit Run. Grant the microphone permission
when prompted, tap "START CALL".

## 5. Talk to it

The app joins a LiveKit room; `friday_voice` should auto-join the same room
and greet you. If it doesn't say anything, check the `friday_voice`
terminal for connection errors first — that isolates backend issues from
Android ones.

## Notes

- All the AI work (STT/LLM/TTS/tools) happens on your backend machine, not
  on the phone — the app is just a thin audio client, so it's light on
  battery.
- `token_server.py` currently allows any request through CORS (`*`) since
  it's just for your own phone. If you ever expose it publicly, lock that
  down and add real auth.
