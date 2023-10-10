package kr.co.jness.momi.eclean.dto

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName
import kr.co.jness.momi.eclean.model.RuleInfoVO

data class KeyWordDto(
    @SerializedName("KEYWORD_LIST")
    val keywordList : List<KeyWord>
)

data class KeyWord(
    @SerializedName("info")
    val info: String,
    @SerializedName("score")
    val score: Int
)