package kr.co.jness.momi.eclean.dto

import com.google.gson.annotations.SerializedName

data class BaseResponseDto<T>(
    @SerializedName("DATA") val data: T
) : BaseDto()