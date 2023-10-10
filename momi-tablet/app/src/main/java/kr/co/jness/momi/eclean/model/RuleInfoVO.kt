package kr.co.jness.momi.eclean.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.*

@Entity(tableName = "rule_info")
data class RuleInfoVO(

    @ColumnInfo(name = "block_video")
    @SerializedName("BLOCK_VIDEO")
    val blockVideo: String,

    @ColumnInfo(name = "block_site")
    @SerializedName("BLOCK_SITE")
    val blockSite: String,

    @ColumnInfo(name = "block_proxy")
    @SerializedName("BLOCK_PROXY")
    val blockProxy: String,

    @ColumnInfo(name = "block_game_site")
    @SerializedName("BLOCK_GAME_SITE")
    val blockGameSite: String,

    @ColumnInfo(name = "block_school_site")
    @SerializedName("BLOCK_SCHOOL_SITE")
    val blockSchoolSite: String,

    @ColumnInfo(name = "block_app")
    @SerializedName("BLOCK_APP")
    val blockApp: String,

    @ColumnInfo(name = "block_school_app")
    @SerializedName("BLOCK_SCHOOL_APP")
    val blockSchoolApp: String,

    @ColumnInfo(name = "block_file_type")
    @SerializedName("BLOCK_FILE_TYPE")
    val blockFileType: List<FileType>?,

    @ColumnInfo(name = "sync_time")
    @SerializedName("SYNC_TIME")
    val syncTime: String,

    @ColumnInfo(name = "use_class_time")
    @SerializedName("USE_CLASS_TIME")
    val useClassTime: String,

    @ColumnInfo(name = "use_night")
    @SerializedName("USE_NIGHT")
    val useNight: String,

    @ColumnInfo(name = "class_times")
    @SerializedName("CLASS_TIMES")
    val classTimes: List<ClassTimes>?,

    @ColumnInfo(name = "allow_infos")
    @SerializedName("ALLOW_INFOS")
    val allowInfos: List<Os>?,

    @ColumnInfo(name = "block_score")
    @SerializedName("BLOCK_SCORE")
    val blockScore: Int,

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
) {
    @ColumnInfo(name = "updated_at")
    var updatedAt: Date? = null
}