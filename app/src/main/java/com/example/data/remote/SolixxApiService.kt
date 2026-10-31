package com.example.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SolixxApiService {
    @GET("api/status")
    suspend fun getStatus(): Response<SystemStatusDto>

    @GET("api/announcements")
    suspend fun getAnnouncements(
        @Header("X-Admin-Key") apiKey: String,
        @Header("X-Admin-Telegram-Id") adminTgId: String? = null,
        @Query("status") status: String = "ALL",
        @Query("search") search: String? = null
    ): Response<List<AnnouncementDto>>

    @GET("api/announcements/{id}")
    suspend fun getAnnouncement(
        @Header("X-Admin-Key") apiKey: String,
        @Header("X-Admin-Telegram-Id") adminTgId: String? = null,
        @Path("id") id: Long
    ): Response<AnnouncementDto>

    @POST("api/announcements/{id}/approve")
    suspend fun approveAnnouncement(
        @Header("X-Admin-Key") apiKey: String,
        @Header("X-Admin-Telegram-Id") adminTgId: String? = null,
        @Path("id") id: Long,
        @Body body: ApproveDto
    ): Response<ApiResponseDto>

    @POST("api/announcements/{id}/reject")
    suspend fun rejectAnnouncement(
        @Header("X-Admin-Key") apiKey: String,
        @Header("X-Admin-Telegram-Id") adminTgId: String? = null,
        @Path("id") id: Long,
        @Body body: RejectDto
    ): Response<ApiResponseDto>

    @POST("api/announcements/{id}/edit")
    suspend fun editAnnouncement(
        @Header("X-Admin-Key") apiKey: String,
        @Header("X-Admin-Telegram-Id") adminTgId: String? = null,
        @Path("id") id: Long,
        @Body body: EditDto
    ): Response<ApiResponseDto>

    @GET("api/friends")
    suspend fun getFriends(
        @Header("X-Admin-Key") apiKey: String,
        @Header("X-Admin-Telegram-Id") adminTgId: String? = null,
        @Query("search") search: String? = null
    ): Response<List<FriendDto>>

    @POST("api/friends")
    suspend fun addFriend(
        @Header("X-Admin-Key") apiKey: String,
        @Header("X-Admin-Telegram-Id") adminTgId: String? = null,
        @Body body: FriendCreateDto
    ): Response<ApiResponseDto>

    @DELETE("api/friends/{telegramId}")
    suspend fun removeFriend(
        @Header("X-Admin-Key") apiKey: String,
        @Header("X-Admin-Telegram-Id") adminTgId: String? = null,
        @Path("telegramId") telegramId: Long
    ): Response<ApiResponseDto>

    @PATCH("api/friends/{telegramId}/toggle")
    suspend fun toggleFriend(
        @Header("X-Admin-Key") apiKey: String,
        @Header("X-Admin-Telegram-Id") adminTgId: String? = null,
        @Path("telegramId") telegramId: Long,
        @Query("active") active: Boolean
    ): Response<ApiResponseDto>

    @GET("api/logs")
    suspend fun getLogs(
        @Header("X-Admin-Key") apiKey: String,
        @Header("X-Admin-Telegram-Id") adminTgId: String? = null,
        @Query("limit") limit: Int = 50
    ): Response<List<LogDto>>

    @POST("api/clear-data")
    suspend fun clearRemoteData(
        @Header("X-Admin-Key") apiKey: String,
        @Header("X-Admin-Telegram-Id") adminTgId: String? = null
    ): Response<ApiResponseDto>
}
