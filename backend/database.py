import sqlite3
import json
import os
from datetime import datetime
from typing import List, Dict, Any, Optional
from config import DATABASE_PATH

def get_connection() -> sqlite3.Connection:
    conn = sqlite3.connect(DATABASE_PATH)
    conn.row_factory = sqlite3.Row
    conn.execute("PRAGMA journal_mode=WAL")
    conn.execute("PRAGMA foreign_keys=ON")
    return conn

def init_db():
    conn = get_connection()
    with conn:
        # Schema migrations table
        conn.execute("""
            CREATE TABLE IF NOT EXISTS schema_migrations (
                version INTEGER PRIMARY KEY,
                applied_at TEXT NOT NULL
            )
        """)

        # Friends table - authorized strictly by numeric Telegram user ID
        conn.execute("""
            CREATE TABLE IF NOT EXISTS friends (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                telegram_id INTEGER UNIQUE NOT NULL,
                username TEXT,
                display_name TEXT NOT NULL,
                added_at TEXT NOT NULL,
                is_active INTEGER NOT NULL DEFAULT 1,
                notes TEXT
            )
        """)
        conn.execute("CREATE INDEX IF NOT EXISTS idx_friends_tgid ON friends(telegram_id)")

        # Announcements table
        conn.execute("""
            CREATE TABLE IF NOT EXISTS announcements (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                sender_telegram_id INTEGER NOT NULL,
                sender_username TEXT,
                sender_display_name TEXT NOT NULL,
                content_type TEXT NOT NULL,
                text_content TEXT,
                media_group_id TEXT,
                media_items_json TEXT NOT NULL DEFAULT '[]',
                status TEXT NOT NULL DEFAULT 'PENDING',
                created_at TEXT NOT NULL,
                updated_at TEXT NOT NULL,
                published_at TEXT,
                published_message_ids_json TEXT,
                rejection_reason TEXT,
                admin_notes TEXT,
                published_by TEXT
            )
        """)
        conn.execute("CREATE INDEX IF NOT EXISTS idx_announcements_status ON announcements(status)")
        conn.execute("CREATE INDEX IF NOT EXISTS idx_announcements_sender ON announcements(sender_telegram_id)")
        conn.execute("CREATE INDEX IF NOT EXISTS idx_announcements_group ON announcements(media_group_id)")

        # System audit logs table
        conn.execute("""
            CREATE TABLE IF NOT EXISTS system_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                timestamp TEXT NOT NULL,
                level TEXT NOT NULL,
                category TEXT NOT NULL,
                message TEXT NOT NULL,
                details TEXT
            )
        """)

        # Record migration
        conn.execute("""
            INSERT OR IGNORE INTO schema_migrations (version, applied_at)
            VALUES (1, datetime('now'))
        """)

    conn.close()

# Friend Operations
def is_friend_authorized(telegram_id: int) -> bool:
    conn = get_connection()
    cursor = conn.cursor()
    cursor.execute("SELECT id FROM friends WHERE telegram_id = ? AND is_active = 1", (telegram_id,))
    row = cursor.fetchone()
    conn.close()
    return row is not None

def get_friends(search: Optional[str] = None) -> List[Dict[str, Any]]:
    conn = get_connection()
    cursor = conn.cursor()
    query = """
        SELECT f.*, COUNT(a.id) as submission_count
        FROM friends f
        LEFT JOIN announcements a ON f.telegram_id = a.sender_telegram_id
    """
    params = []
    if search:
        query += " WHERE f.display_name LIKE ? OR f.username LIKE ? OR CAST(f.telegram_id AS TEXT) LIKE ?"
        like_term = f"%{search}%"
        params.extend([like_term, like_term, like_term])
    query += " GROUP BY f.id ORDER BY f.added_at DESC"
    cursor.execute(query, params)
    rows = [dict(row) for row in cursor.fetchall()]
    conn.close()
    return rows

def add_friend(telegram_id: int, display_name: Optional[str] = None, username: Optional[str] = None, notes: Optional[str] = None) -> Dict[str, Any]:
    conn = get_connection()
    now = datetime.utcnow().strftime("%Y-%m-%d %H:%M:%S")
    clean_display = display_name.strip() if display_name and display_name.strip() else f"User {telegram_id}"
    clean_username = username.strip().replace("@", "") if username and username.strip() else None

    with conn:
        cursor = conn.cursor()
        cursor.execute("""
            INSERT INTO friends (telegram_id, username, display_name, added_at, is_active, notes)
            VALUES (?, ?, ?, ?, 1, ?)
            ON CONFLICT(telegram_id) DO UPDATE SET
                username = COALESCE(excluded.username, friends.username),
                display_name = excluded.display_name,
                is_active = 1,
                notes = COALESCE(excluded.notes, friends.notes)
        """, (telegram_id, clean_username, clean_display, now, notes))
        friend_id = cursor.lastrowid
    conn.close()
    add_log("INFO", "FRIENDS", f"Authorized friend with Telegram ID: {telegram_id}", f"Username: {clean_username or 'None'}")
    return {"id": friend_id, "telegram_id": telegram_id, "display_name": clean_display, "username": clean_username}

