package kr.co.jness.momi.eclean.model

import com.google.gson.annotations.SerializedName

data class AppLicense(
    @SerializedName("SCHOOL_ID")
    val schoolId : Int,
    @SerializedName("EXPIRED_YMD")
    val expiredYMD : String
)