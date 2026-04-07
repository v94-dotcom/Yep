package com.yep.app.data

import androidx.room.TypeConverter
import org.json.JSONArray

class Converters {
    @TypeConverter
    fun fromStringList(paths: List<String>?): String? {
        if (paths.isNullOrEmpty()) return null
        return JSONArray(paths).toString()
    }

    @TypeConverter
    fun toStringList(json: String?): List<String>? {
        if (json == null) return null
        val array = JSONArray(json)
        return (0 until array.length()).map { array.getString(it) }
    }
}
