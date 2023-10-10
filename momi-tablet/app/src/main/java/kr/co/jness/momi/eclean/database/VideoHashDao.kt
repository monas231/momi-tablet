package kr.co.jness.momi.eclean.database

import androidx.room.*
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import kr.co.jness.momi.eclean.model.VideoHashVO

@Dao
abstract class VideoHashDao {

    /**
     * 모든 데이터를 저장한다.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAllVideoHash(videoHashs: List<VideoHashVO>) : Completable

    /**
     * 모든 데이터를 지운다.
     */
    @Query("DELETE FROM whitelist")
    abstract fun deleteAllVideoHash() : Completable

    /**
     * 모든 데이터를 지운 후 저장한다.
     */
    @Transaction
    open suspend fun setAllVideoHash(videoHashs: List<VideoHashVO>) {
        deleteAllVideoHash()
        insertAllVideoHash(videoHashs)
    }

    /**
     * hash 값이 일치하는 특정 데이터를 가져온다.
     */
    @Query("SELECT * FROM video_hashs WHERE hash_string = :hashString LIMIT 1")
    abstract suspend fun loadVideoHash(hashString: String): VideoHashVO?

    /**
     * 모든 데이터를 가져온다.
     */
    @Query("SELECT * FROM video_hashs")
    abstract suspend fun loadAllVideoHash(): List<VideoHashVO>

    /**
     * 여러개의 hash 값과 일치하는 데이터를 가져온다.
     */
    @Query("SELECT * FROM video_hashs WHERE hash_string IN (:hashStrings)")
    abstract suspend fun loadVideoHashs(hashStrings: List<String>): List<VideoHashVO>

    /**
     * hash 값이 일치하는 데이터 수를 가져온다.
     */
    @Query("SELECT COUNT(id) FROM video_hashs WHERE hash_string = :hash LIMIT 1")
    abstract fun getVideoHashCount(hash: String): Single<Int>

    /**
    * version 이하의 filepath항목이 있는지 확인한다.
    */
    @Query("SELECT COUNT(id) FROM video_hashs WHERE filepath = :filepath and version < :version LIMIT 1")
    abstract fun getOldCountByPathVersion(filepath: String, version : Int): Single<Int>


    /**
     *  version 이하의 filepath항목을 삭제한다.
     */
    @Query("DELETE FROM video_hashs WHERE filepath = :filepath and version < :version")
    abstract fun deleteOldByPath(filepath: String, version : Int): Completable

    /**
     * 저장된 값이 있는지 확인한다.
     */
    @Query("SELECT COUNT(id) FROM video_hashs WHERE version = :version LIMIT 1")
    abstract fun existData(version : Int): Single<Int>


}