def update_friend_telegram_info(telegram_id: int, actual_username: Optional[str], actual_name: Optional[str]):
    """Sync real Telegram profile information when user interacts with bot."""
    conn = get_connection()
    with conn:
        cursor = conn.cursor()
        if actual_username and actual_name:
            cursor.execute("""
                UPDATE friends 
                SET username = ?, display_name = ?
                WHERE telegram_id = ?
            """, (actual_username, actual_name, telegram_id))
        elif actual_username:
            cursor.execute("""
                UPDATE friends 
                SET username = ?
                WHERE telegram_id = ?
            """, (actual_username, telegram_id))
    conn.close()

def remove_friend(telegram_id: int) -> bool:
    conn = get_connection()
    with conn:
        cursor = conn.cursor()
        cursor.execute("DELETE FROM friends WHERE telegram_id = ?", (telegram_id,))
        count = cursor.rowcount
    conn.close()
    if count > 0:
        add_log("WARN", "FRIENDS", f"Revoked friend authorization for Telegram ID: {telegram_id}", None)
    return count > 0

def toggle_friend_active(telegram_id: int, is_active: bool) -> bool:
    conn = get_connection()
    with conn:
        cursor = conn.cursor()
        cursor.execute("UPDATE friends SET is_active = ? WHERE telegram_id = ?", (1 if is_active else 0, telegram_id))
        count = cursor.rowcount
    conn.close()
    return count > 0

# Announcement Operations
def create_announcement(
    sender_telegram_id: int,
    sender_username: Optional[str],
    sender_display_name: str,
    content_type: str,
    text_content: Optional[str],
    media_group_id: Optional[str],
    media_items: List[Dict[str, Any]]
) -> int:
    conn = get_connection()
    now = datetime.utcnow().strftime("%Y-%m-%d %H:%M:%S")
    media_json = json.dumps(media_items or [])
    clean_username = sender_username.replace("@", "") if sender_username else None

    with conn:
        cursor = conn.cursor()
        cursor.execute("""
            INSERT INTO announcements 
            (sender_telegram_id, sender_username, sender_display_name, content_type, text_content, media_group_id, media_items_json, status, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDING', ?, ?)
        """, (sender_telegram_id, clean_username, sender_display_name, content_type, text_content, media_group_id, media_json, now, now))
        announcement_id = cursor.lastrowid
    conn.close()
    add_log("INFO", "SUBMISSION", f"New submission #{announcement_id} from Telegram ID {sender_telegram_id} ({content_type})", None)
    return announcement_id

def get_announcements(
    status: Optional[str] = None,
    search: Optional[str] = None,
    limit: int = 50,
    offset: int = 0
) -> List[Dict[str, Any]]:
    conn = get_connection()
    cursor = conn.cursor()
    query = "SELECT * FROM announcements WHERE 1=1"
    params: List[Any] = []

    if status and status.upper() != "ALL":
        query += " AND status = ?"
        params.append(status.upper())

    if search:
        query += " AND (text_content LIKE ? OR sender_display_name LIKE ? OR sender_username LIKE ? OR CAST(sender_telegram_id AS TEXT) LIKE ?)"
        like = f"%{search}%"
        params.extend([like, like, like, like])

    query += " ORDER BY id DESC LIMIT ? OFFSET ?"
    params.extend([limit, offset])

    cursor.execute(query, params)
    rows = []
    for r in cursor.fetchall():
        d = dict(r)
        d["media_items"] = json.loads(d["media_items_json"] or "[]")
        if d.get("published_message_ids_json"):
            d["published_message_ids"] = json.loads(d["published_message_ids_json"])
        else:
            d["published_message_ids"] = []
        rows.append(d)
    conn.close()
    return rows

def get_announcement_by_id(announcement_id: int) -> Optional[Dict[str, Any]]:
    conn = get_connection()
    cursor = conn.cursor()
    cursor.execute("SELECT * FROM announcements WHERE id = ?", (announcement_id,))
    row = cursor.fetchone()
    conn.close()
    if not row:
        return None
    d = dict(row)
    d["media_items"] = json.loads(d["media_items_json"] or "[]")
    if d.get("published_message_ids_json"):
        d["published_message_ids"] = json.loads(d["published_message_ids_json"])
    else:
        d["published_message_ids"] = []
    return d

def approve_announcement_atomic(announcement_id: int, published_message_ids: List[int], published_by: str = "Admin") -> bool:
    """Atomic approval with duplicate protection."""
    conn = get_connection()
    now = datetime.utcnow().strftime("%Y-%m-%d %H:%M:%S")
    msg_json = json.dumps(published_message_ids)
    with conn:
        cursor = conn.cursor()
        cursor.execute("""
            UPDATE announcements
            SET status = 'APPROVED',
                published_at = ?,
                published_by = ?,
                published_message_ids_json = ?,
                updated_at = ?
            WHERE id = ? AND status = 'PENDING'
        """, (now, published_by, msg_json, now, announcement_id))
        updated = cursor.rowcount > 0
    conn.close()
    if updated:
        add_log("SUCCESS", "PUBLISH", f"Approved & published announcement #{announcement_id} to channel by {published_by}", f"Channel message IDs: {msg_json}")
    return updated

