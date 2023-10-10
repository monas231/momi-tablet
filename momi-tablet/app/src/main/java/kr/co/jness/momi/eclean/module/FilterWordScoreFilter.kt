package kr.co.jness.momi.eclean.module

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.schedulers.Schedulers
import kr.co.jness.momi.eclean.api.ApiRepository
import kr.co.jness.momi.eclean.database.DbRepository
import kr.co.jness.momi.eclean.dto.KeyWord
import kr.co.jness.momi.eclean.model.FilterWord
import kr.co.jness.momi.eclean.model.WordFilterVO
import kr.co.jness.momi.eclean.utils.Logger
import javax.inject.Inject

class FilterWordScoreFilter @Inject constructor(
    private val appContext: Context,
    private val dbRepository: DbRepository,
    private val apiRepository: ApiRepository
) {


    var filterWords = listOf<FilterWord>()
    var serverFilterWords = listOf<WordFilterVO>()
    var apiWordFilterWords = listOf<KeyWord>()

    fun loadFilterWord() {

        try {
            if(filterWords.isNullOrEmpty()) {
                appContext.assets.open("filter_word.json").use { inputStream ->
                    JsonReader(inputStream.reader()).use { jsonReader ->
                        val type = object : TypeToken<List<FilterWord>>() {}.type
                        filterWords = Gson().fromJson(jsonReader, type)
                    }
                }
            }
        } catch (e: Exception) {
            Logger.e(e.message ?: "")
        }

    }

    fun loadServerFilterWord() : Disposable {

        return dbRepository.loadAllWordFilter()
            .subscribeOn(Schedulers.io())
            .doOnSubscribe {
                Logger.d("----- [KEYWORD] loadFilterWord filterWords Cnt = ${filterWords?.size}")
            }
            .subscribe({
                try {
                    loadFilterWord()
                    Logger.d("----- [KEYWORD] loadFilterWord filterWords Cnt = ${filterWords?.size}")

                    serverFilterWords = it
                    Logger.d("----- [KEYWORD] loadFilterWord serverFilterWords Cnt = ${serverFilterWords?.size}")
                } catch (e: Exception) {
                    Logger.e(e.message ?: "")
                }
            }, {
                serverFilterWords = listOf()
            })
    }

    fun isOverWordScore(input: String, limit: Int): Boolean {
        try {
            var score = 0

            var replaceInput = input.replace("[~!@#$%^&*()\\-_+=,./?\'\"\\[\\]]".toRegex(), "")

            Logger.d("----- [KEYWORD] FilterWord after = $replaceInput")

            Logger.d("----- [KEYWORD] isOverWordScore filterWords Cnt = ${filterWords.size}")
            Logger.d("----- [KEYWORD] isOverWordScore serverFilterWords Cnt = ${serverFilterWords.size}")

            apiWordFilterWords?.forEach {
                score += if (input.contains(it.info, true)) {
                    it.score
                } else {
                    0
                }

                if (score >= limit) {
                    Logger.d("----- [KEYWORD] FilterWord score = $score")
                    return true
                }
            }

            filterWords?.forEach {
                score += if (input.contains(it.strWord, true)) {
                    it.nScore
                } else {
                    0
                }

                if (score >= limit) {
                    Logger.d("----- [KEYWORD] FilterWord score = $score")
                    return true
                }
            }

            serverFilterWords?.forEach {
                score += if (input.contains(it.name, true)) {
                    it.score
                } else {
                    0
                }
                if (score >= limit) {
                    Logger.d("----- [KEYWORD] FilterWord score = $score")
                    return true
                }
            }
            Logger.d("----- [KEYWORD] FilterWord score = $score")

        } catch (e: Exception) {
            Logger.e(e.message ?: "")
        }
        return false
    }

    fun updateKeywordApi(deviceId: Long, schoolId: Int?) : Disposable {

        Logger.d("----- [KEYWORD] updateKeywordApi")

        return apiRepository.checkKeyword(deviceId, schoolId)
            .subscribeOn(Schedulers.io())
            .subscribe(
                {
                    try {
                        Logger.d("----- [KEYWORD] updateKeywordApi count = ${it.data.keywordList.size}")
                        apiWordFilterWords = it.data.keywordList
                    } catch (e: Exception) {
                        Logger.e(e.message ?: "")
                    }
                }, {
                    Logger.e(it.message ?: "")
                }
            )
    }
}