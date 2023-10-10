package kr.co.jness.momi.eclean.database

import androidx.room.*
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import kr.co.jness.momi.eclean.model.*

@Dao
abstract class LocalWhitelistDao {

    /**
     * 모든 데이터를 저장한다.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAllWhitelist(whitelist: List<LocalWhitelistVO>) : Completable

    /**
     * 모든 데이터를 지운다.
     */
    @Query("DELETE FROM local_whitelist")
    abstract fun deleteAllWhitelist() : Completable

    /**
     * hash 값이 일치하는 특정 데이터를 가져온다.
     */
//    @Query("SELECT * FROM $LOCAL_WHITELIST_TABLE WHERE info LIKE :info LIMIT 1")
//    abstract fun fineInWhitelist(info: String): Single<LocalWhitelistVO>

    /**
     * 모든 데이터를 가져온다.
     */
    @Query("SELECT * FROM local_whitelist")
    abstract suspend fun loadAllWhitelist(): List<LocalWhitelistVO>

    /**
     * info 값을 포함하는 데이터 수를 가져온다.
     */
    @Query("SELECT COUNT(id) FROM local_whitelist WHERE info LIKE :info LIMIT 1")
    abstract fun getWhitelistCount(info: String): Single<Int>
}