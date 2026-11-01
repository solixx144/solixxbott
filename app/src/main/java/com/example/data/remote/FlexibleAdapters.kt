package com.example.data.remote

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

/**
 * Resilient JSON adapter for Boolean values.
 * Safely parses numbers (1/0), booleans (true/false), and string representations without throwing JsonDataException.
 */
class FlexibleBooleanAdapter {
    @FromJson
    fun fromJson(reader: JsonReader): Boolean {
        return when (reader.peek()) {
            JsonReader.Token.BOOLEAN -> reader.nextBoolean()
            JsonReader.Token.NUMBER -> reader.nextInt() != 0
            JsonReader.Token.STRING -> {
                val str = reader.nextString().trim()
                str.equals("true", ignoreCase = true) || str == "1"
            }
            JsonReader.Token.NULL -> {
                reader.nextNull<Unit>()
                false
            }
            else -> {
                reader.skipValue()
                false
            }
        }
    }

    @ToJson
    fun toJson(writer: JsonWriter, value: Boolean) {
        writer.value(value)
    }
}

/**
 * Resilient JSON adapter for Long values (such as Telegram IDs).
 * Safely parses numbers, strings, and handles null/missing values gracefully.
 */
class FlexibleLongAdapter {
    @FromJson
    fun fromJson(reader: JsonReader): Long {
        return when (reader.peek()) {
            JsonReader.Token.NUMBER -> reader.nextLong()
            JsonReader.Token.STRING -> {
                val str = reader.nextString().trim()
                str.toLongOrNull() ?: 0L
            }
            JsonReader.Token.NULL -> {
                reader.nextNull<Unit>()
                0L
            }
            else -> {
                reader.skipValue()
                0L
            }
        }
    }

    @ToJson
    fun toJson(writer: JsonWriter, value: Long) {
        writer.value(value)
    }
}
