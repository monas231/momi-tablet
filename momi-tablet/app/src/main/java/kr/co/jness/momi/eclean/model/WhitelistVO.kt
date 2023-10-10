package kr.co.jness.momi.eclean.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "whitelist")
data class WhitelistVO(
    @ColumnInfo(name = "version")
    val version: Int,
    @ColumnInfo(name = "filepath")
    val filepath: String,
    @ColumnInfo(name = "info")
    val info: String,
    @ColumnInfo(name = "type")
    val type: Int,
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
)
