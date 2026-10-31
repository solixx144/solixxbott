#!/data/data/com.termux/files/usr/bin/bash
# SolixxBot - Termux 1-Click Deployment Script
# Runs SolixxBot Backend & Telegram Bot service locally on Android

set -e

echo "==============================================="
echo "   SOLIXXBOT - TELEGRAM MODERATION SERVER     "
echo "   Target Channel: @SolixxTR                   "
echo "==============================================="

# Update package lists
echo "[1/5] Updating Termux packages..."
pkg update -y && pkg upgrade -y

# Install dependencies
echo "[2/5] Installing Python, SQLite3 and OpenSSL..."
pkg install -y python python-pip sqlite openssl clang

# Setup working directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Check .env configuration
if [ ! -f ".env" ]; then
    echo "[!] .env file not found. Creating from .env.example..."
    cp .env.example .env
    echo "⚠️ Please edit $SCRIPT_DIR/.env to add your TELEGRAM_BOT_TOKEN before running!"
fi

# Install python requirements
echo "[3/5] Installing Python dependencies..."
pip install --upgrade pip
pip install -r requirements.txt

# Start backend server
echo "[4/5] Initializing database and starting API server..."
echo "-----------------------------------------------"
echo "Local Server URL: http://127.0.0.1:8000"
echo "Network IP: $(ifconfig 2>/dev/null | grep -Eo 'inet (addr:)?([0-9]*\.){3}[0-9]*' | grep -Eo '([0-9]*\.){3}[0-9]*' | grep -v '127.0.0.1' | head -n 1 || echo 'Check Wi-Fi IP'):8000"
echo "Target Channel: @SolixxTR"
echo "-----------------------------------------------"
echo "Starting SolixxBot service..."
python main.py
