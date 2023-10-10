package kr.co.jness.momi.eclean.dto

import com.google.gson.annotations.SerializedName
import kr.co.jness.momi.eclean.model.AppInstall

data class AppInstallDto(@SerializedName("DATA") val data : AppInstall) : BaseDto()