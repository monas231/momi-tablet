package kr.co.jness.momi.eclean.database

import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.functions.BiFunction
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.schedulers.Schedulers
import kr.co.jness.momi.eclean.api.ApiRepository
import kr.co.jness.momi.eclean.model.FileInfo
import kr.co.jness.momi.eclean.model.VideoHashVO
import kr.co.jness.momi.eclean.model.WhitelistVO
import kr.co.jness.momi.eclean.model.WordFilterVO
import kr.co.jness.momi.eclean.module.FilterWordScoreFilter
import kr.co.jness.momi.eclean.utils.Logger
import java.util.*
import javax.inject.Inject

class NetworkFileUpdater @Inject constructor(
    private val dbRepository: DbRepository,
    private val apiRepository: ApiRepository,
    private val filterWord: FilterWordScoreFilter
) {

    private val fileQueue : Queue<FileInfo> = LinkedList()
    var downloadFile : FileInfo? = null
    private val disposable = CompositeDisposable()

    fun addFileList(files: List<FileInfo>) {

        if(!files.isNullOrEmpty()) {
            fileQueue.addAll(files)
            downloadNext(false)
        }
    }

    fun downloadNext(finishJob : Boolean = true) {

        if(finishJob) {
            downloadFile = null
        }

        Logger.d("----- [NetworkUpdater] downloadNext")

        if(downloadFile == null) {

            Logger.d("----- [NetworkUpdater] downloadFile == null")

            downloadFile = fileQueue.poll()
            if(downloadFile != null) {

                Logger.d("----- [NetworkUpdater] fileQueue.poll and exist")

                downloadFile?.let {

                    when (it.type) {
                        1 -> {
                            /**
                             * whitelist
                             */
                            updateWhiteList(it)
                        }
                        2 -> {
                            /**
                             * 동영상해시
                             */
                            updateVideoHash(it)
                        }
                        3 -> {
                            /**
                             * 키워드차단
                             */
                            updateWordFilter(it)
                        }
                    }
                }
            } else {
                disposable.clear()

                Logger.d("----- [NetworkUpdater] fileQueue.poll is finish")
            }
        }

    }

    /**
     * whitelist 파일 다운로드
     */
    private fun downloadWhiteList(fileInfo: FileInfo) {

        Logger.d("----- [WHITELIST] downloadWhiteList = ${fileInfo.path}")

        disposable += apiRepository.getFileDownload(fileInfo.path)
            .map { body ->
                body.byteStream().bufferedReader().useLines { lines ->
                    lines.map { line ->
                        val info = line.trim()
                        WhitelistVO(fileInfo.version, fileInfo.path, info,1)
                    }.toList()
                }
            }
            .subscribeOn(Schedulers.io())
            .subscribe({ whitelist ->
                Logger.d("----- [WHITELIST] insertWhitelist")
                disposable += dbRepository.insertWhitelist(whitelist)
                    .subscribeOn(Schedulers.io())
                    .doFinally {
                        downloadNext()
                    }
                    .subscribe({
                        Logger.d("----- [WHITELIST] insert server finish")
                    }, {
                        Logger.e(it.message ?: "")
                    })

            }, {
                Logger.e(it.message ?: "")
                downloadNext()
            })
    }

    /**
     * whitelist 정보 업데이트
     */
    private fun updateWhiteList(fileInfo: FileInfo) {
        try {
            Logger.d("----- [WHITELIST] updateWhiteList : ${fileInfo.path}, ${fileInfo.version}")
            /**
             * 오래된 항목이 있는지 체크
             */
            disposable += Single.zip(
                dbRepository.existWhitelistData(fileInfo.version),
                dbRepository.getOldCountByPathVersionWhitelist(fileInfo.path, fileInfo.version),
                BiFunction<Int, Int, List<Int>> { existCnt, oldCnt -> listOf(existCnt, oldCnt) }
            ).subscribe { t1, t2 ->

                val existCnt = t1.first()
                val oldCnt = t1.last()

                Logger.d("----- [WHITELIST] updateWhiteList : existCnt = $existCnt, oldCnt = $oldCnt")

                if(existCnt == 0) {
                    /**
                     * DB에 데이터가 없으면 다운로드.
                     */
                    downloadWhiteList(fileInfo)
                } else {
                    if(oldCnt>0) {
                        /**
                         * 오래된 whitelist가 있으면 삭제
                         */

                        Logger.d("----- [WHITELIST] deleteOldByPathWhitelist ${fileInfo.path}, ${fileInfo.version}")
                        dbRepository.deleteOldByPathWhitelist(fileInfo.path, fileInfo.version)
                            .subscribeOn(Schedulers.io())
                            .subscribe{
                                /**
                                 * whitelist 최신 버전 다운로드
                                 */
                                downloadWhiteList(fileInfo)
                            }
                    } else {
                        downloadNext()
                    }
                }
            }

        } catch (e: Exception) {
            Logger.d("----- [WHITELIST] updateWhiteList error = ${e.message}")
            downloadNext()
        }
    }

    /**
     * videohash 파일 다운로드
     */
    private fun downloadVideoHash(fileInfo: FileInfo) {

        Logger.d("----- [VIDEOHASH] getFileDownload fileInfo.path = $fileInfo.path")

        disposable += apiRepository.getFileDownload(fileInfo.path)
            .map { body ->
                body.byteStream().bufferedReader().useLines { lines ->
                    lines.map { line ->
                        val parsed = line.split("\t")
                        VideoHashVO(
                            fileInfo.version,
                            parsed[0],
                            fileInfo.path
                        )
                    }.toList()
                }
            }
            .subscribeOn(Schedulers.io())
            .subscribe({ hashs ->
                Logger.d("----- [VIDEOHASH] insertVideoHashs")
                disposable += dbRepository.insertVideoHashs(hashs)
                    .doFinally {
                        downloadNext()
                    }
                    .subscribe({
                        Logger.d("----- [VIDEOHASH] insert server finish")
                    }, {
                        Logger.d("----- [VIDEOHASH] insert server error = ${it.message}")
                    })

            }, {
                Logger.d("----- [VIDEOHASH] download error = ${it.message}")
                downloadNext()
            })
    }

    /**
     * video hash 정보 업데이트
     */
    private fun updateVideoHash(fileInfo: FileInfo) {

        try {
            Logger.d("----- [VIDEOHASH] updateVideoHash : ${fileInfo.path}, ${fileInfo.version}")
            /**
             * 오래된 항목이 있는지 체크
             */

            disposable += Single.zip(
                dbRepository.existVideoHashData(fileInfo.version),
                dbRepository.getOldCountByPathVersionVideoHash(fileInfo.path, fileInfo.version),
                BiFunction<Int, Int, List<Int>> { existCnt, oldCnt -> listOf(existCnt, oldCnt) }
            ).subscribe { t1, t2 ->


                val existCnt = t1.first()
                val oldCnt = t1.last()

                Logger.d("----- [VIDEOHASH] updateVideoHash : existCnt = $existCnt, oldCnt = $oldCnt")

                if(existCnt == 0) {
                    /**
                     * DB에 데이터가 없으면 다운로드.
                     */
                    downloadVideoHash(fileInfo)
                } else {
                    if(oldCnt>0) {
                        /**
                         * 오래된 videohash가 있으면 삭제 후 다운로드
                         */
                        Logger.d("----- [VIDEOHASH] deleteOldByPathVideoHash ${fileInfo.path}, ${fileInfo.version}")
                        dbRepository.deleteOldByPathVideoHash(fileInfo.path, fileInfo.version)
                            .subscribeOn(Schedulers.io())
                            .subscribe{
                                downloadVideoHash(fileInfo)
                            }
                    } else {
                        downloadNext()
                    }
                }
            }

        } catch (e: Exception) {
            Logger.d("----- [VIDEOHASH] updateVideoHash error = ${e.message}")
            downloadNext()
        }

    }


    /**
     * videohash 파일 다운로드
     */
    private fun downloadWordFilter(fileInfo: FileInfo) {

        Logger.d("----- [KEYWORD] getFileDownload fileInfo.path = $fileInfo.path")

        disposable += apiRepository.getFileDownload(fileInfo.path)
            .map { body ->
                body.byteStream().bufferedReader().useLines { lines ->
                    lines.map { line ->
                        val parsed = line.split("\t")
                        WordFilterVO(fileInfo.version, fileInfo.path, parsed[0], parsed[1].toInt())
                    }.toList()
                }
            }
            .subscribeOn(Schedulers.io())
            .subscribe({ wordFilters ->
                Logger.d("----- [KEYWORD] insertWordFilter")
                disposable += dbRepository.insertWordFilter(wordFilters)
                    .doFinally {
                        Logger.d("----- [KEYWORD] loadServerFilterWord")
                        filterWord.loadServerFilterWord()?.let {
                            disposable += it
                        }
                        downloadNext()
                    }
                    .subscribe({
                        Logger.d("----- [KEYWORD] insert server finish")
                    }, {
                        Logger.e(it.message ?: "")
                    })

            }, {
                Logger.e(it.message ?: "")
                downloadNext()
            })
    }


    /**
     * 키워드 차단 정보 업데이트
     */
    private fun updateWordFilter(fileInfo: FileInfo) {
        try {

            Logger.d("----- [KEYWORD] updateWordFilter : ${fileInfo.path}, ${fileInfo.version}")

            disposable += Single.zip(
                dbRepository.existWordFilterData(fileInfo.version),
                dbRepository.getOldCountByPathVersionWordFilter(fileInfo.path, fileInfo.version),
                BiFunction<Int, Int, List<Int>> { existCnt, oldCnt -> listOf(existCnt, oldCnt) }
            ).subscribe { t1, t2 ->

                val existCnt = t1.first()
                val oldCnt = t1.last()

                Logger.d("----- [KEYWORD] updateWordFilter : existCnt = $existCnt, oldCnt = $oldCnt")

                if(existCnt == 0) {
                    /**
                     * DB에 데이터가 없으면 다운로드.
                     */
                    downloadWordFilter(fileInfo)
                } else {
                    if(oldCnt>0) {
                        /**
                         * 오래된 wordfilter가 있으면 삭제
                         */
                        Logger.d("----- [KEYWORD] deleteOldByPathWordFilter ${fileInfo.path}, ${fileInfo.version}")
                        dbRepository.deleteOldByPathWordFilter(fileInfo.path, fileInfo.version)
                            .subscribeOn(Schedulers.io())
                            .subscribe{
                                /**
                                 * wordfilter 최신 버전 다운로드
                                 */
                                downloadWordFilter(fileInfo)
                            }
                    } else {
                        downloadNext()
                    }
                }
            }

        } catch (e: Exception) {
            Logger.e(e.message ?: "")
            downloadNext()
        }
    }
}