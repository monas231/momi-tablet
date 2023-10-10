package kr.co.jness.momi.eclean.common

import android.app.Application
import android.content.Intent
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import kr.co.jness.momi.eclean.service.EcleanService
import kr.co.jness.momi.eclean.service.UrlInterceptorService
import kr.co.jness.momi.eclean.utils.*
import javax.inject.Inject

@HiltAndroidApp
class EcleanApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: EcleanWorkerFactory

    override fun onCreate() {
        super.onCreate()
    }


    private var deviceId = -1L
    private var schoolId = Constant.INVALID_SCHOOL_ID
    var blockScore = 10

    fun resetSchoolId() {
        schoolId = Constant.INVALID_SCHOOL_ID
    }

    /**
     * 감지 기능 동작 제한
     */
    fun stopMonitoring() {
        resetSchoolId()
        LicenseUtil.clearLicense(this)
        UrlInterceptorService.instance?.expiredLicense = true
        stopService(Intent(this, EcleanService::class.java))
    }

    /**
     * 저장된 Device ID 획득
     */
    fun getDeviceId() : Long? {
        if(deviceId == -1L) {
            deviceId = PreferenceUtils.getLong(applicationContext, Value.DEVICE_ID.type)
        }
        return if(deviceId != -1L) deviceId else null
    }

    /**
     * 저장된 학교 ID
     */
    fun getSchoolId() : Int? {
        if(schoolId == Constant.INVALID_SCHOOL_ID) {
            schoolId = PreferenceUtils.getInt(applicationContext, Value.SCHOOL_ID.type, Constant.INVALID_SCHOOL_ID)
        }
        Logger.d("----- schoolId = $schoolId")
        return if(schoolId != Constant.INVALID_SCHOOL_ID) schoolId else null
    }

    /**
     * Background 작업을 위한 workerManager
     */
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }

}