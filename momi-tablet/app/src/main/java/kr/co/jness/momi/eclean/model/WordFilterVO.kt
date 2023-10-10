package kr.co.jness.momi.eclean.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "word_filter")
data class WordFilterVO(
    @ColumnInfo(name = "version")
    val version: Int,
    @ColumnInfo(name = "filepath")
    val filepath: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "score")
    val score: Int,
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
)
