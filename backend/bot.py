import asyncio
import json
import logging
from typing import Dict, List, Optional, Any
from telegram import Update, InputMediaPhoto, InputMediaVideo, Bot
from telegram.ext import (
    Application,
    ApplicationBuilder,
    CommandHandler,
    MessageHandler,
    ContextTypes,
    filters,
)
import config
import database

logger = logging.getLogger("SolixxBot")
logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")

# Global reference to running bot application
bot_app: Optional[Application] = None

# Media group buffer: media_group_id -> dict of collected items and task
media_group_buffers: Dict[str, Dict[str, Any]] = {}

async def start_command(update: Update, context: ContextTypes.DEFAULT_TYPE):
    user = update.effective_user
    if not user:
        return
    
    is_auth = database.is_friend_authorized(user.id)
    if is_auth:
        database.update_friend_telegram_info(user.id, user.username, user.full_name or user.first_name)
        await update.message.reply_text(
            f"👋 Merhaba {user.first_name}!\n\n"
            f"✅ **SolixxBot Yetkili Duyuru Arayüzü**\n"
            f"Telegram ID'niz: `{user.id}`\n\n"
            f"Bu bota göndereceğiniz tüm metin, fotoğraf, albüm, video ve ses kayıtları "
            f"doğrudan yönetici onay sırasına iletilir ve onaylandığında **{config.TARGET_CHANNEL}** kanalında yayımlanır.\n\n"
            f"📌 **Nasıl kullanılır?**\n"
            f"• Duyurmak istediğiniz metni veya medyayı doğrudan bu sohbete gönderin.\n"
            f"• Albüm (çoklu fotoğraf/video) gönderebilirsiniz, sıralamaları ve açıklamaları korunacaktır.\n"
            f"• Onay veya ret durumunda bot size anında özel bildirim gönderecektir.",
            parse_mode="Markdown"
        )
    else:
        await update.message.reply_text(
            f"⚠️ **Erişim Kısıtlı**\n\n"
            f"Merhaba {user.first_name}. Telegram ID'niz: `{user.id}`\n\n"
            f"Bu bot yalnızca yetkilendirilmiş kişiler tarafından kullanılabilir. "
            f"**{config.TARGET_CHANNEL}** kanalına duyuru iletebilmek için lütfen Telegram ID'nizi kanal yöneticisine ileterek sisteme eklenmesini isteyin.",
            parse_mode="Markdown"
        )

async def handle_incoming_message(update: Update, context: ContextTypes.DEFAULT_TYPE):
    msg = update.effective_message
    user = update.effective_user
    if not msg or not user:
        return

    # Check friend authorization strictly by numeric Telegram ID
    if not database.is_friend_authorized(user.id):
        database.add_log("WARN", "AUTH", f"Unauthorized submission attempt from Telegram ID: {user.id}", None)
        await msg.reply_text(
            "⚠️ **Yetkisiz İşlem**\n"
            f"Duyuru gönderme yetkiniz bulunmamaktadır. Lütfen yönetici ile iletişime geçin. (ID: `{user.id}`)",
            parse_mode="Markdown"
        )
        return

    # Sync real Telegram details
    database.update_friend_telegram_info(user.id, user.username, user.full_name or user.first_name)

    # Check if this message is part of an album/media group
    if msg.media_group_id:
        await process_media_group_item(update, context)
        return

    # Single message submission - using real Telegram user identity only
    sender_name = user.full_name or user.first_name or f"User {user.id}"
    sender_user = user.username or None

    content_type = "TEXT"
    text_content = msg.text or msg.caption or ""
    media_items: List[Dict[str, Any]] = []

    if msg.photo:
        content_type = "PHOTO"
        best_photo = msg.photo[-1]
        media_items.append({
            "file_id": best_photo.file_id,
            "file_unique_id": best_photo.file_unique_id,
            "type": "photo",
            "caption": msg.caption or "",
            "width": best_photo.width,
            "height": best_photo.height
        })
    elif msg.video:
        content_type = "VIDEO"
        media_items.append({
            "file_id": msg.video.file_id,
            "file_unique_id": msg.video.file_unique_id,
            "type": "video",
            "caption": msg.caption or "",
            "width": msg.video.width,
            "height": msg.video.height,
            "duration": msg.video.duration
        })
    elif msg.audio:
        content_type = "AUDIO"
        media_items.append({
            "file_id": msg.audio.file_id,
            "file_unique_id": msg.audio.file_unique_id,
            "type": "audio",
            "caption": msg.caption or "",
            "duration": msg.audio.duration,
            "file_size": msg.audio.file_size
        })
    elif msg.voice:
        content_type = "VOICE"
        media_items.append({
            "file_id": msg.voice.file_id,
            "file_unique_id": msg.voice.file_unique_id,
            "type": "voice",
            "caption": msg.caption or "",
            "duration": msg.voice.duration
        })

    # Save to SQLite database as PENDING
    announcement_id = database.create_announcement(
        sender_telegram_id=user.id,
        sender_username=sender_user,
        sender_display_name=sender_name,
        content_type=content_type,
        text_content=text_content,
        media_group_id=None,
        media_items=media_items
    )

    # Reply to friend
    await msg.reply_text(
        f"✅ **Duyurunuz Alındı! (No: #{announcement_id})**\n\n"
        f"İçeriğiniz yönetici incelemesine sunuldu. Onaylandığında **{config.TARGET_CHANNEL}** kanalında yayımlanacaktır.",
        parse_mode="Markdown"
    )

