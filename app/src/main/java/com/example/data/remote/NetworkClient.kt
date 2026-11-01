package com.example.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object NetworkClient {
    private var currentUrl: String = ""
    private var cachedService: SolixxApiService? = null

    private val moshi = Moshi.Builder()
        .add(FlexibleBooleanAdapter())
        .add(FlexibleLongAdapter())
        .add(KotlinJsonAdapterFactory())
        .build()

    fun getService(baseUrl: String): SolixxApiService {
        val trimmed = baseUrl.trim()
        val withScheme = if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            "http://$trimmed"
        } else {
            trimmed
        }
        val sanitizedUrl = if (withScheme.endsWith("/")) withScheme else "$withScheme/"
        if (cachedService != null && currentUrl == sanitizedUrl) {
            return cachedService!!
        }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(sanitizedUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        currentUrl = sanitizedUrl
        cachedService = retrofit.create(SolixxApiService::class.java)
        return cachedService!!
    }
}
