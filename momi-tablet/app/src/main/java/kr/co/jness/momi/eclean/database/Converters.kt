package kr.co.jness.momi.eclean.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import kr.co.jness.momi.eclean.model.ClassTimes
import kr.co.jness.momi.eclean.model.FileType
import kr.co.jness.momi.eclean.model.Os
import java.util.*

class Converters {

    @TypeConverter
    fun osToJson(value: List<Os>?): String? = value?.let { Gson().toJson(it) }

    @TypeConverter
    fun jsonToOs(value: String?) = value?.let { Gson().fromJson(it, Array<Os>::class.java).toList() }

    @TypeConverter
    fun classTimesToJson(value: List<ClassTimes>?): String? = value?.let { Gson().toJson(it) }

    @TypeConverter
    fun jsonToClassTimes(value: String?) = value?.let { Gson().fromJson(it, Array<ClassTimes>::class.java).toList() }

    @TypeConverter
    fun fileTypeToJson(value: List<FileType>?): String? = value?.let { Gson().toJson(it) }

    @TypeConverter
    fun jsonToFileType(value: String?): List<FileType>? = value?.let { Gson().fromJson(it, Array<FileType>::class.java).toList() }

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

}