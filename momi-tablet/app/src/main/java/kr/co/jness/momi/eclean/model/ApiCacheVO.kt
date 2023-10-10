package kr.co.jness.momi.eclean.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "api_cache")
data class ApiCacheVO(
    @ColumnInfo(name = "info")
    val info: String,
    @ColumnInfo(name = "type")
    val type: Int,
    @ColumnInfo(name = "block")
    val block: Int,
    @ColumnInfo(name = "update_time")
    val updateTime: Long,
    @ColumnInfo(name = "reserved_1", defaultValue = "NULL")
    val reserved1: String?=null,
    @ColumnInfo(name = "reserved_2", defaultValue = "NULL")
    val reserved2: String?=null,
    @ColumnInfo(name = "reserved_3", defaultValue = "NULL")
    val reserved3: Long?=0,
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
)
