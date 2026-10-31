import os
from pathlib import Path
from dotenv import load_dotenv

# Load from .env if present
env_path = Path(__file__).resolve().parent / ".env"
load_dotenv(dotenv_path=env_path)

TELEGRAM_BOT_TOKEN = os.getenv("TELEGRAM_BOT_TOKEN", "")
TARGET_CHANNEL = os.getenv("TARGET_CHANNEL", "@SolixxTR")
ADMIN_API_KEY = os.getenv("ADMIN_API_KEY", "")
ADMIN_TELEGRAM_ID = int(os.getenv("ADMIN_TELEGRAM_ID", "0")) if os.getenv("ADMIN_TELEGRAM_ID", "").strip().isdigit() else 0
HOST = os.getenv("HOST", "0.0.0.0")
PORT = int(os.getenv("PORT", "8000"))
DATABASE_PATH = os.getenv("DATABASE_PATH", str(Path(__file__).resolve().parent / "solixxbot.db"))
