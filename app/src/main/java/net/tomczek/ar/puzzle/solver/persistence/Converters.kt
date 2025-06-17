package net.tomczek.ar.puzzle.solver.persistence

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import jakarta.inject.Inject

class Converters {

    @Inject lateinit var gson: Gson

    @TypeConverter
    fun fromIntList(value: List<Int>): String = gson.toJson(value)

    @TypeConverter
    fun toIntList(value: String): List<Int> =
        gson.fromJson(value, object : TypeToken<List<Int>>() {}.type)

    @TypeConverter
    fun fromBooleanList(value: List<Boolean>): String = gson.toJson(value)

    @TypeConverter
    fun toBooleanList(value: String): List<Boolean> =
        gson.fromJson(value, object : TypeToken<List<Boolean>>() {}.type)
}