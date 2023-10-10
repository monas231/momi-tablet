package kr.co.jness.momi.eclean.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kr.co.jness.momi.eclean.utils.Logger

@Entity(tableName = "local_whitelist")
data class LocalWhitelistVO(
    @ColumnInfo(name = "info")
    var info: String,
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
)