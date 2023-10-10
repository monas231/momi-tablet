package kr.co.jness.momi.eclean.model

import com.google.gson.annotations.SerializedName

/**
 */
data class NoticeVO(
    @SerializedName("BLOCK_TYPE")
    val blockType : String
) {
}