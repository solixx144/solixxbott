from typing import List, Optional, Any, Dict
from pydantic import BaseModel, Field

class FriendCreate(BaseModel):
    telegram_id: int = Field(..., description="Telegram numeric user ID")
    display_name: Optional[str] = Field(None, description="Optional friend label or alias")
    username: Optional[str] = Field(None, description="Actual Telegram username without @ (if known)")
    notes: Optional[str] = Field(None, description="Optional admin notes")

class FriendResponse(BaseModel):
    id: int
    telegram_id: int
    username: Optional[str] = None
    display_name: str
    added_at: str
    is_active: int
    notes: Optional[str] = None
    submission_count: Optional[int] = 0

class MediaItem(BaseModel):
    file_id: str
    file_unique_id: Optional[str] = None
    type: str  # 'photo', 'video', 'audio', 'voice'
    caption: Optional[str] = None
    width: Optional[int] = None
    height: Optional[int] = None
    duration: Optional[int] = None
    file_size: Optional[int] = None
    url: Optional[str] = None

class AnnouncementResponse(BaseModel):
    id: int
    sender_telegram_id: int
    sender_username: Optional[str] = None
    sender_display_name: str
    content_type: str
    text_content: Optional[str] = None
    media_group_id: Optional[str] = None
    media_items: List[Dict[str, Any]] = []
    status: str
    created_at: str
    updated_at: str
    published_at: Optional[str] = None
    published_message_ids: List[int] = []
    rejection_reason: Optional[str] = None
    admin_notes: Optional[str] = None
    published_by: Optional[str] = None

class ApproveRequest(BaseModel):
    published_by: Optional[str] = None

class RejectRequest(BaseModel):
    rejection_reason: str = Field(..., min_length=1, description="Explanation sent privately to friend")
    admin_notes: Optional[str] = None

class EditRequest(BaseModel):
    new_text: str = Field(..., description="Edited announcement text or media caption")
    admin_notes: Optional[str] = None

class SystemStatusResponse(BaseModel):
    online: bool
    bot_connected: bool
    target_channel: str
    total_announcements: int
    pending_announcements: int
    published_announcements: int
    rejected_announcements: int
    total_friends: int
    server_time: str
    database_ok: bool
