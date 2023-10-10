package kr.co.jness.momi.eclean.dto

import com.google.gson.annotations.SerializedName

data class PingDto(
    @SerializedName("DATA") val data : PingInfo
) : BaseDto()

data class PingInfo(
    @SerializedName("device_id")
    val deviceId: Long,
    @SerializedName("school_id")
    val schoolId: Int,
    /**
     * 정책 FLAG가 바뀌면 차단 정책정보 (rule_info) 호출하여 정책 갱신
     */
    val flag: Int
)