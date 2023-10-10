package kr.co.jness.momi.eclean.dto

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName
import kr.co.jness.momi.eclean.model.BlockInfo
import kr.co.jness.momi.eclean.model.RuleInfoVO

data class SchoolDto(
    @SerializedName("SCHOOL_LIST") val schoolList : List<School>)
    : BaseDto()

data class School(
    @SerializedName("address") val address : String,
    @SerializedName("name") val name : String,
    @SerializedName("id") val id : Long
)
