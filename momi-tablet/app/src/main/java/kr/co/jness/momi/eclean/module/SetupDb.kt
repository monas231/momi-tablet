package kr.co.jness.momi.eclean.module

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kr.co.jness.momi.eclean.database.DbRepository
import kr.co.jness.momi.eclean.model.*
import kr.co.jness.momi.eclean.utils.Constant
import kr.co.jness.momi.eclean.utils.Logger
import kr.co.jness.momi.eclean.utils.PreferenceUtils
import kr.co.jness.momi.eclean.utils.Value

object SetupDb {

    fun insertLocalVideoHashDB(context: Context, dbRepository: DbRepository) : Disposable? {

        val videoHashVersion = PreferenceUtils.getInt(context, Value.LOCAL_VIDEO_HASH_VERSION.type)

        Logger.d("----- [VIDEOHASH] videoHashVersion = $videoHashVersion")

        if(videoHashVersion < Constant.LOCAL_VIDEO_HASH_VERSION) {

            context.assets.open("video_hash_list.json").use {  inputStream ->

                JsonReader(inputStream.reader()).use { jsonReader ->
                    val type = object : TypeToken<List<LocalVideoHashVO>>() {}.type
                    val localHashs : List<LocalVideoHashVO> =  Gson().fromJson(jsonReader, type)

                    return dbRepository.deleteLocalVideoHashTable()
                        .andThen(dbRepository.insertLocalVideoHashs(localHashs))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            PreferenceUtils.putInt(context, Value.LOCAL_VIDEO_HASH_VERSION.type, Constant.LOCAL_VIDEO_HASH_VERSION)
                            Logger.d("----- [VIDEOHASH] finish insert local video hash")
                        }, {
                            Logger.d("----- [VIDEOHASH] finish insert local video hash error = ${it.message ?: ""}")
                        })
                }
            }
        } else {
            Logger.d("----- [VIDEOHASH] local video hash is latest version")
        }
        return null
    }

    fun insertLocalWhitelistDB(context: Context, dbRepository: DbRepository) : Disposable? {

        val localWhitelistVersion = PreferenceUtils.getInt(context, Value.LOCAL_WHITE_LIST_VERSION.type)

        Logger.d("----- [WHITELIST] localWhitelistVersion = $localWhitelistVersion")

        if(localWhitelistVersion < Constant.LOCAL_WHITELIST_VERSION) {

            context.assets.open("white_list.json").use {  inputStream ->
                JsonReader(inputStream.reader()).use { jsonReader ->
                    val type = object : TypeToken<List<LocalWhitelistVO>>() {}.type
                    val whitelist: List<LocalWhitelistVO> = Gson().fromJson(jsonReader, type)

                    whitelist.forEach {
                        if (!it.info.isNullOrEmpty() && it.info.endsWith("/")) {
                            it.info = it.info.substring(0, it.info.length - 1)
                        }
                    }

                    return dbRepository.deleteLocalWhitelist()
                        .andThen(dbRepository.insertLocalWhitelist(whitelist))
                        .subscribeOn(Schedulers.io())
                        .subscribe({
                            PreferenceUtils.putInt(context, Value.LOCAL_WHITE_LIST_VERSION.type, Constant.LOCAL_WHITELIST_VERSION)
                            Logger.d("----- [WHITELIST] finish insert local whitelist")
                        }, {
                            Logger.d("----- [WHITELIST] finish insert local whitelist error = ${it.message ?: ""}")
                        })

                }
            }
        } else {
            Logger.d("----- [WHITELIST] local whitelist is latested version")
        }

        return null
    }

}