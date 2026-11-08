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

## 24/7 Cloud & Server Deployment (Kesintisiz Çalıştırma)

Backend ve Telegram botu Android uygulamasından bağımsız olarak **7 gün 24 saat** kesintisiz çalışabilir.

### Seçenek 1: Docker / Docker Compose (Tavsiye Edilen)
Bulut sunucunuzda (Ubuntu/Debian VPS, Hetzner, AWS, DigitalOcean vb.):
```bash
cd backend
cp .env.example .env
# .env dosyasını bot tokeni ve admin bilgileriyle doldurun:
nano .env

# Docker compose ile arka planda 7/24 başlatın:
docker compose up -d --build

# Durumu ve logları kontrol edin:
docker compose logs -f
```

### Seçenek 2: Linux VPS Systemd Servisi (Otomatik Yeniden Başlatma)
```bash
sudo cp backend/systemd/solixxbot.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable solixxbot
sudo systemctl start solixxbot

# Servis durumunu kontrol edin:
sudo systemctl status solixxbot
```

### Seçenek 3: Bulut Sağlayıcılar (Railway / Render / Fly.io)
1. Bu depoyu GitHub'a yükleyin.
2. Railway veya Render üzerinde **New Web Service** seçin.
3. Root Directory: `backend` belirleyin.
4. Çevre değişkenlerini (`BOT_TOKEN`, `TARGET_CHANNEL`, `ADMIN_API_KEY`, `ADMIN_TELEGRAM_ID`) tanımlayın.
5. Dağıtılan genel URL'yi (örn: `https://solixxbot.up.railway.app`) Android uygulamasının Ayarlar bölümüne girin.

### Seçenek 4: Android Cihaz / Tablet Üzerinde 24/7 (Termux)
Harici bir bilgisayara ihtiyaç duymadan eski bir Android telefon veya tableti 7/24 sunucuya dönüştürmek için:
1. **Termux** kurun (F-Droid üzerinden).
2. Arka plan pil kısıtlamasını kaldırın (Termux -> App Info -> Battery Unrestricted).
3. `chmod +x backend/run_termux.sh && ./backend/run_termux.sh` çalıştırın.
4. Android uygulamasında `http://127.0.0.1:8000` (veya yerel Wi-Fi IP'si) adresini girin.

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
