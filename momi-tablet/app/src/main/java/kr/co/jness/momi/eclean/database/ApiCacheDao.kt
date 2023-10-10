package kr.co.jness.momi.eclean.database

import androidx.room.*
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import kr.co.jness.momi.eclean.model.ApiCacheVO

@Dao
abstract class ApiCacheDao {

    /**
     * 모든 데이터를 저장한다.
     */
    @Insert
    abstract suspend fun insertAll(apiCaches: List<ApiCacheVO>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(apiCache: ApiCacheVO) : Completable

    /**
     * 모든 데이터를 지운다.
     */
    @Query("DELETE FROM api_cache")
    abstract fun deleteAllApiCache() : Completable

    /**
     * hash 값이 일치하는 특정 데이터를 가져온다.
     */
    @Query("SELECT * FROM api_cache WHERE info = :info LIMIT 1")
    abstract fun loadInfo(info: String): Single<ApiCacheVO>

    /**
     * 모든 데이터를 가져온다.
     */
    @Query("SELECT * FROM api_cache")
    abstract suspend fun loadAll(): List<ApiCacheVO>

}