package kr.co.jness.momi.eclean.model

import com.google.gson.annotations.SerializedName

data class CheckFile(
    @SerializedName("FILE_LIST")
    val fileList : List<FileInfo>
)

data class FileInfo(
    @SerializedName("path")
    val path : String,
    @SerializedName("name")
    val name : String,
    @SerializedName("type")
    val type : Int,
    @SerializedName("version")
    val version : Int
)