package com.example.data.remote

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.ImageRequest
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object ImageLoaderHelper {

    private var cachedApiKey: String = ""
    private var imageLoader: ImageLoader? = null

    fun getAuthenticatedImageLoader(context: Context, apiKey: String): ImageLoader {
        if (imageLoader != null && cachedApiKey == apiKey) {
            return imageLoader!!
        }

        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .header("User-Agent", "SolixxBot-Android/1.0")

            if (apiKey.isNotBlank()) {
                requestBuilder.header("X-Admin-Key", apiKey)
            }

            chain.proceed(requestBuilder.build())
        }

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .writeTimeout(25, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .build()

        val loader = ImageLoader.Builder(context.applicationContext)
            .okHttpClient(okHttpClient)
            .memoryCache {
                MemoryCache.Builder(context.applicationContext)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.applicationContext.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.05)
                    .build()
            }
            .crossfade(true)
            .build()

        cachedApiKey = apiKey
        imageLoader = loader
        return loader
    }

    fun buildImageRequest(
        context: Context,
        imageUrl: String,
        apiKey: String
    ): ImageRequest {
        val trimmed = imageUrl.trim()
        val builder = ImageRequest.Builder(context)
            .data(trimmed)
            .crossfade(true)

        if (apiKey.isNotBlank()) {
            builder.setHeader("X-Admin-Key", apiKey)
        }

        return builder.build()
    }
}
