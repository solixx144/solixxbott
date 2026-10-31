import asyncio
from contextlib import asynccontextmanager
from datetime import datetime
from typing import Optional, List, Dict, Any

from fastapi import FastAPI, Depends, HTTPException, Header, Query, status
from fastapi.middleware.cors import CORSMiddleware
import uvicorn

import config
import database
from models import (
    FriendCreate,
    FriendResponse,
    AnnouncementResponse,
    ApproveRequest,
    RejectRequest,
    EditRequest,
    SystemStatusResponse,
)
import bot

# Security Dependency: Validates administrator credentials server-side
async def verify_admin(
    x_admin_key: Optional[str] = Header(None, alias="X-Admin-Key"),
    x_admin_tg_id: Optional[str] = Header(None, alias="X-Admin-Telegram-Id"),
    authorization: Optional[str] = Header(None)
):
    provided_key = None
    if x_admin_key:
        provided_key = x_admin_key
    elif authorization:
        parts = authorization.split()
        if len(parts) == 2 and parts[0].lower() == "bearer":
            provided_key = parts[1]

    if not config.ADMIN_API_KEY or not provided_key or provided_key != config.ADMIN_API_KEY:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Unauthorized: Invalid Administrator API Key",
            headers={"WWW-Authenticate": "Bearer"},
        )

    # If backend has ADMIN_TELEGRAM_ID configured, enforce matching numeric Telegram ID
    if config.ADMIN_TELEGRAM_ID > 0 and x_admin_tg_id:
        try:
            tg_id = int(x_admin_tg_id.strip())
            if tg_id != config.ADMIN_TELEGRAM_ID:
                raise HTTPException(
                    status_code=status.HTTP_403_FORBIDDEN,
                    detail="Forbidden: Telegram User ID does not match configured Administrator ID"
                )
        except ValueError:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Invalid Administrator Telegram ID format"
            )

    return True

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Initialize DB (Starts completely empty)
    database.init_db()

    # Start Telegram Bot Polling in background
    bot_task = asyncio.create_task(bot.start_telegram_bot())
    yield
    # Graceful shutdown
    if bot.bot_app:
        try:
            await bot.bot_app.updater.stop()
            await bot.bot_app.stop()
            await bot.bot_app.shutdown()
        except Exception:
            pass

