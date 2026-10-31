# SolixxBot — Backend & Telegram Moderation Service

SolixxBot is a production-oriented Telegram announcement submission, moderation, and publishing system built for **@SolixxTR**.

## Architecture Overview

```
+-------------------------------------------------------------+
| Authorized Friends (Telegram)                                |
| • Send text, photo, album, video, audio, voice messages    |
+-------------------------------------------------------------+
                              │
                              ▼
+-------------------------------------------------------------+
| SolixxBot Telegram Service (python-telegram-bot 22.x)       |
| • Checks friend authorization against SQLite DB             |
| • Debounces & aggregates media groups (albums)              |
| • Stores submissions into approval queue (status: PENDING)  |
+-------------------------------------------------------------+
                              │
                              ▼
+-------------------------------------------------------------+
| SolixxBot Admin API (FastAPI + SQLite WAL)                  |
| • Endpoints: /api/announcements, /api/friends, /api/stats   |
| • Atomic transactions, duplicate protection                 |
+-------------------------------------------------------------+
                              │
                              ▼
+-------------------------------------------------------------+
| SolixxBot Android Application                               |
| • Real-time Pending Queue & History                         |
| • One-tap APPROVE, custom reason REJECT, instant EDIT       |
| • Telegram Channel Post Simulator                           |
| • Authorized Friend Management with TG ID lookup            |
+-------------------------------------------------------------+
                              │ (When Admin Approves)
                              ▼
+-------------------------------------------------------------+
| Telegram Channel: @SolixxTR                                 |
| • Preserves original media group formatting & captions      |
| • Sends private confirmation notification to sender         |
+-------------------------------------------------------------+
```

## Quick Start (PC / Linux / Mac)

1. **Install requirements:**
   ```bash
   cd backend
   pip install -r requirements.txt
   ```

2. **Configure environment:**
   ```bash
   cp .env.example .env
   # Edit .env and fill in:
   # TELEGRAM_BOT_TOKEN (obtain from @BotFather on Telegram)
   # TARGET_CHANNEL=@SolixxTR
   # ADMIN_API_KEY=your_secret_admin_api_key
   # ADMIN_TELEGRAM_ID=your_numeric_telegram_user_id
   ```

3. **Telegram Channel Setup:**
   - Add your bot to the **@SolixxTR** channel.
   - Grant the bot **Administrator** rights with the **"Post Messages"** permission.

4. **Run the server:**
   ```bash
   python main.py
   ```
   The API will listen at `http://0.0.0.0:8000`.

## Android Tablet Deployment (Termux)

To run the backend 24/7 on an Android tablet or phone without a computer:

1. Install **Termux** (from F-Droid or GitHub Releases).
2. Clone or copy this repository to Termux.
3. Make the script executable and run:
   ```bash
   chmod +x backend/run_termux.sh
   ./backend/run_termux.sh
   ```
4. In the SolixxBot Android app Settings screen, enter `http://127.0.0.1:8000` (or `http://10.0.2.2:8000` on Android Emulator) and your Admin API Key.

## REST API Reference

All administrative endpoints require the header `X-Admin-Key: <ADMIN_API_KEY>` or `Authorization: Bearer <ADMIN_API_KEY>`.

- `GET /api/status` — System health and bot status.
- `GET /api/stats` — Metrics for Dashboard (pending, published, rejected, friends).
- `GET /api/announcements?status=PENDING` — List announcements by status.
- `POST /api/announcements/{id}/approve` — Approve and publish to `@SolixxTR`.
- `POST /api/announcements/{id}/reject` — Reject with custom explanation sent to submitter.
- `POST /api/announcements/{id}/edit` — Edit text or caption before approving.
- `GET /api/friends` — List authorized friends.
- `POST /api/friends` — Authorize a new friend by Telegram user ID.
- `DELETE /api/friends/{telegram_id}` — Revoke authorization.
- `GET /api/logs` — Review audit trail and publishing logs.
- `GET /api/backup` — Export full JSON backup of database.
