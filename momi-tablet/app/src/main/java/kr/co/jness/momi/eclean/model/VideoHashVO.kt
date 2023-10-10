package kr.co.jness.momi.eclean.model

import androidx.room.ColumnInfo
import androidx.room.ColumnInfo.NOCASE
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "video_hashs")
data class VideoHashVO(
    @ColumnInfo(name = "version")
    val version: Int,
    @ColumnInfo(name = "hash_string", collate = NOCASE)
    val hashString: String,
    @ColumnInfo(name = "filepath")
    val filepath: String,
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
)
