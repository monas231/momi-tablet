package kr.co.jness.momi.eclean.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.schedulers.Schedulers
import kr.co.jness.momi.eclean.api.ApiRepository
import kr.co.jness.momi.eclean.database.DbRepository
import kr.co.jness.momi.eclean.service.UrlInterceptorService
import kr.co.jness.momi.eclean.utils.PreferenceUtils
import kr.co.jness.momi.eclean.utils.Value
import javax.inject.Inject

@HiltViewModel
class LicenseViewModel @Inject constructor(
    application: Application,
    private val apiRepo: ApiRepository,
    private val dbRepository: DbRepository
) : BaseViewModel(application, apiRepo, dbRepository) {

    val apiResult = MutableLiveData<Int>()
    val errorMsg = MutableLiveData<String>()
    val deviceId = MutableLiveData<Long>()
    val license = MutableLiveData<String>()

    fun notifyInstallApp() {
        disposable += apiRepo.notifyInstallApp()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                deviceId.value = it.data.deviceId
            },{
                errorMsg.value = it.message
            })
    }

    fun checkLicense(context: Context, key: String, s_id: Long?=null, s_nm: String?=null, mail: String?=null, phone: String?=null) {

        deviceId.value?.let {
            disposable += apiRepo.checkLicense(it, key, s_id, s_nm, mail, phone)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if(it.resultCode == 1) {
                        saveLicense(context, key, it.data.schoolId, it.data.expiredYMD)
                        license.value = key
                        UrlInterceptorService.instance?.expiredLicense = false
                    }
                    apiResult.value = it.resultCode
                }, {
                    errorMsg.value = it.message
                })
        }
    }

    fun saveLicense(context: Context, license: String, schoolId: Int, expiration: String) {

        PreferenceUtils.putString(
            context,
            Value.LICENSE.type,
            license
        )

        expiration.let {
            PreferenceUtils.putString(
                context,
                Value.EXPIRARION_DATE.type,
                expiration
            )
        } ?: kotlin.run {
            PreferenceUtils.removeValue(
                context,
                Value.EXPIRARION_DATE.type
            )
        }

        PreferenceUtils.putInt(context, Value.SCHOOL_ID.type, schoolId)
    }
}