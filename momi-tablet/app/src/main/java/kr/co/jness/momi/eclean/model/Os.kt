package kr.co.jness.momi.eclean.model

import com.google.gson.annotations.SerializedName

data class Os(
    /**
     * 허용 타입. 1: 사이트, 2: 앱.
     */
    @SerializedName("type")
    val type : Int,
    /**
     * 허용 정보. 도메인, 패키지명.
     */
    @SerializedName("info")
    val info : String,
    /**
     * 허용 앱 이름.
     */
    val name: String?,
    /**
     * 1: 게임, 2: 교육, 3:, 인강, 4: 기타.
     */
    val icon: String?,
    /**
     * 비고 정보.
     */
    val desc: String?
) {
    fun getIconName() =
        icon?.let {
            when(it.toInt()) {
                1 -> "게임"
                2 -> "교육"
                3 -> "인강"
                4 -> "기타"
                else -> ""
            }
        } ?: kotlin.run {
            ""
        }
}