package kr.co.jness.momi.eclean.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.schedulers.Schedulers
import kr.co.jness.momi.eclean.api.ApiRepository
import kr.co.jness.momi.eclean.common.EcleanApplication
import kr.co.jness.momi.eclean.database.DbRepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class ProhibitViewModel @Inject constructor(
    application: Application,
    private val apiRepo: ApiRepository,
    private val dbRepository: DbRepository
) : BaseViewModel(application, apiRepo, dbRepository) {

    val timerCount = MutableLiveData<Long>()

    fun startTimer(start: Long) {

        disposable +=
            Flowable.interval(1, TimeUnit.SECONDS)
            .take(start+1)
            .subscribeOn(Schedulers.trampoline())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                timerCount.value = start-it
            }
    }

}
