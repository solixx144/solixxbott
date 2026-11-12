import os
from pathlib import Path
from dotenv import load_dotenv

# Load from .env if present
env_path = Path(__file__).resolve().parent / ".env"
load_dotenv(dotenv_path=env_path)

TELEGRAM_BOT_TOKEN = os.getenv("TELEGRAM_BOT_TOKEN") or os.getenv("BOT_TOKEN", "")
TARGET_CHANNEL = os.getenv("TARGET_CHANNEL", "@SolixxTR")
ADMIN_API_KEY = os.getenv("ADMIN_API_KEY", "")
raw_admin_id = os.getenv("ADMIN_TELEGRAM_ID", "").strip()
raw_admin_ids = os.getenv("ADMIN_TELEGRAM_IDS", "").strip()
ADMIN_TELEGRAM_ID = int(raw_admin_id) if raw_admin_id.isdigit() else 0

# Set of all authorized administrator Telegram IDs
ADMIN_TELEGRAM_IDS = set()
if ADMIN_TELEGRAM_ID > 0:
    ADMIN_TELEGRAM_IDS.add(ADMIN_TELEGRAM_ID)
if raw_admin_ids:
    for part in raw_admin_ids.split(","):
        part = part.strip()
        if part.isdigit():
            ADMIN_TELEGRAM_IDS.add(int(part))
HOST = os.getenv("HOST", "0.0.0.0")
PORT = int(os.getenv("PORT", "8000"))
DATABASE_PATH = os.getenv("DATABASE_PATH", str(Path(__file__).resolve().parent / "solixxbot.db"))
