package kr.co.jness.momi.eclean.database

import androidx.room.*
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import kr.co.jness.momi.eclean.model.*

@Dao
abstract class WhitelistDao {

    /**
     * 모든 데이터를 저장한다.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAllWhitelist(whitelist: List<WhitelistVO>) : Completable

    /**
     * 모든 데이터를 지운다.
     */
    @Query("DELETE FROM whitelist")
    abstract fun deleteAllWhitelist() : Completable

    /**
     * 모든 데이터를 가져온다.
     */
    @Query("SELECT * FROM whitelist")
    abstract suspend fun loadAllWhitelist(): List<WhitelistVO>


    /**
     * info 값을 포함하는 데이터 수를 가져온다.
     */
    @Query("SELECT COUNT(id) FROM whitelist WHERE info LIKE :info LIMIT 1")
    abstract fun getWhitelistCount(info: String): Single<Int>


    /**
     * version 이하의 filepath항목이 있는지 확인한다.
     */
    @Query("SELECT COUNT(id) FROM whitelist WHERE filepath = :filepath and version < :version LIMIT 1")
    abstract fun getOldCountByPathVersion(filepath: String, version : Int): Single<Int>


    /**
     * version 이하의 filepath항목을 삭제한다.
     */
    @Query("DELETE FROM whitelist WHERE filepath = :filepath and version < :version")
    abstract fun deleteOldByPath(filepath: String, version : Int): Completable

    /**
     * 저장된 값이 있는지 확인한다.
     */
    @Query("SELECT COUNT(id) FROM whitelist WHERE version = :version LIMIT 1")
    abstract fun existData(version : Int): Single<Int>

}