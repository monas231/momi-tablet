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
class TrialViewModel @Inject constructor(
    application: Application,
    private val apiRepo: ApiRepository,
    private val dbRepository: DbRepository
) : BaseViewModel(application, apiRepo, dbRepository) {

    val step = MutableLiveData<Int>(0)
    val schoolType = MutableLiveData<Int>(0)
    val name = MutableLiveData<String>()
    val schoolName = MutableLiveData<String>()
    val emailId = MutableLiveData<String>()
    val domain = MutableLiveData<String>()
    val phone1 = MutableLiveData<String>()
    val phone2 = MutableLiveData<String>()
    val phone3 = MutableLiveData<String>()
    val agree = MutableLiveData<Boolean>()
    val agency = MutableLiveData<String>()
    val area = MutableLiveData<Int>()
    val schoolList = MutableLiveData<List<School>>()
    val deviceId = MutableLiveData<Long>()
    val selectedSchool = MutableLiveData<School>()
    val errorMsg = MutableLiveData<String>()

    fun getPhoneNumber() = "${phone1.value}${phone2.value}${phone3.value}"

    fun getEmailAddress() = "${emailId.value}@${domain.value}"

    fun fetchSchoolList(deviceId: Long, name: String) {
        disposable += apiRepo.searchSchool(deviceId, name)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                it.data?.schoolList?.let {
                    if(it.isNotEmpty()) {
                        schoolList.value = it
                    } else {
                        schoolList.value = listOf<School>()
                    }
                } ?: kotlin.run {
                    schoolList.value = listOf<School>()
                }
                errorMsg.value = ""
            },{
                errorMsg.value = it.message
            })
    }

}