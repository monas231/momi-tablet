package kr.co.jness.momi.eclean.model

import com.google.gson.annotations.SerializedName

/**
 * block: 차단여부 (1:차단,0:차단안함)
 * blockType: 차단타입
 * blockInfo: 차단정보
 */
data class BlockInfo(
    @SerializedName("BLOCK")
    val block : Int,
    @SerializedName("BLOCK_TYPE")
    val blockType : String,
    @SerializedName("BLOCK_INFO")
    val blockInfo : String
) {
    fun isBlockApp() : Boolean {
        return block == 1
    }
}