def reject_announcement_atomic(announcement_id: int, rejection_reason: str, admin_notes: Optional[str] = None) -> bool:
    """Atomic rejection with rejection reason."""
    conn = get_connection()
    now = datetime.utcnow().strftime("%Y-%m-%d %H:%M:%S")
    with conn:
        cursor = conn.cursor()
        cursor.execute("""
            UPDATE announcements
            SET status = 'REJECTED',
                rejection_reason = ?,
                admin_notes = ?,
                updated_at = ?
            WHERE id = ? AND status = 'PENDING'
        """, (rejection_reason, admin_notes, now, announcement_id))
        updated = cursor.rowcount > 0
    conn.close()
    if updated:
        add_log("WARN", "MODERATION", f"Rejected announcement #{announcement_id}", f"Reason: {rejection_reason}")
    return updated

def edit_announcement_content(announcement_id: int, new_text: str, admin_notes: Optional[str] = None) -> bool:
    conn = get_connection()
    now = datetime.utcnow().strftime("%Y-%m-%d %H:%M:%S")
    with conn:
        cursor = conn.cursor()
        cursor.execute("""
            UPDATE announcements
            SET text_content = ?,
                admin_notes = ?,
                updated_at = ?
            WHERE id = ? AND status = 'PENDING'
        """, (new_text, admin_notes, now, announcement_id))
        updated = cursor.rowcount > 0
    conn.close()
    if updated:
        add_log("INFO", "EDIT", f"Administrator edited announcement #{announcement_id} content", None)
    return updated

# Dashboard Stats - strictly calculated from real records
def get_dashboard_stats() -> Dict[str, Any]:
    conn = get_connection()
    cursor = conn.cursor()
    cursor.execute("""
        SELECT 
            COUNT(*) as total,
            SUM(CASE WHEN status = 'PENDING' THEN 1 ELSE 0 END) as pending,
            SUM(CASE WHEN status = 'APPROVED' THEN 1 ELSE 0 END) as published,
            SUM(CASE WHEN status = 'REJECTED' THEN 1 ELSE 0 END) as rejected
        FROM announcements
    """)
    row = cursor.fetchone()
    ann_stats = dict(row) if row else {"total": 0, "pending": 0, "published": 0, "rejected": 0}

    cursor.execute("SELECT COUNT(*) FROM friends WHERE is_active = 1")
    friend_count = cursor.fetchone()[0]

    cursor.execute("SELECT * FROM system_logs ORDER BY id DESC LIMIT 5")
    recent_logs = [dict(r) for r in cursor.fetchall()]

    conn.close()
    return {
        "total_announcements": ann_stats["total"] or 0,
        "pending_announcements": ann_stats["pending"] or 0,
        "published_announcements": ann_stats["published"] or 0,
        "rejected_announcements": ann_stats["rejected"] or 0,
        "total_authorized_friends": friend_count or 0,
        "recent_activity": recent_logs
    }

# Logging
def add_log(level: str, category: str, message: str, details: Optional[str] = None):
    try:
        conn = get_connection()
        now = datetime.utcnow().strftime("%Y-%m-%d %H:%M:%S")
        with conn:
            conn.execute("""
                INSERT INTO system_logs (timestamp, level, category, message, details)
                VALUES (?, ?, ?, ?, ?)
            """, (now, level, category, message, details))
        conn.close()
    except Exception as e:
        print(f"Log error: {e}")

def get_logs(limit: int = 50) -> List[Dict[str, Any]]:
    conn = get_connection()
    cursor = conn.cursor()
    cursor.execute("SELECT * FROM system_logs ORDER BY id DESC LIMIT ?", (limit,))
    rows = [dict(r) for r in cursor.fetchall()]
    conn.close()
    return rows

def clear_all_tables():
    """Wipes all data completely for a clean state."""
    conn = get_connection()
    with conn:
        conn.execute("DELETE FROM announcements")
        conn.execute("DELETE FROM friends")
        conn.execute("DELETE FROM system_logs")
    conn.close()

def export_all_data() -> Dict[str, Any]:
    conn = get_connection()
    cursor = conn.cursor()
    cursor.execute("SELECT * FROM friends")
    friends = [dict(r) for r in cursor.fetchall()]
    cursor.execute("SELECT * FROM announcements")
    announcements = [dict(r) for r in cursor.fetchall()]
    cursor.execute("SELECT * FROM system_logs")
    logs = [dict(r) for r in cursor.fetchall()]
    conn.close()
    return {
        "version": 1,
        "exported_at": datetime.utcnow().strftime("%Y-%m-%d %H:%M:%S"),
        "friends": friends,
        "announcements": announcements,
        "logs": logs
    }
