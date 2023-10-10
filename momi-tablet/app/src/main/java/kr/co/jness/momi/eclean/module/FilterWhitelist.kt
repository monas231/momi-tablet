package kr.co.jness.momi.eclean.module

import android.content.Context
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.BiFunction
import io.reactivex.rxjava3.schedulers.Schedulers
import kr.co.jness.momi.eclean.api.ApiRepository
import kr.co.jness.momi.eclean.database.DbRepository
import kr.co.jness.momi.eclean.model.Os
import kr.co.jness.momi.eclean.service.UrlInterceptorService
import kr.co.jness.momi.eclean.utils.Logger
import javax.inject.Inject

class FilterWhitelist @Inject constructor (private val dbRepository: DbRepository) {

    private val apiAllowInfos: MutableList<String> = mutableListOf()

    fun isWhitelistItem(info: String, callback: (Boolean) -> Unit) : Disposable {

        Logger.d("----- [WHITELIST] checkWhitelist domain = $info")

        return Single.zip(
            dbRepository.getWhitelistCount(info),
            dbRepository.getLocalWhitelistCount(info),
            BiFunction<Int, Int, Int> { localCount, serverCount -> localCount+serverCount }
        )
        .subscribeOn(Schedulers.io())
        .subscribe { t1, t2 ->
            var result = if(t1 == null) {
                false
            } else {
                t1>0
            }

            if(!result) {
                result = isWhitelistApiItem(info)
            }

            Logger.d("----- [WHITELIST] result = $result")
            t2?.message?.let {
                Logger.d("----- [WHITELIST] error = ${t2.message}")
            }
            callback.invoke(result)
        }
    }

    private fun isWhitelistApiItem(info: String) : Boolean {

        apiAllowInfos?.forEach {
            if(it.contains(info)) {
                Logger.d("----- [WHITELIST] isWhitelistApiItem")
                return true
            }
        }
        Logger.d("----- [WHITELIST] is not WhitelistApiItem")
        return false
    }

    fun setWhitelistApiItem(items : List<String>?) {


        apiAllowInfos.clear()
        items?.let {
            apiAllowInfos.addAll(items.toMutableList())

            Logger.d("----- [WHITELIST] setWhitelistApiItem items count = ${items.size}")
        }
    }
}