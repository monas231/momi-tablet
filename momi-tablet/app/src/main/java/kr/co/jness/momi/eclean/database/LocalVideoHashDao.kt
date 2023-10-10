package kr.co.jness.momi.eclean.database

import androidx.room.*
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import kr.co.jness.momi.eclean.model.LocalVideoHashVO

@Dao
abstract class LocalVideoHashDao {

    /**
     * 모든 데이터를 저장한다.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAllLocalVideoHash(videoHashs: List<LocalVideoHashVO>) : Completable

    /**
     * 모든 데이터를 지운다.
     */
    @Query("DELETE FROM local_video_hashs")
    abstract fun deleteAllVideoHash() : Completable

    /**
     * 모든 데이터를 지운 후 저장한다.
     */
    @Transaction
    open suspend fun setAllVideoHash(videoHashs: List<LocalVideoHashVO>) {
        deleteAllVideoHash()
        insertAllLocalVideoHash(videoHashs)
    }

    /**
     * hash 값이 일치하는 데이터 수를 가져온다.
     */
    @Query("SELECT COUNT(id) FROM local_video_hashs WHERE hash = :hash LIMIT 1")
    abstract fun getVideoHashCount(hash: String): Single<Int>

    /**
     * 모든 데이터를 가져온다.
     */
    @Query("SELECT * FROM local_video_hashs")
    abstract suspend fun loadAllVideoHash(): List<LocalVideoHashVO>

    /**
     * 여러개의 hash 값과 일치하는 데이터를 가져온다.
     */
    @Query("SELECT * FROM local_video_hashs WHERE hash IN (:hash)")
    abstract suspend fun loadVideoHashs(hash: List<String>): List<LocalVideoHashVO>

    /**
     * 레코드 마지막의 hash의 id를 가져온다.
     */
    @Query("SELECT id FROM local_video_hashs LIMIT 1 ")
    abstract fun getLastHashId(): Single<Int>


}