async def process_media_group_item(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """Debounce and aggregate album/media group messages into a single announcement."""
    msg = update.effective_message
    user = update.effective_user
    gid = msg.media_group_id

    item = None
    if msg.photo:
        best_photo = msg.photo[-1]
        item = {
            "file_id": best_photo.file_id,
            "file_unique_id": best_photo.file_unique_id,
            "type": "photo",
            "caption": msg.caption or "",
            "width": best_photo.width,
            "height": best_photo.height
        }
    elif msg.video:
        item = {
            "file_id": msg.video.file_id,
            "file_unique_id": msg.video.file_unique_id,
            "type": "video",
            "caption": msg.caption or "",
            "width": msg.video.width,
            "height": msg.video.height,
            "duration": msg.video.duration
        }

    if not item:
        return

    if gid not in media_group_buffers:
        media_group_buffers[gid] = {
            "user": user,
            "items": [],
            "caption": msg.caption or "",
            "task": None
        }

    if msg.caption and not media_group_buffers[gid]["caption"]:
        media_group_buffers[gid]["caption"] = msg.caption

    media_group_buffers[gid]["items"].append(item)

    # Cancel existing timer and restart 1.5s debounce
    if media_group_buffers[gid]["task"] and not media_group_buffers[gid]["task"].done():
        media_group_buffers[gid]["task"].cancel()

    media_group_buffers[gid]["task"] = asyncio.create_task(
        finalize_media_group(gid, update, context)
    )

async def finalize_media_group(gid: str, update: Update, context: ContextTypes.DEFAULT_TYPE):
    try:
        await asyncio.sleep(1.5)
        data = media_group_buffers.pop(gid, None)
        if not data:
            return

        user = data["user"]
        items = data["items"]
        caption = data["caption"]

        sender_name = user.full_name or user.first_name or f"User {user.id}"
        sender_user = user.username or None

        announcement_id = database.create_announcement(
            sender_telegram_id=user.id,
            sender_username=sender_user,
            sender_display_name=sender_name,
            content_type="ALBUM",
            text_content=caption,
            media_group_id=gid,
            media_items=items
        )

        await update.effective_message.reply_text(
            f"✅ **Albüm Duyurunuz Alındı! (No: #{announcement_id})**\n\n"
            f"Toplam {len(items)} medya içeren albümünüz yönetici onayına sunuldu. "
            f"Onaylandığında **{config.TARGET_CHANNEL}** kanalında tek parça halinde yayımlanacaktır.",
            parse_mode="Markdown"
        )
    except asyncio.CancelledError:
        pass
    except Exception as e:
        logger.error(f"Error finalizing media group: {e}")

# Telegram Publishing Action
async def publish_announcement(announcement_id: int, published_by: str = "Admin") -> bool:
    ann = database.get_announcement_by_id(announcement_id)
    if not ann or ann["status"] != "PENDING":
        return False

    if not bot_app or not bot_app.bot:
        logger.error("Bot application is not initialized!")
        return False

    bot: Bot = bot_app.bot
    channel = config.TARGET_CHANNEL
    content_type = ann["content_type"]
    text = ann["text_content"] or ""
    media_items = ann.get("media_items", [])
    sent_msg_ids: List[int] = []

    try:
        if content_type == "TEXT":
            msg = await bot.send_message(chat_id=channel, text=text)
            sent_msg_ids.append(msg.message_id)

        elif content_type == "PHOTO" and media_items:
            photo_id = media_items[0]["file_id"]
            msg = await bot.send_photo(chat_id=channel, photo=photo_id, caption=text)
            sent_msg_ids.append(msg.message_id)

        elif content_type == "VIDEO" and media_items:
            video_id = media_items[0]["file_id"]
            msg = await bot.send_video(chat_id=channel, video=video_id, caption=text)
            sent_msg_ids.append(msg.message_id)

        elif content_type == "AUDIO" and media_items:
            audio_id = media_items[0]["file_id"]
            msg = await bot.send_audio(chat_id=channel, audio=audio_id, caption=text)
            sent_msg_ids.append(msg.message_id)

        elif content_type == "VOICE" and media_items:
            voice_id = media_items[0]["file_id"]
            msg = await bot.send_voice(chat_id=channel, voice=voice_id, caption=text)
            sent_msg_ids.append(msg.message_id)

        elif content_type == "ALBUM" and media_items:
            telegram_media = []
            for idx, m in enumerate(media_items):
                item_caption = text if idx == 0 else ""
                if m.get("type") == "video":
                    telegram_media.append(InputMediaVideo(media=m["file_id"], caption=item_caption))
                else:
                    telegram_media.append(InputMediaPhoto(media=m["file_id"], caption=item_caption))

            msgs = await bot.send_media_group(chat_id=channel, media=telegram_media)
            for m in msgs:
                sent_msg_ids.append(m.message_id)

        # Atomic commit in database
        success = database.approve_announcement_atomic(
            announcement_id=announcement_id,
            published_message_ids=sent_msg_ids,
            published_by=published_by
        )

        # Notify the submitting friend privately
        try:
            await bot.send_message(
                chat_id=ann["sender_telegram_id"],
                text=(
                    f"🎉 **Duyurunuz Onaylandı!**\n\n"
                    f"#{announcement_id} numaralı duyurunuz yönetici tarafından onaylandı ve "
                    f"**{config.TARGET_CHANNEL}** kanalında başarıyla paylaşıldı."
                ),
                parse_mode="Markdown"
            )
        except Exception as notify_err:
            logger.warning(f"Could not notify friend {ann['sender_telegram_id']}: {notify_err}")

        return success

    except Exception as e:
        logger.error(f"Failed to publish announcement #{announcement_id} to Telegram: {e}")
        database.add_log("ERROR", "PUBLISH", f"Publish failed for #{announcement_id}: {str(e)}", None)
        return False

# Telegram Rejection Action
async def notify_rejection(announcement_id: int, reason: str, admin_notes: Optional[str] = None) -> bool:
    ann = database.get_announcement_by_id(announcement_id)
    if not ann or ann["status"] != "PENDING":
        return False

    success = database.reject_announcement_atomic(announcement_id, reason, admin_notes)
    if not success:
        return False

    if bot_app and bot_app.bot:
        try:
            await bot_app.bot.send_message(
                chat_id=ann["sender_telegram_id"],
                text=(
                    f"❌ **Duyurunuz Reddedildi**\n\n"
                    f"#{announcement_id} numaralı duyuru başvurunuz yönetici tarafından incelendi ve reddedildi.\n\n"
                    f"**Gerekçe:** {reason}\n\n"
                    f"Gerekli düzenlemeleri yaparak dilerseniz tekrar gönderebilirsiniz."
                ),
                parse_mode="Markdown"
            )
        except Exception as e:
            logger.warning(f"Could not send rejection message to user: {e}")

    return True

async def start_telegram_bot():
    """Initializes and runs the Telegram bot if token is configured."""
    global bot_app
    token = config.TELEGRAM_BOT_TOKEN
    if not token or "example" in token.lower() or token.startswith("123456789:"):
        logger.warning("Telegram Bot Token is not set. Bot listener will stay idle.")
        return None

    try:
        app = ApplicationBuilder().token(token).build()

        app.add_handler(CommandHandler("start", start_command))
        app.add_handler(CommandHandler("help", start_command))
        app.add_handler(MessageHandler(filters.ALL & ~filters.COMMAND, handle_incoming_message))

        await app.initialize()
        await app.start()
        await app.updater.start_polling()

        bot_app = app
        logger.info(f"SolixxBot connected and polling for updates for {config.TARGET_CHANNEL}")
        database.add_log("SUCCESS", "BOT", f"Telegram bot polling active for {config.TARGET_CHANNEL}", None)
        return app
    except Exception as e:
        logger.error(f"Failed to start Telegram Bot: {e}")
        database.add_log("ERROR", "BOT", f"Failed to start Telegram bot: {str(e)}", None)
        return None
