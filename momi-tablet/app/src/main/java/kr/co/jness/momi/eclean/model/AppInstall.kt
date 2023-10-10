package kr.co.jness.momi.eclean.model

import com.google.gson.annotations.SerializedName

data class AppInstall(
    @SerializedName("ID")
    val deviceId : Long,
    @SerializedName("SCHOOL_ID")
    val schoolId : Int,
    @SerializedName("FLAG")
    val flag : Int
)