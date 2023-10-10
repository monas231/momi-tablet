package kr.co.jness.momi.eclean.dto

import com.google.gson.annotations.SerializedName

abstract class BaseDto {
    @SerializedName("RESULT_CODE")
    val resultCode: Int = 0
    @SerializedName("CMD")
    val cmd: String = ""
}