package kr.co.jness.momi.eclean.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.trello.rxlifecycle4.kotlin.bindToLifecycle
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.annotations.NonNull
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.schedulers.Schedulers
import kr.co.jness.momi.eclean.api.ApiRepository
import kr.co.jness.momi.eclean.database.DbRepository
import kr.co.jness.momi.eclean.dto.AppInstallDto
import kr.co.jness.momi.eclean.model.RuleInfoManager
import kr.co.jness.momi.eclean.utils.PreferenceUtils
import kr.co.jness.momi.eclean.utils.Value
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    private val apiRepo: ApiRepository,
    private val dbRepository: DbRepository
) : BaseViewModel(application, apiRepo, dbRepository) {

    val apiResult = MutableLiveData<Int>()
    val appInstallDto = MutableLiveData<AppInstallDto>()
    val ruleInfo by lazy { dbRepository.loadRuleInfo() }
    var ruleInfoManager: RuleInfoManager? = null
    private val context by lazy { application.applicationContext }
    val noticeInfo = MutableLiveData<String>()
    val schoolName = MutableLiveData<String>()
    val isDemo = MutableLiveData<Boolean>()
    val errorMsg = MutableLiveData<String>()

    fun notifyInstallApp() {
        disposable += apiRepo.notifyInstallApp()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                appInstallDto.value = it
                getNotice(it.data.deviceId, it.data.schoolId)
            }, {

            })
    }

    private fun getNoticeTopic() = PreferenceUtils.getString(context, Value.SUBSCRIBE_NOTICE.type)
    private fun getRuleTopic() = PreferenceUtils.getString(context, Value.SUBSCRIBE_RULE.type)

    fun testTCM(): Completable {
        return apiRepo.testFCM("notice", "test", topic = getNoticeTopic())
            .andThen {
                apiRepo.testFCM("rule", "test", topic = getRuleTopic())
            }
    }

    fun getNotice(deviceId: Long, schoolId: Int?) {
        disposable += apiRepo.noticeInfo(deviceId, schoolId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                it.data?.let {
                    noticeInfo.value = it.notice
                    schoolName.value = it.schoolName
                }
            }, {

            })
    }

}