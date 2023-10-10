package kr.co.jness.momi.eclean.model

import com.google.gson.annotations.SerializedName

data class NoticeInfoVO(
    /**
     * 공지사항
     */
    @SerializedName("NOTICE")
    val notice: String,
    @SerializedName("SCHOOL_NAME")
    val schoolName: String
)
