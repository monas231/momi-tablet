package kr.co.jness.momi.eclean.dto

import com.google.gson.annotations.SerializedName
import kr.co.jness.momi.eclean.model.BlockInfo

data class BlockInfoDto(@SerializedName("DATA") val data : BlockInfo) : BaseDto()