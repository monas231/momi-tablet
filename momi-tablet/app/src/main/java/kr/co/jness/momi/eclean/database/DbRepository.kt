package kr.co.jness.momi.eclean.database

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import kr.co.jness.momi.eclean.model.*
import kr.co.jness.momi.eclean.utils.Logger
import javax.inject.Inject

class DbRepository @Inject constructor(
    private val apiCacheDao: ApiCacheDao,
    private val ruleInfoDao: RuleInfoDao,
    private val localVideoHashDao: LocalVideoHashDao,
    private val videoHashDao: VideoHashDao,
    private val whitelistDao: WhitelistDao,
    private val wordFilterDao: WordFilterDao,
    private val localWhitelistDao: LocalWhitelistDao
) {

    /**
     * 차단 API 결과 조회
     */
    fun getApiCache(info: String): Single<ApiCacheVO> =
        apiCacheDao.loadInfo(info)

    /**
     * 차단 API 결과 저장
     */
    fun insertApiCache(apiCache: ApiCacheVO) : Completable =
        apiCacheDao.insert(apiCache)

    /**
     * 차단 API 삭제
     */
    fun deleteAllApiCache() = apiCacheDao.deleteAllApiCache()

    /**
     * 정책정보 조회
     */
    fun loadRuleInfo() =
        ruleInfoDao.loadRuleInfo()

    /**
     * 정책정보 저장
     */
    suspend fun setRuleInfo(info: List<RuleInfoVO>) {
        Logger.d("----- setRuleInfo")
        ruleInfoDao.setRuleInfo(info)
    }

    /**
     * assets video hash 값의 마지막 ID
     */
    fun getLocalVideoHashLastId() : Single<Int> = localVideoHashDao.getLastHashId()

    /**
     * assets video hash에서 hash로 count가 있는지 검색
     */
    fun getLocalVideoHashCount(hash: String) : Single<Int> = localVideoHashDao.getVideoHashCount(hash)

    /**
     * assets video hash값 추가
     */
    fun insertLocalVideoHashs(info: List<LocalVideoHashVO>) : Completable = localVideoHashDao.insertAllLocalVideoHash(info)

    /**
     * delete assets video
     */
    fun deleteLocalVideoHashTable() : Completable = localVideoHashDao.deleteAllVideoHash()

    /**
     * server video hash에서 hash로 count가 있는지 검색
     */
    fun getVideoHashCount(hash: String) : Single<Int> = videoHashDao.getVideoHashCount(hash)

    /**
     * videohash에 filepath로 이전 version 정보가 있는지 체크
     */
    fun getOldCountByPathVersionVideoHash(filepath: String, version : Int) : Single<Int> = videoHashDao.getOldCountByPathVersion(filepath, version)

    /**
     * videoHash 테이블에 저장된 값이 있는지 확인한다.
     */
    fun existVideoHashData(version : Int) : Single<Int> = videoHashDao.existData(version)

    /**
     * videohash에 filepath의 이전 version 정보 삭제
     */
    fun deleteOldByPathVideoHash(filepath: String, version : Int) : Completable = videoHashDao.deleteOldByPath(filepath, version)


    /**
     * server video hash값 추가
     */
    fun insertVideoHashs(info: List<VideoHashVO>) : Completable = videoHashDao.insertAllVideoHash(info)

    /**
     * delete server video
     */
    fun deleteVideoHashTable() : Completable = videoHashDao.deleteAllVideoHash()


    /**
     * whitelist에 포함되는 항목인지 체크
     */
//    fun fineInWhitelist(info: String) : Single<WhitelistVO> = whitelistDao.fineInWhitelist(info)



    /**
     * whitelist에 filepath로 이전 version 정보가 있는지 체크
     */
    fun getOldCountByPathVersionWhitelist(filepath: String, version : Int) : Single<Int> = whitelistDao.getOldCountByPathVersion(filepath, version)

    /**
     * filepath의 이전 version 정보 삭제
     */
    fun deleteOldByPathWhitelist(filepath: String, version : Int) : Completable = whitelistDao.deleteOldByPath(filepath, version)

    /**
     * whitelist에 포함되는 갯수
     */
    fun getWhitelistCount(info: String) : Single<Int> = whitelistDao.getWhitelistCount(info)

    /**
     * wihtelist 추가
     */
    fun insertWhitelist(info: List<WhitelistVO>) : Completable = whitelistDao.insertAllWhitelist(info)


    /**
     * whitelist 테이블에 저장된 값이 있는지 확인한다.
     */
    fun existWhitelistData(version : Int) : Single<Int> = whitelistDao.existData(version)

    /**
     * delete whitelist
     */
//    fun deleteWhitelist() : Completable = whitelistDao.deleteAllWhitelist()


    /**
     * wordfilter에 포함되는 항목인지 체크
     */
    fun loadAllWordFilter() : Single<List<WordFilterVO>> = wordFilterDao.loadAllWordFilter()

    /**
     * wordfilter 추가
     */
    fun insertWordFilter(info: List<WordFilterVO>) : Completable = wordFilterDao.insertAllWordFilter(info)

    /**
     * delete wordfilter
     */
    fun deleteWordFilter() : Completable = wordFilterDao.deleteWordFilter()

    /**
     * wordfilter 테이블에 저장된 값이 있는지 확인한다.
     */
    fun existWordFilterData(version : Int) : Single<Int> = wordFilterDao.existData(version)


    /**
     * wordfilter에 filepath로 이전 version 정보가 있는지 체크
     */
    fun getOldCountByPathVersionWordFilter(filepath: String, version : Int) : Single<Int> = wordFilterDao.getOldCountByPathVersion(filepath, version)

    /**
     * wordfilter에 filepath의 이전 version 정보 삭제
     */
    fun deleteOldByPathWordFilter(filepath: String, version : Int) : Completable = wordFilterDao.deleteOldByPath(filepath, version)


    /**
     * local whitelist에 포함되는 항목인지 체크
     */
//    fun findInLocalWhitelist(info: String) : Single<LocalWhitelistVO> = localWhitelistDao.fineInWhitelist(info)

    /**
     * local wihtelist 추가
     */
    fun insertLocalWhitelist(list: List<LocalWhitelistVO>) : Completable = localWhitelistDao.insertAllWhitelist(list)

    /**
     * delete whitelist
     */
    fun deleteLocalWhitelist() : Completable = localWhitelistDao.deleteAllWhitelist()


    /**
     * local whitelist에 포함되는 갯수
     */
    fun getLocalWhitelistCount(info: String) : Single<Int> = localWhitelistDao.getWhitelistCount(info)

}