package kr.co.jness.momi.eclean.dto

import com.google.gson.annotations.SerializedName
import kr.co.jness.momi.eclean.model.RuleInfoVO

data class RuleInfoDto(
    @SerializedName("DATA") val data : RuleInfoVO
) : BaseDto()