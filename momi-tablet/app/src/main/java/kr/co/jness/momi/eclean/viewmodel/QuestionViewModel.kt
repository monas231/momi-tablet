package kr.co.jness.momi.eclean.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
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
import kr.co.jness.momi.eclean.dto.School
import kr.co.jness.momi.eclean.dto.SchoolDto
import kr.co.jness.momi.eclean.model.RuleInfoManager
import kr.co.jness.momi.eclean.utils.PreferenceUtils
import kr.co.jness.momi.eclean.utils.Value
import javax.inject.Inject

@HiltViewModel
class QuestionViewModel @Inject constructor(
    application: Application,
    private val apiRepo: ApiRepository,
    private val dbRepository: DbRepository
) : BaseViewModel(application, apiRepo, dbRepository) {

    val name = MutableLiveData<String>()
    val type = MutableLiveData<Int>()
    val emailId = MutableLiveData<String>()
    val domain = MutableLiveData<String>()
    val phone1 = MutableLiveData<String>()
    val phone2 = MutableLiveData<String>()
    val phone3 = MutableLiveData<String>()
    val q_type = MutableLiveData<Int>()
    val text = MutableLiveData<String>()
    val errorMsg = MutableLiveData<String>()
    val apiResult = MutableLiveData<Int>()

    fun getPhoneNumber() = "${phone1.value}${phone2.value}${phone3.value}"

    fun getEmailAddress() = "${emailId.value}@${domain.value}"

    fun allDataIsSet(): Boolean {
        return !name.value.isNullOrEmpty()
                && type.value != null
                && type.value != 0
                && !phone1.value.isNullOrEmpty()
                && !phone2.value.isNullOrEmpty()
                && !phone3.value.isNullOrEmpty()
                && !emailId.value.isNullOrEmpty()
                && !domain.value.isNullOrEmpty()
                && q_type.value != null
                && q_type.value != 0
                && !text.value.isNullOrEmpty()
    }

    fun sendQuestion(deviceId: Long) {
        disposable += apiRepo.sendQuestion(
            deviceId,
            name.value, type.value, getEmailAddress(), getPhoneNumber(), q_type.value, text.value)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                errorMsg.value = ""
                apiResult.value = it.resultCode
            },{
                errorMsg.value = it.message
            })
    }

}