app = FastAPI(
    title="SolixxBot Admin API",
    description="Backend API for SolixxBot Telegram Announcement & Moderation System for @SolixxTR",
    version="1.0.0",
    lifespan=lifespan
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Public health & info endpoint
@app.get("/api/status", response_model=SystemStatusResponse)
async def get_system_status():
    stats = database.get_dashboard_stats()
    is_bot_running = bot.bot_app is not None and bot.bot_app.updater is not None and bot.bot_app.updater.running
    return {
        "online": True,
        "bot_connected": is_bot_running,
        "target_channel": config.TARGET_CHANNEL,
        "total_announcements": stats["total_announcements"],
        "pending_announcements": stats["pending_announcements"],
        "published_announcements": stats["published_announcements"],
        "rejected_announcements": stats["rejected_announcements"],
        "total_friends": stats["total_authorized_friends"],
        "server_time": datetime.utcnow().strftime("%Y-%m-%d %H:%M:%S UTC"),
        "database_ok": True
    }

# Dashboard Stats
@app.get("/api/stats", dependencies=[Depends(verify_admin)])
async def get_stats():
    return database.get_dashboard_stats()

# Announcements Queue & History
@app.get("/api/announcements", response_model=List[AnnouncementResponse], dependencies=[Depends(verify_admin)])
async def list_announcements(
    status: Optional[str] = Query("ALL", description="Filter by status: PENDING, APPROVED, REJECTED, ALL"),
    search: Optional[str] = Query(None, description="Search in text or author info"),
    limit: int = Query(50, ge=1, le=200),
    offset: int = Query(0, ge=0)
):
    return database.get_announcements(status=status, search=search, limit=limit, offset=offset)

@app.get("/api/announcements/{announcement_id}", response_model=AnnouncementResponse, dependencies=[Depends(verify_admin)])
async def get_announcement(announcement_id: int):
    ann = database.get_announcement_by_id(announcement_id)
    if not ann:
        raise HTTPException(status_code=404, detail="Announcement not found")
    return ann

@app.post("/api/announcements/{announcement_id}/approve", dependencies=[Depends(verify_admin)])
async def approve_announcement(
    announcement_id: int,
    req: ApproveRequest,
    x_admin_tg_id: Optional[str] = Header(None, alias="X-Admin-Telegram-Id")
):
    ann = database.get_announcement_by_id(announcement_id)
    if not ann:
        raise HTTPException(status_code=404, detail="Announcement not found")
    if ann["status"] != "PENDING":
        raise HTTPException(status_code=400, detail=f"Cannot approve announcement with status {ann['status']}")

    publisher = req.published_by or (f"Admin (TG ID: {x_admin_tg_id})" if x_admin_tg_id else "Admin")

    # If live bot is connected, publish via Telegram Bot API
    if bot.bot_app and bot.bot_app.bot:
        success = await bot.publish_announcement(announcement_id, published_by=publisher)
        if not success:
            raise HTTPException(status_code=500, detail="Failed to publish announcement to Telegram channel @SolixxTR")
    else:
        # Commit local approval
        success = database.approve_announcement_atomic(
            announcement_id=announcement_id,
            published_message_ids=[],
            published_by=publisher
        )

    updated_ann = database.get_announcement_by_id(announcement_id)
    return {"success": True, "message": "Announcement approved and published", "announcement": updated_ann}

@app.post("/api/announcements/{announcement_id}/reject", dependencies=[Depends(verify_admin)])
async def reject_announcement(announcement_id: int, req: RejectRequest):
    ann = database.get_announcement_by_id(announcement_id)
    if not ann:
        raise HTTPException(status_code=404, detail="Announcement not found")
    if ann["status"] != "PENDING":
        raise HTTPException(status_code=400, detail=f"Cannot reject announcement with status {ann['status']}")

    success = await bot.notify_rejection(announcement_id, reason=req.rejection_reason, admin_notes=req.admin_notes)
    if not success:
        success = database.reject_announcement_atomic(announcement_id, req.rejection_reason, req.admin_notes)

    updated_ann = database.get_announcement_by_id(announcement_id)
    return {"success": True, "message": "Announcement rejected and explanation sent", "announcement": updated_ann}

@app.post("/api/announcements/{announcement_id}/edit", dependencies=[Depends(verify_admin)])
async def edit_announcement(announcement_id: int, req: EditRequest):
    ann = database.get_announcement_by_id(announcement_id)
    if not ann:
        raise HTTPException(status_code=404, detail="Announcement not found")
    if ann["status"] != "PENDING":
        raise HTTPException(status_code=400, detail="Only pending announcements can be edited before approval")

    updated = database.edit_announcement_content(announcement_id, req.new_text, req.admin_notes)
    if not updated:
        raise HTTPException(status_code=500, detail="Failed to update announcement")

    updated_ann = database.get_announcement_by_id(announcement_id)
    return {"success": True, "message": "Announcement updated successfully", "announcement": updated_ann}

# Friend Management
@app.get("/api/friends", response_model=List[FriendResponse], dependencies=[Depends(verify_admin)])
async def list_friends(search: Optional[str] = Query(None)):
    return database.get_friends(search=search)

@app.post("/api/friends", response_model=Dict[str, Any], dependencies=[Depends(verify_admin)])
async def create_friend(req: FriendCreate):
    try:
        new_friend = database.add_friend(
            telegram_id=req.telegram_id,
            display_name=req.display_name,
            username=req.username,
            notes=req.notes
        )
        return {"success": True, "friend": new_friend}
    except Exception as e:
        raise HTTPException(status_code=400, detail=f"Could not add friend: {str(e)}")

@app.delete("/api/friends/{telegram_id}", dependencies=[Depends(verify_admin)])
async def delete_friend(telegram_id: int):
    removed = database.remove_friend(telegram_id)
    if not removed:
        raise HTTPException(status_code=404, detail="Friend not found")
    return {"success": True, "message": f"Friend authorization removed for TG ID {telegram_id}"}

@app.patch("/api/friends/{telegram_id}/toggle", dependencies=[Depends(verify_admin)])
async def toggle_friend(telegram_id: int, active: bool = Query(...)):
    toggled = database.toggle_friend_active(telegram_id, active)
    if not toggled:
        raise HTTPException(status_code=404, detail="Friend not found")
    return {"success": True, "is_active": active}

@app.get("/api/friends/{telegram_id}/history", dependencies=[Depends(verify_admin)])
async def friend_submission_history(telegram_id: int):
    conn = database.get_connection()
    cursor = conn.cursor()
    cursor.execute("SELECT * FROM announcements WHERE sender_telegram_id = ? ORDER BY id DESC", (telegram_id,))
    rows = [dict(r) for r in cursor.fetchall()]
    conn.close()
    return rows

# Logs & Diagnostics
@app.get("/api/logs", dependencies=[Depends(verify_admin)])
async def get_system_logs(limit: int = Query(50, ge=1, le=200)):
    return database.get_logs(limit=limit)

# Backup & Restore
@app.get("/api/backup", dependencies=[Depends(verify_admin)])
async def export_backup():
    return database.export_all_data()

@app.post("/api/clear-data", dependencies=[Depends(verify_admin)])
async def clear_database():
    database.clear_all_tables()
    return {"success": True, "message": "All data cleared successfully"}

if __name__ == "__main__":
    uvicorn.run("main:app", host=config.HOST, port=config.PORT, reload=False)
