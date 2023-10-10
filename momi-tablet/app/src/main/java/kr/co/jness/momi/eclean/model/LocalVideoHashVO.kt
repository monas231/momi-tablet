package kr.co.jness.momi.eclean.model

import androidx.room.ColumnInfo
import androidx.room.ColumnInfo.NOCASE
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_video_hashs")
data class LocalVideoHashVO(
    @ColumnInfo(name = "hash", collate = NOCASE)
    val hash: String,
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
)
