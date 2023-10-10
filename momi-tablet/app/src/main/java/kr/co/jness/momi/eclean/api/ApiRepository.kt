package kr.co.jness.momi.eclean.api

import io.reactivex.rxjava3.core.Single
import kr.co.jness.momi.eclean.BuildConfig
import kr.co.jness.momi.eclean.dto.*
import kr.co.jness.momi.eclean.model.*
import kr.co.jness.momi.eclean.utils.DeviceInfo
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ApiRepository @Inject constructor() {

    private val api by lazy {
        ServiceFactory.getApi(BuildConfig.SERVER_URL)
    }

    private fun getToday() = SimpleDateFormat("yyyyMMdd").format(Date())

    fun notifyInstallApp(): Single<AppInstallDto> =
        api.notifyInstallApp(DeviceInfo.getDeviceUniqueId(), BuildConfig.VERSION_NAME)

    fun checkApp(deviceId: Long, schoolId: Int?, pkg: String): Single<BaseResponseDto<BlockInfo>> =
        api.checkApp(deviceId, schoolId, pkg, getToday())

    fun checkUrl(deviceId: Long, schoolId: Int?, check: UrlCheck): Single<BaseResponseDto<BlockInfo>> =
        api.checkUrl(deviceId, schoolId, check.domain, check.encDomain, check.subDir, check.all, getToday())

    fun getFileDownload(url: String) = api.getFileDownload(BuildConfig.SERVER_URL + url)

    fun sendPing(deviceId: Long) =
        api.sendPing(deviceId, BuildConfig.VERSION_NAME)

    fun requestRuleInfo(deviceId: Long, schoolId: Int?): Single<RuleInfoDto> =
        api.requestRuleInfo(deviceId, schoolId)

    fun registerFCMToken(deviceId: Long, schoolId: Int?, token: String): Single<BaseDto> =
        api.registerFCMToken(deviceId, schoolId, token)

    fun testFCM(title: String, msg: String, topic: String? = null, token: String? = null) =
        api.testFCM(title, msg, topic, token)

    fun blockFile(deviceId: Long, schoolId: Int?, blockInfo: String) =
        api.blockFile(deviceId, schoolId, blockInfo, getToday())

    fun blockTime(deviceId: Long, schoolId: Int?, blockInfo: String) =
        api.blockTime(deviceId, schoolId, blockInfo, getToday())


    fun checkLicense(deviceId: Long, key: String, s_id: Long?=null, s_nm: String?=null, mail: String?=null, phone: String?=null): Single<BaseResponseDto<AppLicense>> =
        api.checkLicense(deviceId, key, s_id, s_nm, mail, phone)


    fun noticeInfo(deviceId: Long, schoolId: Int?): Single<BaseResponseDto<NoticeInfoVO>> =
        api.noticeInfo(deviceId, schoolId)


    fun checkFile(deviceId: Long, schoolId: Int?): Single<BaseResponseDto<CheckFile>> =
        api.checkFile(deviceId, schoolId)

    fun checkKeyword(deviceId: Long, schoolId: Int?): Single<BaseResponseDto<KeyWordDto>> =
        api.checkKeyword(deviceId, schoolId)

    fun searchSchool(deviceId: Long, name: String): Single<BaseResponseDto<SchoolDto>> =
        api.searchSchool(deviceId, name)

    fun sendQuestion(device_id: Long, name: String?, type: Int?, mail: String?, phone: String?, q_type: Int?, text: String?): Single<BaseResponseDto<Void>> =
        api.sendQuestion(device_id, name, type, mail, phone, q_type, text)

}

